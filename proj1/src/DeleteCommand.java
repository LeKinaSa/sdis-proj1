public class DeleteCommand extends ClientCommand {
    private String file;

    public DeleteCommand(String[] args) {
        super(args);
        assert (args[1].equals("DELETE"));
        
        if (args.length != 3) {
            System.out.println("java TestApp <peer_ap> DELETE <file>");
            throw new IllegalArgumentException();
        }
        
        this.file = args[2];
    }
}
