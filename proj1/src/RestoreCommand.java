public class RestoreCommand extends ClientCommand {
    private String file;

    public RestoreCommand(String[] args) {
        super(args);
        assert (args[1].equals("RESTORE"));

        if (args.length != 3) {
            System.out.println("java TestApp <peer_ap> RESTORE <file>");
            throw new IllegalArgumentException();
        }

        this.file = args[2];
    }
}
