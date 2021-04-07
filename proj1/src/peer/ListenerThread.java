package peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ListenerThread extends Thread {
    private static final int MESSAGE_SIZE = 60;

    private InetAddress ip;
    private int port;
    
    public ListenerThread(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void run() {
        System.out.println("Thread listening to " + ip + ":" + port);
        
        // Open Socket
        MulticastSocket socket;
        try {
            socket = new MulticastSocket(port);
        }
        catch (IOException exception) {
            System.out.println("Error ocurred");
            return;
        }
        try {
            socket.joinGroup(ip);
        }
        catch (IOException exception) {
            System.out.println("Error occurred: " + exception.getMessage());
            socket.close();
            return;
        }
        
        // Read messages
        // TODO: define message size
        // TODO: define message
        // TODO: decypher the message and run the correct thing
        byte[] buf = new byte[MESSAGE_SIZE];
        DatagramPacket p = new DatagramPacket(buf, buf.length);
        int x = 1;
        while (x == 1) { // TODO: non infinite loop
            try {
                socket.receive(p);
                String message = new String(p.getData(), 0, p.getLength());
                System.out.println("Message received: " + message);
            }
            catch (IOException ignored) { }
        }

        // Close Socket
        try {
            socket.leaveGroup(ip);
        }
        catch (Exception ignored) { }
        socket.close();
    }
}
