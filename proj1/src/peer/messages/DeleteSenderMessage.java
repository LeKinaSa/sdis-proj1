package peer.messages;

import peer.Channel;
import peer.ChannelName;

public class DeleteSenderMessage extends Message {
    private final String fileId;

    public DeleteSenderMessage(Channel mc, Channel mdb, Channel mdr, String version, int peerId, String fileId) {
        super(ChannelName.MC, mc, mdb, mdr, version, peerId);
        this.fileId = fileId;
    }

    public String toString() {
        return "[peer" + this.peerId + "] v" + this.version + " - Delete " + this.fileId;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> DELETE <SenderId> <FileId> "
        String message = this.version + " DELETE " + this.peerId + " " + this.fileId + " ";

        // Get Message Bytes (Header)
        return generateMessageWithoutBody(message);
    }

    @Override
    public Message answer(int id) {
        // TODO
        return null;
    }
}
