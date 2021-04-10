package peer.messages;

import peer.Channel;
import peer.ChannelName;
import peer.state.PeerState;

public class BackupReceiverMessage extends Message {
    private final String fileId;
    private final int chunkNo;

    public BackupReceiverMessage(Channel mc, Channel mdb, Channel mdr, String version, int peerId, String fileId, int chunkNo) {
        super(ChannelName.MC, mc, mdb, mdr, version, peerId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    public String toString() {
        return "[peer" + this.messagePeerId + "] v" + this.version + " - Backup " + this.fileId + ":" + this.chunkNo;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> STORED <SenderId> <FileId> <ChunkNo> "
        String message = this.version + " STORED " + this.messagePeerId + " " + this.fileId + " " + this.chunkNo + " ";

        // Get Message Bytes (Header)
        return generateMessageWithoutBody(message);
    }

    @Override
    public Message answer(int id) {
        PeerState.INSTANCE.peerHasChunk(this.fileId, this.chunkNo, this.messagePeerId);
        return null;
    }

    public boolean correspondsTo(String fileId, int chunkNo) {
        return this.fileId.equals(fileId) && (this.chunkNo == chunkNo);
    }
}
