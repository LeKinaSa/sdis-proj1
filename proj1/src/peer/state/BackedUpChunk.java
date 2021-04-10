package peer.state;

public class BackedUpChunk {
    private final String fileId;
    private final int chunkNo;
    private final int size; // TODO: im storing in Bytes but the project specification says KBytes)
    private final int desiredReplicationDegree;
    private int perceivedReplicationDegree;

    public BackedUpChunk(String fileId, int chunkNo, int size, int desiredReplicationDegree) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.size = size;
        this.desiredReplicationDegree = desiredReplicationDegree;
        this.perceivedReplicationDegree = -1;
    }

    public int getSize() {
        return this.size;
    }

    public void setPerceivedReplicationDegree(int perceivedReplicationDegree) {
        this.perceivedReplicationDegree = perceivedReplicationDegree;
    }

    public String toString() {
        String chunkState = "";
        chunkState += "\tFile: " + this.fileId + ":" + this.chunkNo + "\n";
        chunkState += "\tSize: " + this.size + "\n";
        chunkState += "\tRepl: " + this.desiredReplicationDegree + " - " + this.perceivedReplicationDegree + "\n";
        return chunkState;
    }
}
