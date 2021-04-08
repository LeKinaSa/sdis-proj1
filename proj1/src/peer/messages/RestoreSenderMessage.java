package peer.messages;

public class RestoreSenderMessage extends Message {
    private final String fileId;
    private final int chunkNo;

    public RestoreSenderMessage(String version, int peerId, String fileId, int chunkNo) {
        super(version, peerId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    public String toString() {
        return "[peer" + this.peerId + "] v" + this.version + " - Restore initiator " + this.fileId + ":" + this.chunkNo;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> GETCHUNK <SenderId> <FileId> <ChunkNo> "
        String message = this.version + " GETCHUNK " + this.peerId + " " + this.fileId + " " + this.chunkNo + " ";

        // Get Message Bytes (Header)
        return generateMessageWithoutBody(message);
    }

    @Override
    public byte[] answer(int id) {
        // TODO
        return null;
    }
}
