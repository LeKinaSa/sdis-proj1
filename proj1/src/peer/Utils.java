package peer;

import peer.messages.Message;
import peer.state.PeerState;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Utils {
    private static ScheduledExecutorService autoSave;

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

    public static int indexOf(byte[] array, byte[] matchingSequence) {
        boolean found;
        for (int i = 0; i < array.length - matchingSequence.length + 1; i ++) {
            found = true;
            for (int j = 0; j < matchingSequence.length; j ++) {
                if (array[i + j] != matchingSequence[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
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
                catch (IOException ignored) { }
            }
        }
        else if (directory.isDirectory()) {
            try {
                // Create the file
                fileReady = file.createNewFile();
            }
            catch (IOException ignored) { }
        }

        if (fileReady) {
            // File has been created successfully
            try {
                // Store information inside the file
                try (FileOutputStream stream = new FileOutputStream("../peer-data/" + id + "/" + fileId + "/" + chunkNo)) {
                    stream.write(chunkContent);
                }
                return true; // The information was stored successfully
            }
            catch (IOException ignored) { }
        }

        // If information wasn't stored, make sure there is no file
        file.delete();
        return false;
    }

    public static byte[] load(int id, String fileId, int chunkNo) {
        File file = new File("../peer-data/" + id + "/" + fileId + "/" + chunkNo);
        if (file.exists()) {
            // File was found
            byte[] buf = new byte[Message.CHUNK_SIZE];
            try {
                // Load information from inside the file
                int readSize;
                try (FileInputStream stream = new FileInputStream("../peer-data/" + id + "/" + fileId + "/" + chunkNo)) {
                    readSize = stream.read(buf);
                }
                return Arrays.copyOfRange(buf, 0, readSize);
            }
            catch (IOException ignored) { }
        }
        return null;
    }

    public static void deleteFile(int id, String fileId) {
        File file = new File("../peer-data/" + id + "/" + fileId);
        if (file.exists()) {
            // Remove all the files inside the folder
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            // Remove the entire folder
            file.delete();
        }
    }

    public static void deleteChunk(int id, String fileId, int chunkNo) {
        File file = new File("../peer-data/" + id + "/" + fileId + "/" + chunkNo);
        file.delete();
        File directory = new File("../peer-data/" + id + "/" + fileId);
        if (directory.listFiles() == null) {
            directory.delete();
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

    public static PeerState loadState(int peerId) {
        File file = new File("../peer-data");
        if (!file.exists()) {
            return new PeerState();
        }
        file = new File("../peer-data/" + peerId);
        if (!file.exists()) {
            return new PeerState();
        }
        file = new File("../peer-data/" + peerId + "/state.json");
        if (!file.exists()) {
            return new PeerState();
        }
        try (FileInputStream stream = new FileInputStream("../peer-data/" + peerId + "/state.json")) {
            StringBuilder fileInfo = new StringBuilder();
            int byteRead = stream.read();
            while (byteRead != -1) {
                fileInfo.append(byteRead);
                byteRead = stream.read();
            }
            return PeerState.fromJson(fileInfo.toString());
        }
        catch (IOException exception) {
            return new PeerState();
        }
    }

    public static void saveState(int peerId) {
        File directory = new File("../peer-data/" + peerId);
        File file      = new File("../peer-data/" + peerId + "/state.json");

        if ((!directory.exists()) || (!directory.isDirectory())) {
            file.getParentFile().mkdirs();
        }
        try {
            file.createNewFile();
            try (FileOutputStream stream = new FileOutputStream("../peer-data/" + peerId + "/state.json")) {
                PrintWriter writer = new PrintWriter(stream, true);
                writer.println(ClientEndpoint.state.toJson());
            }
        }
        catch (IOException ignored) { }
    }

    public static void scheduleSave(int peerId) {
        final int delay = 20;
        if ((autoSave != null) && (!autoSave.isShutdown())) {
            autoSave.shutdownNow();
        }
        autoSave = Executors.newSingleThreadScheduledExecutor();
        autoSave.scheduleAtFixedRate(() -> Utils.saveState(peerId), 0, delay, TimeUnit.SECONDS);
    }
}
