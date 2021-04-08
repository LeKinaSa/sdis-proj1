package peer.messages;

import java.nio.charset.StandardCharsets;

public class BackupSenderMessage extends Message {
    private String fileId;
    private int chunkNo;
    private int replicationDegree;
    private byte[] chunkContent;

    public BackupSenderMessage(String version, int peerId, String fileId, int chunkNo, int replicationDegree, byte[] chunkContent) {
        super(version, peerId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.chunkContent = chunkContent;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> PUTCHUNK <SenderId> <FileId> <ChunkNo> <ReplicationDegree> "
        String message = this.version + " PUTCHUNK " + this.peerId + " " + this.fileId + " " + this.chunkNo + " " + this.replicationDegree + " ";

        // Get Message Bytes (Header + Body)
        byte[] buffer = generateMessageWithBody(message, this.chunkContent);
        // TODO: buffer might be null
        return buffer;
    }

    public String toString() {
        String content = new String(chunkContent, StandardCharsets.UTF_8);
        return "[peer" + this.peerId + "]" + this.version + " - Backup initiator " + this.fileId + ":" + this.chunkNo + " x" + replicationDegree + "::" + content;
    }
}