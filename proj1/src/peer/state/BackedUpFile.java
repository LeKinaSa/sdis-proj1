package peer.state;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BackedUpFile {
    private final String pathname;
    private final String fileId;
    private final int desiredReplicationDegree;
    private final Map<Integer, Set<Integer>> perceivedReplicationDegreePerChunk;

    BackedUpFile(String pathname, String fileId, int replicationDegree) {
        this.pathname = pathname;
        this.fileId = fileId;
        this.desiredReplicationDegree = replicationDegree;
        this.perceivedReplicationDegreePerChunk = new HashMap<>();
    }

    public static BackedUpFile fromJson() {
        // TODO
        return null;
    }

    public String toJson() {
        StringBuilder fileInfo = new StringBuilder();
        fileInfo.append("{");

        fileInfo.append("pathname:").append(this.pathname).append(",");
        fileInfo.append("fileId:").append(this.fileId).append(",");
        fileInfo.append("desiredReplicationDegree:").append(this.desiredReplicationDegree).append(",");

        fileInfo.append("perceivedReplicationDegreePerChunk:{");
        for (int chunk : this.perceivedReplicationDegreePerChunk.keySet()) {
            fileInfo.append(chunk).append(":[");

            for (int peer : this.perceivedReplicationDegreePerChunk.get(chunk)) {
                fileInfo.append(peer).append(",");
            }
            if (fileInfo.lastIndexOf(",") == (fileInfo.length() - 1)) {
                fileInfo.deleteCharAt(fileInfo.length() - 1);
            }

            fileInfo.append("],");
        }
        if (fileInfo.lastIndexOf(",") == (fileInfo.length() - 1)) {
            fileInfo.deleteCharAt(fileInfo.length() - 1);
        }
        fileInfo.append("}");

        fileInfo.append("}");
        return fileInfo.toString();
    }

    public boolean correspondsTo(String fileId) {
        return this.fileId.equals(fileId);
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
            peers.addAll(this.perceivedReplicationDegreePerChunk.get(chunk));
        }
        return peers;
    }

    public String toString() {
        StringBuilder fileState = new StringBuilder();
        fileState.append("\tPath: ").append(this.pathname).append("\n");
        fileState.append("\tFile: ").append(this.fileId).append("\n");
        fileState.append("\tRepl: ").append(this.desiredReplicationDegree).append("\n");
        for (int chunk : this.perceivedReplicationDegreePerChunk.keySet()) {
            fileState.append("\t\tChunk ").append(chunk).append(": ").append(this.perceivedReplicationDegreePerChunk.get(chunk).size()).append("\n");
        }
        return fileState.toString();
    }
}
