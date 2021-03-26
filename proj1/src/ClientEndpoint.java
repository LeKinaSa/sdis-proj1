import java.util.Arrays;

public class ClientEndpoint implements ServerCommands {
    private final int CHUNK_SIZE = 64000;

    public void backupFile(String fileName, byte[] fileContents, int replicationDegree) {
        System.out.println("backupFile()");

        int fileSize = fileContents.length;
        int backedUp = 0;
        int toBackUp;

        while (backedUp < fileSize) {
            toBackUp = Math.min(CHUNK_SIZE, fileSize - backedUp);

            // Backup Chunk [backedUp, backedUp + toBackUp[
            backupChunk(Arrays.copyOfRange(fileContents, backedUp, backedUp + toBackUp), replicationDegree);
            backedUp += toBackUp;
        }

        // File size is a multiple of the chunk size
        if (fileSize % CHUNK_SIZE == 0) {
            // Backup Chunk with size 0
            backupChunk(new byte[0], replicationDegree);
        }
    }

    private void backupChunk(byte[] chunkContent, int replicationDegree) {
        System.out.println("backup chunk with size:" + chunkContent.length);
        // TODO: implement
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


    public ClientEndpoint() {
        ////
    }

}
