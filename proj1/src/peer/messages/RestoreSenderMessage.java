package peer.messages;

public class RestoreSenderMessage extends Message {
    private String fileId;
    private int chunkNo;

    public RestoreSenderMessage(String version, int peerId, String fileId, int chunkNo) {
        super(version, peerId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> GETCHUNK <SenderId> <FileId> <ChunkNo> "
        String message = this.version + " GETCHUNK " + this.peerId + " " + this.fileId + " " + this.chunkNo + " ";

        // Get Message Bytes (Header)
        byte[] buffer = generateMessageWithoutBody(message);
        // TODO: buffer might be null
        return buffer;

    }

    public String toString() {
        return "[peer" + this.peerId + "]" + this.version + " - Restore initiator " + this.fileId + ":" + this.chunkNo;
    }
}
