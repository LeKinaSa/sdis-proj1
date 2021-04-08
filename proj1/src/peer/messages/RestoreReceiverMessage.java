package peer.messages;

import java.nio.charset.StandardCharsets;

public class RestoreReceiverMessage extends Message {
    private final String fileId;
    private final int chunkNo;
    private final byte[] chunkContent;

    public RestoreReceiverMessage(String version, int peerId, String fileId, int chunkNo, byte[] chunkContent) {
        super(version, peerId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.chunkContent = chunkContent;
    }

    public String toString() {
        String content = new String(chunkContent, StandardCharsets.UTF_8);
        return "[peer" + this.peerId + "] v" + this.version + " - Restore " + this.fileId + ":" + this.chunkNo + "::" + content;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> CHUNK <SenderId> <FileId> <ChunkNo> "
        String message = this.version + " CHUNK " + this.peerId + " " + this.fileId + " " + this.chunkNo + " ";

        // Get Message Bytes (Header + Body)
        return generateMessageWithBody(message, this.chunkContent);
    }

    @Override
    public byte[] answer(int id) {
        // TODO
        return null;
    }
}
