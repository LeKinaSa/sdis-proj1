package peer.messages;

import peer.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

public class ReclaimReceiverMessage extends Message {
    private final String fileId;
    private final int chunkNo;

    public ReclaimReceiverMessage(Channel mc, Channel mdb, Channel mdr, String version, int peerId, String fileId, int chunkNo) {
        super(ChannelName.MC, mc, mdb, mdr, version, peerId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    public String toString() {
        return "[peer" + this.messagePeerId + "] v" + this.version + " - Reclaim " + this.fileId + ":" + this.chunkNo;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> REMOVED <SenderId> <FileId> <ChunkNo> "
        String message = this.version + " REMOVED " + this.messagePeerId + " " + this.fileId + " " + this.chunkNo + " ";

        // Get Message Bytes (Header)
        return generateMessageWithoutBody(message);
    }

    @Override
    public Message answer(int id) {
        // Remove the chunk from the peer state
        ClientEndpoint.state.peerRemovedChunk(this.fileId, this.chunkNo, this.messagePeerId);
        // Verify if the replication is still acceptable
        if (ClientEndpoint.state.chunkWithSufficientReplication(this.fileId, this.chunkNo)) {
            return null;
        }
        // The chunk replication has dropped below the desired replication degree
        // New Thread to send the new putchunk message
        Thread thread = new Thread(() -> {
            // Get desired replication degree
            int replication = ClientEndpoint.state.getReplicationDegreeForChunk(this.fileId, this.chunkNo);
            // Search for the chunkContent
            byte[] chunkContent = Utils.load(id, this.fileId, this.chunkNo);
            if (chunkContent == null) {
                return;
            }

            // Get message
            Message message = new BackupSenderMessage(this.mc, this.mdb, this.mdr, this.version, id, this.fileId, this.chunkNo, replication, chunkContent);

            // Open MDB Socket
            MulticastSocket socket;
            try {
                socket = new MulticastSocket(this.mdb.port);
            }
            catch (IOException exception) {
                PeerDebugger.println("Error occurred");
                return;
            }
            try {
                socket.joinGroup(this.mdb.ip);
            }
            catch (IOException exception) {
                PeerDebugger.println("Error occurred: " + exception.getMessage());
                socket.close();
                return;
            }

            // Verify if no other peer is faster to send the putchunk
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
                    if ((answer instanceof BackupSenderMessage) && (((BackupSenderMessage) answer).correspondsTo(fileId, chunkNo))) {
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

            // Close MDB Socket
            try {
                socket.leaveGroup(this.mdb.ip);
            }
            catch (Exception ignored) { }
            socket.close();
        });
        thread.start();
        return null;
    }
}
