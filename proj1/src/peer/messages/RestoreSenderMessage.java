package peer.messages;

import peer.Channel;
import peer.ChannelName;

public class RestoreSenderMessage extends Message {
    private final String fileId;
    private final int chunkNo;

    public RestoreSenderMessage(Channel mc, Channel mdb, Channel mdr, String version, int peerId, String fileId, int chunkNo) {
        super(ChannelName.MC, mc, mdb, mdr, version, peerId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    public String toString() {
        return "[peer" + this.peerId + "] v" + this.version + " - Restore initiator " + this.fileId + ":" + this.chunkNo;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> GETCHUNK <SenderId> <FileId> <ChunkNo> "
        String message = this.version + " GETCHUNK " + this.peerId + " " + this.fileId + " " + this.chunkNo + " ";

        // Get Message Bytes (Header)
        return generateMessageWithoutBody(message);
    }

    @Override
    public Message answer(int id) {
        // TODO
        return null;
    }
}
