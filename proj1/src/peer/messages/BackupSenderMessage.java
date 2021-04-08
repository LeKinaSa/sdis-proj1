package peer.messages;

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
}
