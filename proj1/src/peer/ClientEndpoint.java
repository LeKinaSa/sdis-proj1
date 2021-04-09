package peer;

import peer.messages.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Arrays;

public class ClientEndpoint implements ServerCommands { // Peer endpoint that the client reaches out
    private final int CHUNK_SIZE = 64000;
    private final int REPETITIONS = 5;
    private Channel mc, mdb, mdr;
    private final String version;
    private final int peerId;

    public ClientEndpoint(String version, int id) {
        this.version = version;
        this.peerId = id;
    }

    public void setChannels(Channel mc, Channel mdb, Channel mdr) {
        this.mc = mc;
        this.mdb = mdb;
        this.mdr = mdr;
    }

    public void backupFile(String fileName, byte[] fileContents, int replicationDegree) {
        PeerDebugger.println("backupFile()");

        int fileSize = fileContents.length;
        int backedUp = 0;
        int toBackUp;
        int chunk = 0;

        while (backedUp < fileSize) {
            toBackUp = Math.min(CHUNK_SIZE, fileSize - backedUp);

            // Backup Chunk [backedUp, backedUp + toBackUp[
            backupChunk(Arrays.copyOfRange(fileContents, backedUp, backedUp + toBackUp), replicationDegree, fileName, chunk);
            backedUp += toBackUp;
            chunk += 1;
        }

        // File size is a multiple of the chunk size
        if (fileSize % CHUNK_SIZE == 0) {
            // Backup Chunk with size 0
            backupChunk(new byte[0], replicationDegree, fileName, chunk);
        }
    }

    private void backupChunk(byte[] chunkContent, int replicationDegree, String fileId, int chunkNo) {
        PeerDebugger.println("backup chunk with size:" + chunkContent.length);

        // Get Message
        Message message = new BackupSenderMessage(this.mc, this.mdb, this.mdr, this.version, this.peerId, fileId, chunkNo, replicationDegree, chunkContent);

        // Open MC Socket
        MulticastSocket socket;
        try {
            socket = new MulticastSocket(mc.port);
        }
        catch (IOException exception) {
            PeerDebugger.println("Error occurred");
            return;
        }
        try {
            socket.joinGroup(mc.ip);
        }
        catch (IOException exception) {
            PeerDebugger.println("Error occurred: " + exception.getMessage());
            socket.close();
            return;
        }

        // Read Answers from MC channel
        int timeInterval = 1000; // 1 second
        int answers;
        byte[] buf = new byte[Message.MESSAGE_SIZE];
        DatagramPacket p = new DatagramPacket(buf, buf.length);
        for (int n = 0; n < this.REPETITIONS; n ++) {
            // Send Message
            Utils.sendMessage(message);
            // Obtain answers during timeInterval
            long initial_timestamp = System.currentTimeMillis();
            long current_timestamp = System.currentTimeMillis();
            answers = 0;
            while (current_timestamp < initial_timestamp + timeInterval) {
                try {
                    socket.receive(p);
                    Message answer = Message.parse(mc, mdb, mdr, p);
                    if (answer instanceof BackupReceiverMessage) {
                        answers ++;
                    }
                }
                catch (IOException ignored) { }
                current_timestamp = System.currentTimeMillis();
            }

            if (answers >= replicationDegree) {
                break;
            }
            timeInterval = timeInterval * 2;
        }
        // Close Socket
        try {
            socket.leaveGroup(mc.ip);
        }
        catch (Exception ignored) { }
        socket.close();
    }

    public byte[] restoreFile(String fileName) {
        PeerDebugger.println("restoreFile()");
        //TODO: implement
        return new byte[0];
    }

    public void deleteFile(String fileName) {
        PeerDebugger.println("deleteFile()");

        // Get message
        Message message = new DeleteSenderMessage(this.mc, this.mdb, this.mdr, this.version, this.peerId, fileName);
        // Send message
        Utils.sendMessage(message);
        for (int n = 0; n < this.REPETITIONS - 1; n ++) {
            Utils.pause(Utils.getRandomNumber(0, 401));
            Utils.sendMessage(message);
        }
    }

    public void reclaimSpace(int space) {
        PeerDebugger.println("reclaimSpace()");
        //TODO: implement
    }

    public String state() {
        PeerDebugger.println("state()");
        //TODO: implement
        return "hello";
    }
}
