package peer;

import peer.messages.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ListenerThread extends Thread {
    private static final int MESSAGE_SIZE = 65000;
    // This size tries to guarantee that the full message will be read from the socket

    private final InetAddress ip;
    private final int port;
    
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
            PeerDebugger.println("Error occurred");
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
            // Obtain Packet
            try {
                socket.receive(p);
            }
            catch (IOException exception) {
                continue;
            }

            // Obtain Message contained in Received Packet
            Message message = Message.parse(p);
            if (message == null) {
                String errorMessage = new String(p.getData(), 0, p.getLength());
                PeerDebugger.println("Message received with error: " + errorMessage);
                continue;
            }

            // Execute the Necessary Action based on the Received Message
            PeerDebugger.println("Message received: " + message.toString());
            message.answer();
        }

        // Close Socket
        try {
            socket.leaveGroup(ip);
        }
        catch (Exception ignored) { }
        socket.close();
    }
}
