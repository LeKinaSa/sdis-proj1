package peer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MessageMaker {
    private static final byte[] CRLF = {0xD, 0xA};
    private String version;
    private int peerId;
    
    public MessageMaker(String version, int peerId) {
        this.version = version;
        this.peerId = peerId;
    }

    private byte[] generateMessageWithBody(String header, byte[] body) {
        ByteArrayOutputStream aux = new ByteArrayOutputStream();
        try {
            aux.write(header.getBytes());
            aux.write(CRLF);
            aux.write(CRLF);
            aux.write(body);
        }
        catch (IOException exception) {
            return null;
        }
        return aux.toByteArray();
    }

    private byte[] generateMessageWithoutBody(String header) {
        ByteArrayOutputStream aux = new ByteArrayOutputStream();
        try {
            aux.write(header.getBytes());
            aux.write(CRLF);
            aux.write(CRLF);
        }
        catch (IOException exception) {
            return null;
        }
        return aux.toByteArray();
    }

    public byte[] backupSender(String fileId, int chunkNo, int replicationDegree, byte[] chunkContent) {
        // Header: "<Version> PUTCHUNK <SenderId> <FileId> <ChunkNo> <ReplicationDef> "
        String message = this.version + " PUTCHUNK " + this.peerId + " " + fileId + " " + chunkNo + " " + replicationDegree + " ";

        // Get Message Bytes (Header + Body)
        byte[] buffer = generateMessageWithBody(message, chunkContent);
        // TODO: buffer might be null
        return buffer;
    }
    
    public byte[] backupReceiver(String fileId, int chunkNo) {
        // Header: "<Version> STORED <SenderId> <FileId> <ChunkNo> "
        String message = this.version + " STORED " + this.peerId + " " + fileId + " " + chunkNo + " ";

        // Get Message Bytes (Header)
        byte[] buffer = generateMessageWithoutBody(message);
        // TODO: buffer might be null
        return buffer;
    }

    public byte[] restoreSender(String fileId, int chunkNo) {
        // Header: "<Version> GETCHUNK <SenderId> <FileId> <ChunkNo> "
        String message = this.version + " GETCHUNK " + this.peerId + " " + fileId + " " + chunkNo + " ";

        // Get Message Bytes (Header)
        byte[] buffer = generateMessageWithoutBody(message);
        // TODO: buffer might be null
        return buffer;
    }

    public byte[] restoreReceiver(String fileId, int chunkNo, byte[] chunkContent) {
        // Header: "<Version> CHUNK <SenderId> <FileId> <ChunkNo> "
        String message = this.version + " CHUNK " + this.peerId + " " + fileId + " " + chunkNo + " ";

        // Get Message Bytes (Header + Body)
        byte[] buffer = generateMessageWithBody(message, chunkContent);
        // TODO: buffer might be null
        return buffer;
    }

    public byte[] deleteSender(String fileId) {
        // Header: "<Version> DELETE <SenderId> <FileId> "
        String message = this.version + " DELETE " + this.peerId + " " + fileId + " ";

        // Get Message Bytes (Header)
        byte[] buffer = generateMessageWithoutBody(message);
        // TODO: buffer might be null
        return buffer;
    }

    public byte[] reclaimSender(String fileId, int chunkNo) {
        // Header: "<Version> REMOVED <SenderId> <FileId> <ChunkNo> "
        String message = this.version + " REMOVED " + this.peerId + " " + fileId + " " + chunkNo + " ";

        // Get Message Bytes (Header)
        byte[] buffer = generateMessageWithoutBody(message);
        // TODO: buffer might be null
        return buffer;
    }
}
