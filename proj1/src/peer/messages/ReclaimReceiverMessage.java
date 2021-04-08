package peer.messages;

public class ReclaimReceiverMessage extends Message {
    private String fileId;
    private int chunkNo;

    public ReclaimReceiverMessage(String version, int peerId, String fileId, int chunkNo) {
        super(version, peerId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> REMOVED <SenderId> <FileId> <ChunkNo> "
        String message = this.version + " REMOVED " + this.peerId + " " + this.fileId + " " + this.chunkNo + " ";

        // Get Message Bytes (Header)
        byte[] buffer = generateMessageWithoutBody(message);
        // TODO: buffer might be null
        return buffer;
    }

    public String toString() {
        return "[peer" + this.peerId + "]" + this.version + " - Reclaim " + this.fileId + ":" + this.chunkNo;
    }
}