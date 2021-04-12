package peer.clientCommands;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import peer.ServerCommands;

public abstract class ClientCommand {
    public String accessPoint;
    protected ServerCommands stub;

    ClientCommand(String[] args) {
        this.accessPoint = args[0];
        try {
            Registry registry = LocateRegistry.getRegistry();
            this.stub = (ServerCommands) registry.lookup(this.accessPoint);
        }
        catch (Exception exception) {
            System.err.println("Client exception: " + exception.toString());
            exception.printStackTrace();
        }
    }

    public abstract void execute();

    public static ClientCommand parseArgs(String[] args) {
        // Read Operation -> Call Correct Constructor
        switch (args[1]) {
            case "STATE":
                return new StateCommand(args);
            case "BACKUP":
                return new BackupCommand(args);
            case "DELETE":
                return new DeleteCommand(args);
            case "RESTORE":
                return new RestoreCommand(args);
            case "RECLAIM":
                return new ReclaimCommand(args);
            default:
                return null;
        }
    }

    public static String fileId(String path) {
        // create object of Path
        Path fullPath = Paths.get(path);
  
        String fileName = fullPath.getFileName().toString();
  
        return String.valueOf(fileName.hashCode());
    }
}
