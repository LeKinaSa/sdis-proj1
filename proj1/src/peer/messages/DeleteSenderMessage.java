package peer.messages;

public class DeleteSenderMessage extends Message {
    private String fileId;

    public DeleteSenderMessage(String version, int peerId, String fileId) {
        super(version, peerId);
        this.fileId = fileId;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> DELETE <SenderId> <FileId> "
        String message = this.version + " DELETE " + this.peerId + " " + this.fileId + " ";

        // Get Message Bytes (Header)
        byte[] buffer = generateMessageWithoutBody(message);
        // TODO: buffer might be null
        return buffer;
    }
}
