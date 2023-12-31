package peer.clientCommands;

import java.io.FileOutputStream;

public class RestoreCommand extends ClientCommand {
    private final String file;

    public RestoreCommand(String[] args) {
        super(args);
        assert (args[1].equals("RESTORE"));

        if (args.length != 3) {
            System.out.println("java peer.TestApp <peer_ap> RESTORE <file>");
            throw new IllegalArgumentException();
        }

        this.file = args[2];
    }

    public void execute() {
        System.out.println("Executing restore...");
        try {
            byte[] fileBytes = stub.restoreFile(this.fileId(this.file));
            // System.out.println("File Restored: " + new String(fileBytes));

            try (FileOutputStream fos = new FileOutputStream(this.file)) {
                fos.write(fileBytes);
            }

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
