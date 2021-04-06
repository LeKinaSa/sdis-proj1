package peer.clientCommands;

public class StateCommand extends ClientCommand {

    public StateCommand(String args[]) {
        super(args);
        assert (args[1].equals("STATE"));

        if (args.length != 2) {
            System.out.println("java peer.TestApp <peer_ap> STATE");
            throw new IllegalArgumentException();
        }
    }

    public void execute() {
        System.out.println("Executing state...");
        try {
            String response = stub.state();
            System.out.println("response: " + response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
    
}
