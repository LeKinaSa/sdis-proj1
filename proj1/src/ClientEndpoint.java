public class ClientEndpoint implements ServerCommands {

    public void backupFile(String fileName, byte[] fileContents, int replicationDegree) {
        //
    }

    public byte[] restoreFile(String fileName) {
        return new byte[0];
    }

    public void deleteFile(String fileName) {
        //
    }

    public void reclaimSpace(int space) {
        //
    }

    public String state() {
        return "hi";
    }


    public ClientEndpoint() {
        ////
    }

}
