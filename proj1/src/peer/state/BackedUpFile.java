package peer.state;

import java.util.HashMap;
import java.util.Map;

public class BackedUpFile {
    private final String pathname;
    private final String fileId;
    private final int desiredReplicationDegree;
    private final Map<Integer, Integer> perceivedReplicationDegreePerChunk;

    BackedUpFile(String pathname, String fileId, int replicationDegree) {
        this.pathname = pathname;
        this.fileId = fileId;
        this.desiredReplicationDegree = replicationDegree;
        this.perceivedReplicationDegreePerChunk = new HashMap<>();
    }

    public void putChunk(int chunkNo, int perceivedReplicationDegree) {
        this.perceivedReplicationDegreePerChunk.remove(chunkNo);
        this.perceivedReplicationDegreePerChunk.put(chunkNo, perceivedReplicationDegree);
    }

    public int checkChunk(int chunkNo) {
        if (this.perceivedReplicationDegreePerChunk.containsKey(chunkNo)) {
            return this.perceivedReplicationDegreePerChunk.get(chunkNo);
        }
        return -1;
    }

    public String toString() {
        String fileState = "";
        fileState += "\tPath: " + this.pathname + "\n";
        fileState += "\tFile: " + this.fileId + "\n";
        fileState += "\tRepl: " + this.desiredReplicationDegree + "\n";
        for (int chunk : this.perceivedReplicationDegreePerChunk.keySet()) {
            fileState += "\t\tChunk " + chunk + ": " + this.perceivedReplicationDegreePerChunk.get(chunk) + "\n";
        }
        return fileState;
    }
}
