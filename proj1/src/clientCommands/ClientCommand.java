public class ClientCommand {
    public int accessPoint;
    ClientCommand(String[] args) {
        this.accessPoint = Integer.parseInt(args[0]);
    }

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
