package peer.messages;

public class DeleteSenderMessage extends Message {
    private final String fileId;

    public DeleteSenderMessage(String version, int peerId, String fileId) {
        super(version, peerId);
        this.fileId = fileId;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> DELETE <SenderId> <FileId> "
        String message = this.version + " DELETE " + this.peerId + " " + this.fileId + " ";

        // Get Message Bytes (Header)
        return generateMessageWithoutBody(message);
    }

    public String toString() {
        return "[peer" + this.peerId + "] v" + this.version + " - Delete " + this.fileId;
    }
}
