package peer.clientCommands;

public class ReclaimCommand extends ClientCommand {
    private final int diskSpace; // Disk Space Used to Store the Chunks (in KByte)
    
    public ReclaimCommand(String[] args) {
        super(args);
        assert (args[1].equals("RECLAIM"));

        if (args.length != 3) {
            System.out.println("java peer.TestApp <peer_ap> RECLAIM <diskSpace>");
            throw new IllegalArgumentException();
        }

        this.diskSpace = Integer.parseInt(args[2]);
    }

    public void execute() {
        System.out.println("Executing reclaim...");
        try {
            stub.reclaimSpace(this.diskSpace);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
