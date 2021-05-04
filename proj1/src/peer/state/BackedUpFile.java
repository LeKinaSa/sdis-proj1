package peer.state;

import peer.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BackedUpFile {
    private final String pathname;
    private final String fileId;
    private int desiredReplicationDegree;
    private final Map<Integer, Set<Integer>> perceivedReplicationDegreePerChunk;

    public BackedUpFile(String pathname, String fileId, int replicationDegree) {
        this.pathname = pathname;
        this.fileId = fileId;
        this.desiredReplicationDegree = replicationDegree;
        this.perceivedReplicationDegreePerChunk = new HashMap<>();
    }

    public BackedUpFile(String pathname, String fileId, int replicationDegree, Map<Integer, Set<Integer>> perceivedReplicationDegreePerChunk) {
        this.pathname = pathname;
        this.fileId = fileId;
        this.desiredReplicationDegree = replicationDegree;
        this.perceivedReplicationDegreePerChunk = perceivedReplicationDegreePerChunk;
    }

    public static BackedUpFile fromJson(String info) {
        // Pathname
        String pathname = info.substring(info.indexOf(":") + 1, info.indexOf(","));
        info = info.substring(info.indexOf(",") + 1);

        // FileId
        String fileId = info.substring(info.indexOf(":") + 1, info.indexOf(","));
        info = info.substring(info.indexOf(",") + 1);

        // Desired Replication Degree
        String desiredReplicationDegreeStr = info.substring(info.indexOf(":") + 1, info.indexOf(","));
        int desiredReplicationDegree = Integer.parseInt(desiredReplicationDegreeStr);
        info = info.substring(info.indexOf(",") + 1);

        // Perceived Replication Degree
        Map<Integer, Set<Integer>> perceivedReplicationDegree = new HashMap<>();
        info = info.substring(info.indexOf("{") + 1, info.indexOf("}"));

        String chunkNoStr;
        int chunkNo;
        String peersStr;
        Set<Integer> peers;
        while (true) {
            // ChunkNo
            chunkNoStr = info.substring(0, info.indexOf(":"));
            chunkNo = Integer.parseInt(chunkNoStr);

            // Peers
            peersStr = info.substring(info.indexOf("[") + 1, info.indexOf("]"));
            peers = Utils.parseNumberList(peersStr);

            // Add to map
            perceivedReplicationDegree.put(chunkNo, peers);

            // Advance to the next chunk
            if (info.indexOf("]") == (info.length() - 1)) {
                break;
            }
            info = info.substring(info.indexOf("]") + 2);
        }
        return new BackedUpFile(pathname, fileId, desiredReplicationDegree, perceivedReplicationDegree);
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

    public void changeReplicationDegree(int desiredReplicationDegree) {
        this.desiredReplicationDegree = desiredReplicationDegree;
    }

    public void clearReplicationDegree() {
        this.perceivedReplicationDegreePerChunk.clear();
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
