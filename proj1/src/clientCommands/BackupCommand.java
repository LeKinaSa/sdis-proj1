import java.io.*;

public class BackupCommand extends ClientCommand {
    public String fileName;
    public int replicationFactor;

    public BackupCommand(String[] args) {
        super(args);
        assert (args[1].equals("BACKUP"));

        if (args.length != 4) {
            System.out.println("java TestApp <peer_ap> BACKUP <file> <replicationFactor>");
            throw new IllegalArgumentException();
        }

        this.fileName = args[2];
        this.replicationFactor = Integer.parseInt(args[3]);
    }
    
    public void execute() {
        System.out.println("hi");
        try {
            File file = new File(this.fileName);
            FileInputStream in = null;
            try {
                in = new FileInputStream(file);
            }
            catch (FileNotFoundException exception) {
                System.out.println("File: " + fileName + " not found");
                return;
            }

            int fileSize = (int) file.length();
            byte[] buffer = new byte[fileSize];
            int readSize;
            try {
                readSize = in.read(buffer);
                stub.backupFile(this.fileName, buffer, this.replicationFactor);
            }
            catch (IOException exception) {
                // TODO
            }
            try {
                in.close();
            }
            catch (IOException exception) { }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
