package peer.messages;

import peer.Channel;
import peer.ChannelName;
import peer.ClientEndpoint;
import peer.Utils;

public class DeleteSenderMessage extends Message {
    private final String fileId;

    public DeleteSenderMessage(Channel mc, Channel mdb, Channel mdr, String version, int peerId, String fileId) {
        super(ChannelName.MC, mc, mdb, mdr, version, peerId);
        this.fileId = fileId;
    }

    public String toString() {
        return "[peer" + this.messagePeerId + "] v" + this.version + " - Delete " + this.fileId;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> DELETE <SenderId> <FileId> "
        String message = this.version + " DELETE " + this.messagePeerId + " " + this.fileId + " ";

        // Get Message Bytes (Header)
        return generateMessageWithoutBody(message);
    }

    @Override
    public void answer(int id) {
        // New Thread to deal with the answer
        Thread thread = new Thread(() -> {
            // Verify for the enhancement
            this.enhancement(id);
            // Delete all chunks from this file stored in this peer
            Utils.deleteFile(id, this.fileId);
            // Remove all chunks from this file from the peer state
            ClientEndpoint.state.removeFile(this.fileId);
        });
        thread.start();
    }

    private void enhancement(int id) {
        if (this.version.equals("1.0")) {
            return;
        }

        // Implement enhancement
        ClientEndpoint.state.fileIsBeingRemoved(this.fileId);

        // Message from this peer (id)
        Message message = new DeleteReceiverMessage(this.mc, this.mdb, this.mdr, this.version, id, this.fileId);

        // Send Message
        Utils.sendMessage(message);
    }
}
