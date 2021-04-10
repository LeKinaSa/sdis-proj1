package peer.state;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BackedUpChunk {
    private final String fileId;
    private final int chunkNo;
    private final int size; // TODO: im storing in Bytes but the project specification says KBytes
    private final int desiredReplicationDegree;
    private final Set<Integer> perceivedReplicationDegree;

    public BackedUpChunk(String fileId, int chunkNo, int size, int desiredReplicationDegree) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.size = size;
        this.desiredReplicationDegree = desiredReplicationDegree;
        this.perceivedReplicationDegree = new HashSet<>();
    }

    public int getSize() {
        return this.size;
    }

    public void peerHasChunk(int peerId) {
        if (!this.perceivedReplicationDegree.contains(peerId)) {
            this.perceivedReplicationDegree.add(peerId);
        }
    }

    public boolean correspondsTo(String fileId, int chunkNo) {
        return this.fileId.equals(fileId) && (this.chunkNo == chunkNo);
    }

    public String toString() {
        String chunkState = "";
        chunkState += "\tFile: " + this.fileId + ":" + this.chunkNo + "\n";
        chunkState += "\tSize: " + this.size + "\n";
        chunkState += "\tRepl: " + this.desiredReplicationDegree + " - " + this.perceivedReplicationDegree.size() + "\n";
        return chunkState;
    }
}
