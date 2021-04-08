package peer.messages;

import java.nio.charset.StandardCharsets;

public class RestoreReceiverMessage extends Message {
    private String fileId;
    private int chunkNo;
    private byte[] chunkContent;

    public RestoreReceiverMessage(String version, int peerId, String fileId, int chunkNo, byte[] chunkContent) {
        super(version, peerId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.chunkContent = chunkContent;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> CHUNK <SenderId> <FileId> <ChunkNo> "
        String message = this.version + " CHUNK " + this.peerId + " " + this.fileId + " " + this.chunkNo + " ";

        // Get Message Bytes (Header + Body)
        byte[] buffer = generateMessageWithBody(message, this.chunkContent);
        // TODO: buffer might be null
        return buffer;
    }

    public String toString() {
        String content = new String(chunkContent, StandardCharsets.UTF_8);
        return "[peer" + this.peerId + "]" + this.version + " - Restore " + this.fileId + ":" + this.chunkNo + "::" + content;
    }
}
