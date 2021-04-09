package peer;

import peer.messages.Message;

import java.io.File;
import java.io.FileOutputStream;
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
        File file = new File("../peer-data/" + id + "/" + fileId + "/" + chunkNo);
        return file.exists();
    }

    public static boolean store(int id, String fileId, int chunkNo, byte[] chunkContent) {
        File directory = new File("../peer-data/" + id + "/" + fileId);
        File file      = new File("../peer-data/" + id + "/" + fileId + "/" + chunkNo);

        boolean fileReady = false;
        if (file.exists()) {
            // File exists
            return true;
        }
        else if (!directory.exists()) {
            // If the directory doesn't exist, create it
            if (file.getParentFile().mkdirs()) {
                try {
                    // Create the file
                    fileReady = file.createNewFile();
                }
                catch (IOException exception) {
                    fileReady = false;
                }
            }
        }
        else if (directory.isDirectory()) {
            try {
                // Create the file
                fileReady = file.createNewFile();
            }
            catch (IOException exception) {
                fileReady = false;
            }
        }

        if (fileReady) {
            // File has been created successfully
            try {
                // Store information inside the folder
                FileOutputStream stream = new FileOutputStream("../peer-data/" + id + "/" + fileId + "/" + chunkNo);
                stream.write(chunkContent);
                stream.close();
                return true; // The information was stored successfully
            }
            catch (IOException ignored) { }
        }

        // If information wasn't stored, make sure there is no file
        file.delete();
        return false;
    }

    public static void deleteFile(int id, String fileId) {
        File file = new File("../peer-data/" + id + "/" + fileId);
        if (file.exists()) {
            // Remove all the files inside the folder
            for (File f : file.listFiles()) {
                f.delete();
            }
            // Remove the entire folder
            file.delete();
        }
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
