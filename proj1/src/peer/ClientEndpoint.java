package peer;

import java.util.Arrays;

public class ClientEndpoint implements ServerCommands { // Peer endpoint that the client reaches out
    private static final int CHUNK_SIZE = 64000;
    private MessageMaker messageMaker;
    private String mc, mdb, mdr;

    public ClientEndpoint() {
        this.messageMaker = new MessageMaker("1.0", 1);
        this.mc = ""; // Multicast Control Channel
        this.mdb = ""; // Multicast Data Backup Channel
        this.mdr = ""; // Multicast Data Restore Channel
    }

    public void backupFile(String fileName, byte[] fileContents, int replicationDegree) {
        System.out.println("backupFile()");

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
        System.out.println("backup chunk with size:" + chunkContent.length);
        // TODO: implement

        // Get Message
        byte[] message = this.messageMaker.backupSender(fileId, chunkNo, replicationDegree, chunkContent);
        // Send Message
        //canal certo -> mandar mensagem
    }

    public byte[] restoreFile(String fileName) {
        System.out.println("restoreFile()");
        //TODO: implement
        return new byte[0];
    }

    public void deleteFile(String fileName) {
        System.out.println("deleteFile()");
        //TODO: implement
    }

    public void reclaimSpace(int space) {
        System.out.println("reclaimSpace()");
        //TODO: implement
    }

    public String state() {
        System.out.println("state()");
        //TODO: implement
        return "hello";
    }

}
