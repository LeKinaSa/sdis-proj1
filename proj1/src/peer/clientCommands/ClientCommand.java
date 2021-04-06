package peer.clientCommands;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import peer.ServerCommands;

public abstract class ClientCommand {
    public int accessPoint;
    protected ServerCommands stub;

    ClientCommand(String[] args) {
        this.accessPoint = Integer.parseInt(args[0]);
        try {
        Registry registry = LocateRegistry.getRegistry();
        this.stub = (ServerCommands) registry.lookup(args[0]);
        }
        catch (Exception exception) {
            System.err.println("Client exception: " + exception.toString());
            exception.printStackTrace();
        }
    }

    public abstract void execute();

    public static ClientCommand parseArgs(String[] args) {
        // Read Operation -> Call Correct Constructor
        if (args[1].equals("STATE")) {
            return new StateCommand(args);
        }
        else if (args[1].equals("BACKUP")) {
            return new BackupCommand(args);
        }
        else if (args[1].equals("DELETE")) {
            return new DeleteCommand(args);
        }
        else if (args[1].equals("RESTORE")) {
            return new RestoreCommand(args);
        }
        else if (args[1].equals("RECLAIM")) {
            return new ReclaimCommand(args);
        }
        else {
            return null;
        }
    }
}
