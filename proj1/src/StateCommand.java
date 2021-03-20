public class StateCommand extends ClientCommand {

    public StateCommand(String args[]) {
        super(args);
        assert (args[1].equals("STATE"));

        if (args.length != 2) {
            System.out.println("java TestApp <peer_ap> STATE");
            throw new IllegalArgumentException();
        }
    }
    
}
