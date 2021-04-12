package peer;

import peer.messages.*;
import peer.state.PeerState;

import java.io.*;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ClientEndpoint implements ServerCommands { // Peer endpoint that the client reaches out
    public static PeerState state;
    public static final int REPETITIONS = 5;

    private Channel mc, mdb, mdr;
    private final String version;
    private final int peerId;

    // TODO: check fileName (if this file is in a directory, should it be in a directory once it is inside the peer-data folder? -> probably not)

    public ClientEndpoint(String version, int id) {
        ClientEndpoint.state = Utils.loadState(id);
        Utils.scheduleSave(id);
        this.version = version;
        this.peerId = id;
    }

    public void setChannels(Channel mc, Channel mdb, Channel mdr) {
        this.mc = mc;
        this.mdb = mdb;
        this.mdr = mdr;
    }

    public Channel getMC() {
        return this.mc;
    }

    public Channel getMDB() {
        return this.mdb;
    }

    public Channel getMDR() {
        return this.mdr;
    }

    public String getVersion() {
        return this.version;
    }

    public int getId() {
        return this.peerId;
    }

    public void backupFile(String fileName, byte[] fileContents, int replicationDegree) {
        PeerDebugger.println("backupFile()");

        int fileSize = fileContents.length;
        int backedUp = 0;
        int toBackUp;
        int chunk = 0;

        String fileId = fileName; // TODO: get fileId from fileName
        ClientEndpoint.state.insertFile(fileName, fileId, replicationDegree);
        while (backedUp < fileSize) {
            toBackUp = Math.min(Message.CHUNK_SIZE, fileSize - backedUp);

            // Backup Chunk [backedUp, backedUp + toBackUp[
            backupChunk(Arrays.copyOfRange(fileContents, backedUp, backedUp + toBackUp), replicationDegree, fileId, chunk);
            backedUp += toBackUp;
            chunk += 1;
        }

        // File size is a multiple of the chunk size
        if (fileSize % Message.CHUNK_SIZE == 0) {
            // Backup Chunk with size 0
            backupChunk(new byte[0], replicationDegree, fileId, chunk);
        }
    }

    private void backupChunk(byte[] chunkContent, int replicationDegree, String fileId, int chunkNo) {
        PeerDebugger.println("backup chunk with size:" + chunkContent.length);

        // Get Message
        Message message = new BackupSenderMessage(this.mc, this.mdb, this.mdr, this.version, this.peerId, fileId, chunkNo, replicationDegree, chunkContent);
        Set<Integer> previousAnswers = new HashSet<>();
        Set<Integer> answers;

        // Read Answers from MC channel (on the MC thread)
        int timeInterval = 1000; // 1 second
        for (int n = 0; n < REPETITIONS; n ++) {
            ClientEndpoint.state.chunkClearReplication(fileId, chunkNo);
            // Send Message
            Utils.sendMessage(message);
            // Obtain answers during timeInterval (not here)
            Utils.pause(timeInterval);
            // If replication degree is enough, stop
            if (ClientEndpoint.state.chunkWithSufficientReplication(fileId, chunkNo)) {
                return;
            }
            if (!this.version.equals("1.0")) {
                answers = ClientEndpoint.state.chunkGetReplication(fileId, chunkNo);
                if (answers != null) {
                    // If the answers repeat, then it is likely that there is no more peers or no more peers with space
                    if (answers.equals(previousAnswers)) {
                        return;
                    }
                    previousAnswers = answers;
                }
            }
            timeInterval = timeInterval * 2;
        }
    }

    public byte[] restoreFile(String fileName) {
        PeerDebugger.println("restoreFile()");

        if (!this.version.equals("1.0")) {
            byte[] file = this.restoreFileEnhanced(fileName);
            if (file != null) {
                return file;
            }
        }
        return this.restoreFileRegular(fileName);
    }

    public byte[] restoreFileRegular(String fileName) {
        // TODO: introduce timeout? so it doesn't get stuck here forever
        // aux will be holding the file data
        ByteArrayOutputStream aux = new ByteArrayOutputStream();

        // Open MDR Channel
        MulticastSocket socket;
        try {
            socket = new MulticastSocket(mdr.port);
        }
        catch (IOException exception) {
            PeerDebugger.println("Error occurred");
            return null;
        }
        try {
            socket.joinGroup(mdr.ip);
        }
        catch (IOException exception) {
            PeerDebugger.println("Error occurred: " + exception.getMessage());
            socket.close();
            return null;
        }

        int chunk = 0;
        int chunkSize = Message.CHUNK_SIZE;
        byte[] buf = new byte[Message.MESSAGE_SIZE];
        DatagramPacket p = new DatagramPacket(buf, buf.length);
        while (chunkSize >= Message.CHUNK_SIZE) {
            // Get message
            Message message = new RestoreSenderMessage(this.mc, this.mdb, this.mdr, this.version, this.peerId, fileName, chunk);
            // Send message
            Utils.sendMessage(message);

            // Receive answer
            try {
                socket.receive(p);
            }
            catch (IOException exception) {
                continue;
            }

            // Verify if the message obtained is the one we need
            Message answer = Message.parse(this.mc, this.mdb, this.mdr, p);
            if (answer instanceof RestoreReceiverMessage) {
                RestoreReceiverMessage restoreAnswer = (RestoreReceiverMessage) answer;
                // Verify if the chunk is the one we need
                if (restoreAnswer.correspondsTo(fileName, chunk)) {
                    // Write chunk to aux
                    try {
                        aux.write(restoreAnswer.getChunk());
                    } catch (IOException exception) {
                        continue;
                    }
                    // Check chunk size
                    chunkSize = restoreAnswer.getChunk().length;
                }
            }
            // Chunk received and stored -> continue to next chunk
            chunk ++;
        }
        return aux.toByteArray();
    }

    public byte[] restoreFileEnhanced(String fileName) {
        // aux will be holding the file data
        ByteArrayOutputStream aux = new ByteArrayOutputStream();

        int chunk = 0;
        int chunkSize = Message.CHUNK_SIZE;
        byte[] buf = new byte[Message.MESSAGE_SIZE];
        int readSize;
        // TODO: verify if the next calls blocks in the case that the other peer isn't enhanced
        // TODO: verify that only 1 peer connects to this socket
        try (Socket socket = new Socket(this.mdr.ip, this.mdr.port)) {
            InputStream input = socket.getInputStream();
            while (chunkSize >= Message.CHUNK_SIZE) {
                // Get message
                Message message = new RestoreSenderMessage(this.mc, this.mdb, this.mdr, this.version, this.peerId, fileName, chunk);
                // Send message
                Utils.sendMessage(message);
                // TODO: review this Part (if UDP message is lost, the next call will block)

                // Receive answer
                readSize = input.read(buf, 0, buf.length);
                if (readSize < 0) {
                    // TODO: what should i do in this situation (?)
                    continue; // TODO: could this be because the UDP failed to deliver?
                }

                // Verify if the message obtained is the one we need
                Message answer = Message.parse(this.mc, this.mdb, this.mdr, new DatagramPacket(buf, readSize));
                if (answer instanceof RestoreReceiverMessage) {
                    RestoreReceiverMessage restoreAnswer = (RestoreReceiverMessage) answer;
                    // Verify if the chunk is the one we need
                    if (restoreAnswer.correspondsTo(fileName, chunk)) {
                        // Write chunk to aux
                        try {
                            aux.write(restoreAnswer.getChunk());
                        } catch (IOException exception) {
                            continue;
                        }
                        // Check chunk size
                        chunkSize = restoreAnswer.getChunk().length;
                    }
                }

                // Chunk received and stored -> continue to next chunk
                chunk ++;
            }
        }
        catch (IOException exception) {
            return null;
        }

        return aux.toByteArray();
    }

    public void deleteFile(String fileName) {
        PeerDebugger.println("deleteFile()");

        // Get message
        Message message = new DeleteSenderMessage(this.mc, this.mdb, this.mdr, this.version, this.peerId, fileName);
        // Send message
        Utils.sendMessage(message);
        for (int n = 0; n < REPETITIONS - 1; n ++) {
            Utils.pause(Utils.getRandomNumber(0, 401));
            Utils.sendMessage(message);
        }
    }

    public void reclaimSpace(int space) {
        PeerDebugger.println("reclaimSpace()");
        // space is in KBytes and the capacity in peer state is in Bytes
        ClientEndpoint.state.readjustCapacity(this, space * 1000);
    }

    public String state() {
        PeerDebugger.println("state()");
        return ClientEndpoint.state.toString();
    }
}
