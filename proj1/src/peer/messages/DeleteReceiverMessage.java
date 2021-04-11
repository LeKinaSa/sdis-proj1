package peer.messages;

import peer.Channel;
import peer.ChannelName;
import peer.ClientEndpoint;

public class DeleteReceiverMessage extends Message {
    private final String fileId;

    public DeleteReceiverMessage(Channel mc, Channel mdb, Channel mdr, String version, int peerId, String fileId) {
        super(ChannelName.MC, mc, mdb, mdr, version, peerId);
        this.fileId = fileId;
    }

    public String toString() {
        return "[peer" + this.messagePeerId + "] v" + this.version + " - Deleted " + this.fileId;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> DELETED <SenderId> <FileId> "
        String message = this.version + " DELETED " + this.messagePeerId + " " + this.fileId + " ";

        // Get Message Bytes (Header)
        return generateMessageWithoutBody(message);
    }

    @Override
    public void answer(int id) {
        ClientEndpoint.state.fileRemovedFromPeer(this.fileId, id);
    }
}
