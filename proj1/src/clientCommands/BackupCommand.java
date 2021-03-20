public class BackupCommand extends ClientCommand {
    public String file;
    public int replicationFactor;

    public BackupCommand(String[] args) {
        super(args);
        assert (args[1].equals("BACKUP"));

        if (args.length != 4) {
            System.out.println("java TestApp <peer_ap> BACKUP <file> <replicationFactor>");
            throw new IllegalArgumentException();
        }

        this.file = args[2];
        this.replicationFactor = Integer.parseInt(args[3]);
    }
   
}
