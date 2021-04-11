package peer.messages;

import peer.Channel;
import peer.ChannelName;
import peer.ClientEndpoint;
import peer.Utils;

public class DeleteTargetMessage extends Message {
    private final String fileId;
    private final int targetPeerId;

    public DeleteTargetMessage(Channel mc, Channel mdb, Channel mdr, String version, int messagePeerId, String fileId, int targetPeerId) {
        super(ChannelName.MC, mc, mdb, mdr, version, messagePeerId);
        this.fileId = fileId;
        this.targetPeerId = targetPeerId;
    }

    public String toString() {
        return "[peer" + this.messagePeerId + "] v" + this.version + " - Delete Target " + this.targetPeerId + " " + this.fileId;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> REMOVE <SenderId> <FileId> <TargetId> "
        String message = this.version + " REMOVE " + this.messagePeerId + " " + this.fileId + " " + this.targetPeerId + " ";

        // Get Message Bytes (Header)
        return generateMessageWithoutBody(message);
    }

    @Override
    public void answer(int id) {
        if (this.targetPeerId == id) {
            // Delete all chunks from this file stored in this peer
            Utils.deleteFile(id, this.fileId);
            // Remove all chunks from this file from the peer state
            ClientEndpoint.state.removeFile(this.fileId);

            // Message from this peer (id)
            Message message = new DeleteReceiverMessage(this.mc, this.mdb, this.mdr, this.version, id, this.fileId);
            // Send Message
            Utils.sendMessage(message);
        }
    }
}
