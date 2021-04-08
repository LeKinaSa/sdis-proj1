package peer.clientCommands;

import java.io.*;

public class BackupCommand extends ClientCommand {
    public final String fileName;
    public final int replicationFactor;

    public BackupCommand(String[] args) {
        super(args);
        assert (args[1].equals("BACKUP"));

        if (args.length != 4) {
            System.out.println("java peer.TestApp <peer_ap> BACKUP <file> <replicationFactor>");
            throw new IllegalArgumentException();
        }

        this.fileName = args[2];
        this.replicationFactor = Integer.parseInt(args[3]);
    }
    
    public void execute() {
        System.out.println("Executing backup...");
        try {
            File file = new File(this.fileName);
            FileInputStream in;
            try {
                in = new FileInputStream(file);
            }
            catch (FileNotFoundException exception) {
                System.out.println("File: " + fileName + " not found");
                return;
            }

            int fileSize = (int) file.length();
            byte[] buffer = new byte[fileSize];
            try {
                in.read(buffer);
                stub.backupFile(this.fileName, buffer, this.replicationFactor);
            }
            catch (IOException exception) {
                System.err.println("Client exception: " + exception.toString());
                exception.printStackTrace();
            }
            try {
                in.close();
            }
            catch (IOException ignored) { }
        }
        catch (Exception exception) {
            System.err.println("Client exception: " + exception.toString());
            exception.printStackTrace();
        }
    }
}
