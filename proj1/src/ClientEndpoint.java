public class ClientEndpoint implements ServerCommands {
    //TODO: implement these commands
    public void backupFile(String fileName, byte[] fileContents, int replicationDegree) {
        System.out.println("backupFile()");
    }

    public byte[] restoreFile(String fileName) {
        System.out.println("restoreFile()");
        return new byte[0];
    }

    public void deleteFile(String fileName) {
        System.out.println("deleteFile()");

    }

    public void reclaimSpace(int space) {
        System.out.println("reclaimSpace()");

    }

    public String state() {
        System.out.println("state()");
        return "hello";
    }


    public ClientEndpoint() {
        ////
    }

}
