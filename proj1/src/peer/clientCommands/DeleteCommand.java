package peer.clientCommands;

public class DeleteCommand extends ClientCommand {
    private final String file;

    public DeleteCommand(String[] args) {
        super(args);
        assert (args[1].equals("DELETE"));
        
        if (args.length != 3) {
            System.out.println("java peer.TestApp <peer_ap> DELETE <file>");
            throw new IllegalArgumentException();
        }
        
        this.file = args[2];
    }

    public void execute() {
        System.out.println("Executing delete...");
        try {
            stub.deleteFile(this.file);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
