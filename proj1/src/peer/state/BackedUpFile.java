package peer.state;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BackedUpFile {
    private final String pathname;
    private final String fileId;
    private int desiredReplicationDegree;
    private final Map<Integer, Set<Integer>> perceivedReplicationDegreePerChunk;

    BackedUpFile(String pathname, String fileId, int replicationDegree) {
        this.pathname = pathname;
        this.fileId = fileId;
        this.desiredReplicationDegree = replicationDegree;
        this.perceivedReplicationDegreePerChunk = new HashMap<>();
    }

    public boolean correspondsTo(String fileId) {
        return this.fileId.equals(fileId);
    }

    public void changeReplicationDegree(int desiredReplicationDegree) {
        this.desiredReplicationDegree = desiredReplicationDegree;
    }

    public void putChunk(int chunkNo, int peerId) {
        if (!this.perceivedReplicationDegreePerChunk.containsKey(chunkNo)) {
            this.perceivedReplicationDegreePerChunk.put(chunkNo, new HashSet<>());
        }
        this.perceivedReplicationDegreePerChunk.get(chunkNo).add(peerId);
    }

    public void peerRemovedChunk(int chunkNo, int peerId) {
        this.perceivedReplicationDegreePerChunk.get(chunkNo).remove(peerId);
    }

    public Set<Integer> getPeersThatBackedUpTheFile() {
        Set<Integer> peers = new HashSet<>();
        for (int chunk : this.perceivedReplicationDegreePerChunk.keySet()) {
            for (int peer : this.perceivedReplicationDegreePerChunk.get(chunk)) {
                peers.add(peer);
            }
        }
        return peers;
    }

    public String toString() {
        String fileState = "";
        fileState += "\tFile: " + this.pathname + "\n";
        fileState += "\tId  : " + this.fileId + "\n";
        fileState += "\tRepl: " + this.desiredReplicationDegree + "\n";
        for (int chunk : this.perceivedReplicationDegreePerChunk.keySet()) {
            fileState += "\t\tChunk " + chunk + ": " + this.perceivedReplicationDegreePerChunk.get(chunk).size() + "\n";
        }
        return fileState;
    }
}
