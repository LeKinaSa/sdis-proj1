package peer.messages;

import peer.Channel;
import peer.ChannelName;
import peer.PeerDebugger;
import peer.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;

public class RestoreSenderMessage extends Message {
    private final String fileId;
    private final int chunkNo;

    public RestoreSenderMessage(Channel mc, Channel mdb, Channel mdr, String version, int peerId, String fileId, int chunkNo) {
        super(ChannelName.MC, mc, mdb, mdr, version, peerId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    public String toString() {
        return "[peer" + this.messagePeerId + "] v" + this.version + " - Restore initiator " + this.fileId + ":" + this.chunkNo;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> GETCHUNK <SenderId> <FileId> <ChunkNo> "
        String message = this.version + " GETCHUNK " + this.messagePeerId + " " + this.fileId + " " + this.chunkNo + " ";

        // Get Message Bytes (Header)
        return generateMessageWithoutBody(message);
    }

    @Override
    public void answer(int id) {
        // New Thread to deal with the answer
        Thread thread = new Thread(() -> {
            if (this.version.equals("1.0")) {
                this.regularAnswer(id);
            }
            else {
                this.enhancedAnswer(id);
            }
        });
        thread.start();
    }

    public void regularAnswer(int id) {
        // Search for the chunkContent
        byte[] chunkContent = Utils.load(id, this.fileId, this.chunkNo);
        if (chunkContent == null) {
            return;
        }

        // Get message
        Message message = new RestoreReceiverMessage(this.mc, this.mdb, this.mdr, this.version, id, this.fileId, this.chunkNo, chunkContent);

        // Open MDR Socket
        MulticastSocket socket;
        try {
            socket = new MulticastSocket(this.mdr.port);
        }
        catch (IOException exception) {
            PeerDebugger.println("Error occurred");
            return;
        }
        try {
            socket.joinGroup(this.mdr.ip);
        }
        catch (IOException exception) {
            PeerDebugger.println("Error occurred: " + exception.getMessage());
            socket.close();
            return;
        }

        // Verify if no other peer is faster to send the chunk
        byte[] buf = new byte[Message.MESSAGE_SIZE];
        DatagramPacket p = new DatagramPacket(buf, buf.length);
        long initial_timestamp = System.currentTimeMillis();
        long current_timestamp = System.currentTimeMillis();
        int timeToWait = Utils.getRandomNumber(0, 401);
        boolean answerReceived = false;
        while (current_timestamp < initial_timestamp + timeToWait) {
            try {
                socket.setSoTimeout((int) (initial_timestamp + timeToWait - current_timestamp));
                socket.receive(p);
                Message answer = Message.parse(mc, mdb, mdr, p);
                if ((answer instanceof RestoreReceiverMessage) && (((RestoreReceiverMessage) answer).correspondsTo(fileId, chunkNo))) {
                    answerReceived = true;
                    break;
                }
            }
            catch (SocketTimeoutException exception) {
                // The time expired and no message was received
            }
            catch (IOException ignored) { }
            current_timestamp = System.currentTimeMillis();
        }

        // Send chunk if needed
        if (!answerReceived) {
            Utils.sendMessage(message);
        }

        // Close MDR Socket
        try {
            socket.leaveGroup(this.mdr.ip);
        }
        catch (Exception ignored) { }
        socket.close();
    }

    public void enhancedAnswer(int id) {
        // Search for the chunkContent
        byte[] chunkContent = Utils.load(id, this.fileId, this.chunkNo);
        if (chunkContent == null) {
            return;
        }

        // Open TCP socket
        Utils.pause(Utils.getRandomNumber(0, 401)); // TODO: is this line needed so that all the connections don't happen at the same time
        try (ServerSocket serverSocket = new ServerSocket(this.mdr.port)) {
            serverSocket.setSoTimeout(1000); // TODO: check timeout
            // Accept Socket Connection
            Socket socket;
            try {
                socket = serverSocket.accept();
            }
            catch (SocketTimeoutException exception) {
                regularAnswer(id);
                return;
            }

            // Write to the Socket
            OutputStream output = socket.getOutputStream();
            output.write(chunkContent);

            // Close Socket without Losing Information
            output.flush();
            socket.shutdownOutput();
            socket.close();

            // Information was successfully transmitted
        }
        catch (IOException ex) {
            regularAnswer(id);
        }
    }
}
