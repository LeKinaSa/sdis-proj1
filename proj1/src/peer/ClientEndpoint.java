package peer;

import peer.messages.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class ClientEndpoint implements ServerCommands { // Peer endpoint that the client reaches out
    private final int CHUNK_SIZE = 64000;
    private InetAddress ipMC, ipMDB, ipMDR;
    private int portMC, portMDB, portMDR;
    private final String version;
    private final int peerId;

    public ClientEndpoint(String version, int id) {
        this.version = version;
        this.peerId = id;
    }

    public void setMC(InetAddress ip, int port) {
        this.ipMC = ip;
        this.portMC = port;
    }

    public void setMDB(InetAddress ip, int port) {
        this.ipMDB = ip;
        this.portMDB = port;
    }

    public void setMDR(InetAddress ip, int port) {
        this.ipMDR = ip;
        this.portMDR = port;
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
        Message messageMaker = new BackupSenderMessage(this.version, this.peerId, fileId, chunkNo, replicationDegree, chunkContent);
        byte[] message = messageMaker.assemble();
        // Send Message
        this.sendMessage(ipMDB, portMDB, message);
    }

    public byte[] restoreFile(String fileName) {
        PeerDebugger.println("restoreFile()");
        //TODO: implement
        return new byte[0];
    }

    public void deleteFile(String fileName) {
        PeerDebugger.println("deleteFile()");

        // Get message
        Message messageMaker = new DeleteSenderMessage(this.version, this.peerId, fileName);
        byte[] message = messageMaker.assemble();
        // Send message
        this.sendMessage(ipMC, portMC, message);
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

    private void sendMessage(InetAddress ip, int port, byte[] buf) {
        DatagramSocket socket;
        try {
            socket = new DatagramSocket();
        }
        catch (SocketException exception) {
            return;
        }
        DatagramPacket packet = new DatagramPacket(buf, buf.length, ip, port);
        try {
            socket.send(packet);
        }
        catch (IOException ignored) { }
        socket.close();
    }
}
