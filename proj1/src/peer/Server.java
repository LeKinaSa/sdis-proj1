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
        if (!getInformationFromArg(args[3])) {
            return;
        }
        Channel mc = new Channel(ip, port);

        // ----- Backup service -----
        if (!getInformationFromArg(args[4])) {
            return;
        }
        Channel mdb = new Channel(ip, port);

        // ----- Restore service -----
        if (!getInformationFromArg(args[5])) {
            return;
        }
        Channel mdr = new Channel(ip, port);

        // --------------- Initiate Threads ---------------
        obj.setChannels(mc, mdb, mdr);
        Thread mcThread  = new ListenerThread(id, ChannelName.MC , mc, mdb, mdr);
        mcThread.start();
        Thread mdbThread = new ListenerThread(id, ChannelName.MDB, mc, mdb, mdr);
        mdbThread.start();
        Thread mdrThread = new ListenerThread(id, ChannelName.MDR, mc, mdb, mdr);
        mdrThread.start();
        // TODO: do we need this thread ??? everything that is sent to this channel is an answer to RestoreSender

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
