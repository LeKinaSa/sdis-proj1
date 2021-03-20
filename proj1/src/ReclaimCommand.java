public class ReclaimCommand extends ClientCommand {
    private int diskSpace; // Disk Space Used to Store the Chunks (in KByte)
    
    public ReclaimCommand(String[] args) {
        super(args);
        assert (args[1].equals("RECLAIM"));

        if (args.length != 3) {
            System.out.println("java TestApp <peer_ap> RECLAIM <diskSpace>");
            throw new IllegalArgumentException();
        }

        this.diskSpace = Integer.parseInt(args[2]);
    }
}
