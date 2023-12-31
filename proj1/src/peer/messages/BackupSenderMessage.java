package peer.messages;

import peer.*;

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
        return "[peer" + this.messagePeerId + "] v" + this.version + " - Backup initiator " + this.fileId + ":" + this.chunkNo + " x" + replicationDegree + ":: chunk size " + this.chunkContent.length;
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
        // New Thread to deal with the answer
        Thread thread = new Thread(() -> {
            if (this.messagePeerId == id) {
                if (ClientEndpoint.state.hasChunk(this.fileId, this.chunkNo)) {
                    // Send Message (ensure all the peers have the correct perceived chunk replication after reclaim protocol)
                    Message answer = new BackupReceiverMessage(this.mc, this.mdb, this.mdr, this.version, id, this.fileId, this.chunkNo);
                    Utils.sendMessage(answer);
                }
                return;
            }

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

            if (!this.version.equals("1.0")) {
                // When the replication degree was already acquired, there is no need to store this chunk
                if (ClientEndpoint.state.chunkWithSufficientReplication(this.fileId, this.chunkNo)) {
                    // Delete the chunk from this peer storage
                    Utils.deleteChunk(id, this.fileId, this.chunkNo);
                    // Remove the chunk from the peer state
                    ClientEndpoint.state.removeChunk(this.fileId, this.chunkNo);
                    return;
                }
            }
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
