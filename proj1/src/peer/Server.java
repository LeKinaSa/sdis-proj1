package peer;

import java.net.UnknownHostException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.InetAddress;
import java.rmi.server.UnicastRemoteObject;
        

public class Server {
    private static InetAddress ip;
    private static int port;

    public static void main(String[] args) {
        // Args: <protocol version> <peer id> <service access point> <MC> <MDB> <MDR>
        // Parse Args
        if (args.length != 6) {
            System.out.println("java peer.Server <protocol_version> <peer_id> <service_access_point> <MC> <MDB> <MDR>");
            return;
        }
        String version = args[0];
        int id;
        try {
            id = Integer.parseInt(args[1]);
            PeerDebugger.setId(id);
        }
        catch (NumberFormatException exception) {
            System.out.println("<peer_id> is int");
            return;
        }
        String accessPoint = args[2];

        // Start ClientEndpoint
        ClientEndpoint obj;
        try {
            obj = new ClientEndpoint(version, id);
            ServerCommands stub = (ServerCommands) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(accessPoint, stub);
        }
        catch (Exception exception) {
            System.err.println("Server exception: " + exception.toString());
            exception.printStackTrace();
            return;
        }

        // --------------- More services here ---------------
        PeerDebugger.println("Starting services");

        // ----- Control channel service -----
        // Parse
        if (!getInformationFromArg(args[3])) {
            return;
        }
        obj.setMC(ip, port);
        // Open
        PeerDebugger.println("Starting first thread");
        Thread mcThread = new ListenerThread(id, ip, port);
        mcThread.start();
        
        // ----- Backup service -----
        // Parse
        if (!getInformationFromArg(args[4])) {
            return;
        }
        obj.setMDB(ip, port);
        // Open
        Thread mdbThread = new ListenerThread(id, ip, port);
        mdbThread.start();

        // ----- Restore service -----
        // Parse
        if (!getInformationFromArg(args[5])) {
            return;
        }
        obj.setMDR(ip, port);
        // Open
        Thread mdrThread = new ListenerThread(id, ip, port);
        mdrThread.start();

        // --------------- Server is Ready ---------------
        PeerDebugger.println("Server ready");
    }

    private static boolean getInformationFromArg(String arg) {
        String[] list = arg.split(":");
        if (list.length != 2) {
            return false;
        }

        try {
            ip = InetAddress.getByName(list[0]);
        }
        catch (UnknownHostException exception) {
            return false;
        }
        try {
            port = Integer.parseInt(list[1]);
        }
        catch (NumberFormatException exception) {
            return false;
        }
        return true;
    }
}
