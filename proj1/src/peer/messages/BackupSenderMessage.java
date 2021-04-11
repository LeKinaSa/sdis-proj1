package peer.messages;

import peer.*;

import java.nio.charset.StandardCharsets;

public class BackupSenderMessage extends Message {
    private final String fileId;
    private final int chunkNo;
    private final int replicationDegree;
    private final byte[] chunkContent;

    public BackupSenderMessage(Channel mc, Channel mdb, Channel mdr, String version, int peerId, String fileId, int chunkNo, int replicationDegree, byte[] chunkContent) {
        super(ChannelName.MDB, mc, mdb, mdr, version, peerId);
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.chunkContent = chunkContent;
    }

    public String toString() {
        String content = new String(chunkContent, StandardCharsets.UTF_8);
        return "[peer" + this.messagePeerId + "] v" + this.version + " - Backup initiator " + this.fileId + ":" + this.chunkNo + " x" + replicationDegree + "::" + content;
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> PUTCHUNK <SenderId> <FileId> <ChunkNo> <ReplicationDegree> "
        String message = this.version + " PUTCHUNK " + this.messagePeerId + " " + this.fileId + " " + this.chunkNo + " " + this.replicationDegree + " ";

        // Get Message Bytes (Header + Body)
        return generateMessageWithBody(message, this.chunkContent);
    }

    @Override
    public void answer(int id) {
        if (this.messagePeerId == id) {
            return;
        }

        // New Thread to deal with the answer
        Thread thread = new Thread(() -> {
            if (!Utils.fileExists(id, this.fileId, this.chunkNo)) {

                // If this peer didn't have this chunk, insert it on peer state
                if (!ClientEndpoint.state.insertChunk(this.fileId, this.chunkNo, this.chunkContent.length, this.replicationDegree)) {
                    // If it doesn't fit, return
                    return;
                }
                // Store the chunk (if the chunk isn't already stored in this peer)
                if (!Utils.store(id, this.fileId, this.chunkNo, this.chunkContent)) {
                    // An error has occurred while storing, remove the chunk from the peer state
                    ClientEndpoint.state.removeChunk(this.fileId, this.chunkNo);
                    PeerDebugger.println("Error when storing " + this.fileId + " chunk " + this.chunkNo);
                    return;
                }
            }
            // Delay from [0, 400[ ms
            Utils.pause(Utils.getRandomNumber(0, 401));

            // Send Message: BackupReceiverMessage (with this peer id not the id on the received message)
            Message answer = new BackupReceiverMessage(this.mc, this.mdb, this.mdr, this.version, id, this.fileId, this.chunkNo);
            Utils.sendMessage(answer);
        });
        thread.start();
    }

    public boolean correspondsTo(String fileId, int chunkNo) {
        return this.fileId.equals(fileId) && (this.chunkNo == chunkNo);
    }
}
