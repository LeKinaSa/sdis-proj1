import java.io.*;

public class ClientEndpoint implements ServerCommands {
    private int CHUNK_SIZE = 1; // TODO: Find this value

    public void backupFile(String fileName, byte[] fileContents, int replicationDegree) {
        System.out.println("backupFile()");
        File file = new File(fileName);
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
        }
        catch (FileNotFoundException exception) {
            System.out.println("File: " + fileName + " not found");
            return;
        }

        int fileSize = (int) file.length();
        
        for (int i = 0; i < fileSize / CHUNK_SIZE; i ++) {
            // Backup Chunk
            // Maybe: while loop since reading from in may cause exception
        }

        // Last chunk (incomplete)
        // If there's no incomplete chunk to backup, backup chunk with 0 size

        try {
            in.close();
        }
        catch (IOException exception) {

        }
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
