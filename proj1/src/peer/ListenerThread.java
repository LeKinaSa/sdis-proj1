package peer;

import peer.messages.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ListenerThread extends Thread {
    private static final int MESSAGE_SIZE = 65000;
    // This size tries to garantee that the full message will be read from the socket

    private InetAddress ip;
    private int port;
    
    public ListenerThread(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void run() {
        PeerDebugger.println("Thread listening to " + ip + ":" + port);
        
        // Open Socket
        MulticastSocket socket;
        try {
            socket = new MulticastSocket(port);
        }
        catch (IOException exception) {
            PeerDebugger.println("Error ocurred");
            return;
        }
        try {
            socket.joinGroup(ip);
        }
        catch (IOException exception) {
            PeerDebugger.println("Error occurred: " + exception.getMessage());
            socket.close();
            return;
        }
        
        // Read messages
        byte[] buf = new byte[MESSAGE_SIZE];
        DatagramPacket p = new DatagramPacket(buf, buf.length);
        int x = 1;
        while (x == 1) { // TODO: non infinite loop
            try {
                socket.receive(p);
                Message message = Message.parse(p);
                PeerDebugger.println("Message received: " + message.toString());
                // TODO: run the correct thing based on the message type
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