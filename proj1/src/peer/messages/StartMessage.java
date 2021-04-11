package peer.messages;

import peer.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.Set;

public class StartMessage extends Message {

    public StartMessage(Channel mc, Channel mdb, Channel mdr, String version, int messagePeerId) {
        super(ChannelName.MC, mc, mdb, mdr, version, messagePeerId);
    }

    public String toString() {
        return "[peer" + this.messagePeerId + "] v" + this.version + " - Start";
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> START <SenderId> "
        String message = this.version + " START " + this.messagePeerId + " ";

        // Get Message Bytes (Header)
        return generateMessageWithoutBody(message);
    }

    @Override
    public void answer(int id) {
        // New Thread to deal with the answer
        Thread thread = new Thread(() -> {
            Set<String> files = ClientEndpoint.state.removedFilesFromPeer(this.messagePeerId);
            Message message;
            // Open MC Socket
            MulticastSocket socket;
            try {
                socket = new MulticastSocket(this.mc.port);
            }
            catch (IOException exception) {
                PeerDebugger.println("Error occurred");
                return;
            }
            try {
                socket.joinGroup(this.mc.ip);
            }
            catch (IOException exception) {
                PeerDebugger.println("Error occurred: " + exception.getMessage());
                socket.close();
                return;
            }

            for (String fileId : files) {
                message = new DeleteTargetMessage(this.mc, this.mdb, this.mdr, this.version, id, fileId, this.messagePeerId);

                // Verify if no other peer is faster to send the delete target message
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
                        if ((answer instanceof DeleteTargetMessage) && (((DeleteTargetMessage) answer).correspondsTo(fileId, this.messagePeerId))) {
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
                    for (int n = 0; n < ClientEndpoint.REPETITIONS - 1; n ++) {
                        Utils.pause(Utils.getRandomNumber(0, 401));
                        Utils.sendMessage(message);
                    }
                }
            }
            // Close MDR Socket
            try {
                socket.leaveGroup(this.mdr.ip);
            }
            catch (Exception ignored) { }
            socket.close();
        });
        thread.start();
    }
}
