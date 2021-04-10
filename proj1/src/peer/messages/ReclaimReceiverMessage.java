package peer.messages;

import peer.Channel;
import peer.ChannelName;
import peer.ClientEndpoint;
import peer.state.PeerState;

public class ReclaimReceiverMessage extends Message {
    private final String fileId;
    private final int chunkNo;

    public ReclaimReceiverMessage(Channel mc, Channel mdb, Channel mdr, String version, int peerId, String fileId, int chunkNo) {
        super(ChannelName.MC, mc, mdb, mdr, version, peerId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    public String toString() {
        return "[peer" + this.messagePeerId + "] v" + this.version + " - Reclaim " + this.fileId + ":" + this.chunkNo;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> REMOVED <SenderId> <FileId> <ChunkNo> "
        String message = this.version + " REMOVED " + this.messagePeerId + " " + this.fileId + " " + this.chunkNo + " ";

        // Get Message Bytes (Header)
        return generateMessageWithoutBody(message);
    }

    @Override
    public Message answer(int id) {
        ClientEndpoint.state.peerRemovedChunk(this.fileId, this.chunkNo, this.messagePeerId);
        // TODO
        return null;
    }
}
