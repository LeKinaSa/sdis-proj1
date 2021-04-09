package peer;

import peer.messages.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ListenerThread extends Thread {
    private static final int MESSAGE_SIZE = 65000;
    // This size tries to guarantee that the full message will be read from the socket

    private int id;
    private ChannelName name;
    private Channel mc, mdb, mdr;
    
    public ListenerThread(int id, ChannelName name, Channel mc, Channel mdb, Channel mdr) {
        this.id = id;
        this.name = name;
        this.mc = mc;
        this.mdb = mdb;
        this.mdr = mdr;
    }

    public void run() {
        PeerDebugger.println("Thread listening to " + this.getIp() + ":" + this.getPort());
        
        // Open Socket
        MulticastSocket socket;
        try {
            socket = new MulticastSocket(this.getPort());
        }
        catch (IOException exception) {
            PeerDebugger.println("Error occurred");
            return;
        }
        try {
            socket.joinGroup(this.getIp());
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
            Message message = Message.parse(this.mc, this.mdb, this.mdr, p);
            if (message == null) {
                String errorMessage = new String(p.getData(), 0, p.getLength());
                PeerDebugger.println("Message received with error: " + errorMessage);
                continue;
            }

            // Execute the Necessary Action based on the Received Message
            PeerDebugger.println("Message received: " + message.toString());
            message.answer(this.id);
            // TODO: Problems: (backup) this thread is based on mdb channel and answer needs to go to mc channel
        }

        // Close Socket
        try {
            socket.leaveGroup(this.getIp());
        }
        catch (Exception ignored) { }
        socket.close();
    }

    private InetAddress getIp() {
        switch (this.name) {
            case MC:
                return mc.ip;
            case MDB:
                return mdb.ip;
            case MDR:
                return mdr.ip;
            default:
                return null;
        }
    }

    private int getPort() {
        switch (this.name) {
            case MC:
                return mc.port;
            case MDB:
                return mdb.port;
            case MDR:
                return mdr.port;
            default:
                return 0;
        }
    }
}
