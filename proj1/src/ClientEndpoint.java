import java.io.*;

public class ClientEndpoint implements ServerCommands {
    private final int CHUNK_SIZE = 64000;

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
        int bytesRead = 0;
        int readSize;
        while (bytesRead < fileSize) {
            readSize = Math.min(CHUNK_SIZE, fileSize - bytesRead);
            byte[] buffer = new byte[readSize];
            try {
                readSize = in.read(buffer);
            }
            catch (IOException exception) {
                continue;
            }
            bytesRead += readSize;

            // Backup Chunk in buffer
            // TODO
        }

        // Last chunk needs to be incomplete
        if (fileSize % CHUNK_SIZE == 0) {
            // Backup Chunk with size 0
            // TODO
        }

        try {
            in.close();
        }
        catch (IOException exception) { }
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
