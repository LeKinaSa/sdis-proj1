package peer.messages;

public class ReclaimReceiverMessage extends Message {
    private final String fileId;
    private final int chunkNo;

    public ReclaimReceiverMessage(String version, int peerId, String fileId, int chunkNo) {
        super(version, peerId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    public String toString() {
        return "[peer" + this.peerId + "] v" + this.version + " - Reclaim " + this.fileId + ":" + this.chunkNo;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> REMOVED <SenderId> <FileId> <ChunkNo> "
        String message = this.version + " REMOVED " + this.peerId + " " + this.fileId + " " + this.chunkNo + " ";

        // Get Message Bytes (Header)
        return generateMessageWithoutBody(message);
    }

    @Override
    public void answer() {
        // TODO
    }
}
