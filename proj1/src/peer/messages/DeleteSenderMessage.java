package peer.messages;

import peer.Channel;
import peer.ChannelName;
import peer.Utils;
import peer.state.PeerState;

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
    public Message answer(int id) {
        // New Thread to deal with the answer
        Thread thread = new Thread(() -> {
            // Delete all chunks from this file stored in this peer
            Utils.deleteFile(id, this.fileId);
            // Remove all chunks from this file from the peer state
            PeerState.INSTANCE.removeFile(this.fileId);
        });
        thread.start();
        return null;
    }
}
