package peer;

import peer.messages.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static int getRandomNumber(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public static void pause(int ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        }
        catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    public static boolean fileExists(int id, String fileId, int chunkNo) {
        // TODO
        return false;
    }

    public static void store(int id, String fileId, int chunkNo, byte[] chunkContent) {
        // TODO
    }

    public static void sendMessage(Message message) {
        InetAddress ip = message.getIp();
        int port = message.getPort();
        byte[] buf = message.assemble();
        DatagramSocket socket;
        try {
            socket = new DatagramSocket();
        }
        catch (SocketException exception) {
            return;
        }
        DatagramPacket packet = new DatagramPacket(buf, buf.length, ip, port);
        try {
            socket.send(packet);
        }
        catch (IOException ignored) { }
        socket.close();
    }
}
