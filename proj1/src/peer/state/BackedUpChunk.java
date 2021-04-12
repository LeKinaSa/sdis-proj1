package peer.state;

import peer.Utils;

import java.util.HashSet;
import java.util.Set;

public class BackedUpChunk {
    private final String fileId;
    private final int chunkNo;
    private final int size; // Bytes
    private final int desiredReplicationDegree;
    private final Set<Integer> perceivedReplicationDegree;

    public BackedUpChunk(String fileId, int chunkNo, int size, int desiredReplicationDegree) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.size = size;
        this.desiredReplicationDegree = desiredReplicationDegree;
        this.perceivedReplicationDegree = new HashSet<>();
    }

    public BackedUpChunk(String fileId, int chunkNo, int size, int desiredReplicationDegree, Set<Integer> perceivedReplicationDegree) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.size = size;
        this.desiredReplicationDegree = desiredReplicationDegree;
        this.perceivedReplicationDegree = perceivedReplicationDegree;
    }

    public static BackedUpChunk fromJson(String info) {
        // FileId
        String fileId = info.substring(info.indexOf(":") + 1, info.indexOf(","));
        info = info.substring(info.indexOf(",") + 1);

        // ChunkNo
        String chunkNoStr = info.substring(info.indexOf(":") + 1, info.indexOf(","));
        int chunkNo = Integer.parseInt(chunkNoStr);
        info = info.substring(info.indexOf(",") + 1);

        // Size
        String sizeStr = info.substring(info.indexOf(":") + 1, info.indexOf(","));
        int size = Integer.parseInt(sizeStr);
        info = info.substring(info.indexOf(",") + 1);

        // Desired Replication Degree
        String desiredReplicationDegreeStr = info.substring(info.indexOf(":") + 1, info.indexOf(","));
        int desiredReplicationDegree = Integer.parseInt(desiredReplicationDegreeStr);
        info = info.substring(info.indexOf(",") + 1);

        // Perceived Replication Degree
        String perceivedReplicationDegreeStr = info.substring(info.indexOf("[") + 1, info.indexOf("]"));
        Set<Integer> perceivedReplicationDegree = Utils.parseNumberList(perceivedReplicationDegreeStr);

        return new BackedUpChunk(fileId, chunkNo, size, desiredReplicationDegree, perceivedReplicationDegree);
    }

    public String toJson() {
        StringBuilder chunkInfo = new StringBuilder();
        chunkInfo.append("{");

        chunkInfo.append("fileId:").append(this.fileId).append(",");
        chunkInfo.append("chunkNo:").append(this.chunkNo).append(",");
        chunkInfo.append("size:").append(this.size).append(",");
        chunkInfo.append("desiredReplicationDegree:").append(this.desiredReplicationDegree).append(",");

        chunkInfo.append("perceivedReplicationDegree:[");
        for (int peer : this.perceivedReplicationDegree) {
            chunkInfo.append(peer).append(",");
        }
        if (chunkInfo.lastIndexOf(",") == (chunkInfo.length() - 1)) {
            chunkInfo.deleteCharAt(chunkInfo.length() - 1);
        }
        chunkInfo.append("]");

        chunkInfo.append("}");
        return chunkInfo.toString();
    }

    public String getFileId() {
        return this.fileId;
    }

    public int getChunkNo() {
        return this.chunkNo;
    }

    public int getSize() {
        return this.size;
    }

    public int getDesiredReplicationDegree() {
        return this.desiredReplicationDegree;
    }

    public void clearReplication() {
        this.perceivedReplicationDegree.clear();
    }

    public Set<Integer> backupAnswers() {
        return this.perceivedReplicationDegree;
    }

    public void peerAddedChunk(int peerId) {
        this.perceivedReplicationDegree.add(peerId);
    }

    public void peerRemovedChunk(int peerId) {
        this.perceivedReplicationDegree.remove(peerId);
    }

    public boolean correspondsTo(String fileId, int chunkNo) {
        return this.fileId.equals(fileId) && (this.chunkNo == chunkNo);
    }

    public boolean belongsTo(String fileId) {
        return this.fileId.equals(fileId);
    }

    public boolean canBeRemovedSafely() {
        return this.perceivedReplicationDegree.size() > this.desiredReplicationDegree;
    }

    public boolean hasSufficientReplication() {
        return this.perceivedReplicationDegree.size() >= this.desiredReplicationDegree;
    }

    public Set<Integer> getPeersThatBackedUpTheChunk() {
        return this.perceivedReplicationDegree;
    }

    public String toString() {
        String chunkState = "";
        chunkState += "\tFile: " + this.fileId + ":" + this.chunkNo + "\n";
        chunkState += "\tSize: " + this.size + "\n";
        chunkState += "\tRepl: " + this.desiredReplicationDegree + " - " + this.perceivedReplicationDegree.size() + "\n";
        return chunkState;
    }
}
