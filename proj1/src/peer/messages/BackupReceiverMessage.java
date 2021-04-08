package peer.messages;

public class BackupReceiverMessage extends Message {
    private String fileId;
    private int chunkNo;

    public BackupReceiverMessage(String version, int peerId, String fileId, int chunkNo) {
        super(version, peerId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> STORED <SenderId> <FileId> <ChunkNo> "
        String message = this.version + " STORED " + this.peerId + " " + this.fileId + " " + this.chunkNo + " ";

        // Get Message Bytes (Header)
        byte[] buffer = generateMessageWithoutBody(message);
        // TODO: buffer might be null
        return buffer;
    }
}
