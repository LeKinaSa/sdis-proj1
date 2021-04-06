package peer;

import java.net.UnknownHostException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.server.UnicastRemoteObject;
        

public class Server {
    private static InetAddress ipMC, ipMDB, ipMDR;
    private static int portMC, portMDB, portMDR;

    private static InetAddress tempIp;
    private static int tempPort;

    public static void main(String[] args) {
        // Args: <protocol version> <peer id> <service access point> <MC> <MDB> <MDR>
        // Parse Args
        if (args.length != 6) {
            System.out.println("java peer.Server <protocol_version> <peer_id> <service_acess_point> <MC> <MDB> <MDR>");
            return;
        }
        String version = args[0];
        int id;
        try {
            id = Integer.parseInt(args[1]);
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
            registry.bind("Hello", stub);

            System.err.println("Server ready");
        }
        catch (Exception exception) {
            System.err.println("Server exception: " + exception.toString());
            exception.printStackTrace();
            return;
        }

        // More services here

        // Control channel service
        getInformationFromArg(args[3]);
        ipMC = tempIp;
        portMC = tempPort;
        obj.setMC(ipMC, portMC);
        
        // Backup service
        getInformationFromArg(args[4]);
        ipMDB = tempIp;
        portMDB = tempPort;
        obj.setMDB(ipMDB, portMDB);

        // Restore service
        getInformationFromArg(args[5]);
        ipMDR = tempIp;
        portMDR = tempPort;
        obj.setMDR(ipMDR, portMDR);
    }

    private static void getInformationFromArg(String arg) {
        String[] list = arg.split(":");
        tempIp = null;
        tempPort = -1;

        try {
            tempIp = InetAddress.getByName(list[0]);
        }
        catch (UnknownHostException ignored) { }
        try {
            tempPort = Integer.parseInt(list[1]);
        }
        catch (NumberFormatException ignored) { }
    }

    public static void receiveMessage(InetAddress ip, int port) {
        MulticastSocket socket;
        try {
            socket = new MulticastSocket(port);
        } catch (IOException exception) {
            return;
        }
        try {
            socket.joinGroup(ip);
        } catch (IOException e) {
            socket.close();
            return;
        }
        // TODO: read message
        try {
            socket.leaveGroup(ip);
        }
        catch (Exception exception) {
            socket.close();
            return;
        }
        socket.close();
    }
}
