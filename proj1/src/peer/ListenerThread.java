package peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ListenerThread extends Thread {
    InetAddress ip;
    int port;

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
        } catch (IOException exception) {
            // TODO
            return;
        }
        try {
            socket.joinGroup(ip);
        } catch (IOException e) {
            socket.close();
            // TODO
            return;
        }
        
        // Read messages
        // TODO: define message size
        // TODO: define message
        // TODO: decypher the message and run the correct thing
        byte[] buf = new byte[8];
        DatagramPacket p = new DatagramPacket(buf, buf.length);
        int x = 1;
        while (x == 1) { // TODO: non infinite loop
            try {
                socket.receive(p);
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
