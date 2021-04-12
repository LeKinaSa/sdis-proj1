package peer.messages;

import peer.Channel;
import peer.ChannelName;

public class RestoreReceiverMessage extends Message {
    private final String fileId;
    private final int chunkNo;
    private final byte[] chunkContent;

    public RestoreReceiverMessage(Channel mc, Channel mdb, Channel mdr, String version, int peerId, String fileId, int chunkNo, byte[] chunkContent) {
        super(ChannelName.MDR, mc, mdb, mdr, version, peerId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.chunkContent = chunkContent;
    }

    public String toString() {
        return "[peer" + this.messagePeerId + "] v" + this.version + " - Restore " + this.fileId + ":" + this.chunkNo + ":: chunk size " + this.chunkContent.length;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> CHUNK <SenderId> <FileId> <ChunkNo> "
        String message = this.version + " CHUNK " + this.messagePeerId + " " + this.fileId + " " + this.chunkNo + " ";

        // Get Message Bytes (Header + Body)
        return generateMessageWithBody(message, this.chunkContent);
    }

    @Override
    public void answer(int id) {
        return;
    }

    public byte[] getChunk() {
        return this.chunkContent;
    }

    public boolean correspondsTo(String fileId, int chunkNo) {
        return this.fileId.equals(fileId) && (this.chunkNo == chunkNo);
    }
}
