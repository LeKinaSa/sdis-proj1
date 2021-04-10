package peer.state;

import java.util.ArrayList;
import java.util.List;

public class PeerState {
    private static final int UNLIMITED_STORAGE = -1;

    public static final PeerState INSTANCE = new PeerState(); // TODO: is this the best way to go, or is this a bad cut?

    private final List<BackedUpFile> files;
    private final List<BackedUpChunk> chunks;
    private int currentCapacity; // TODO: im storing in Bytes but the project specification says KBytes
    private int storageCapacity; // TODO: im storing in Bytes but the project specification says KBytes

    public PeerState() {
        this.files = new ArrayList<>();
        this.chunks = new ArrayList<>();
        this.storageCapacity = PeerState.UNLIMITED_STORAGE;
    }

    public boolean fits(int size) {
        if (this.storageCapacity == PeerState.UNLIMITED_STORAGE) {
            return true;
        }
        return this.storageCapacity >= this.currentCapacity + size;
    }

    public boolean storageIsStable() {
        return this.currentCapacity <= this.storageCapacity;
    }

    public void readjustCapacity(int newStorageCapacity) {
        this.storageCapacity = newStorageCapacity;
        if (this.storageIsStable()) {
            return;
        }

        // Separate All Chunks in Safe and Unsafe for Removal
        List<BackedUpChunk> safeRemoves = new ArrayList<>();
        List<BackedUpChunk> unsafeRemoves = new ArrayList<>();
        for (BackedUpChunk chunk : this.chunks) {
            if (chunk.canBeRemovedSafely()) {
                safeRemoves.add(chunk);
            }
            else {
                unsafeRemoves.add(chunk);
            }
        }

        // Firstly, Remove the Safe Chunks
        for (BackedUpChunk safeRemove : safeRemoves) {
            // Remove the Chunk
            this.removeChunkFromPeerStorage(safeRemove);
            // Alert the Other Peers
            // TODO: Alert other peers
            // Verify if More Removals are Needed
            if (this.storageIsStable()) {
                return;
            }
        }

        // Then, Remove the Other Ones (since the peer is still out of storage space)
        for (BackedUpChunk unsafeRemove : unsafeRemoves) {
            // Remove the Chunk
            this.removeChunkFromPeerStorage(unsafeRemove);
            // Alert the Other Peers
            // TODO: Alert other peers
            // Verify if More Removals are Needed
            if (this.storageIsStable()) {
                return;
            }
        }
    }

    public void insertFile(String pathname, String fileId, int replicationDegree) {
        for (BackedUpFile file : this.files) {
            if (file.correspondsTo(fileId)) {
                // TODO: file was already found - do i have to do something with this info
                return;
            }
        }
        BackedUpFile file = new BackedUpFile(pathname, fileId, replicationDegree);
        this.files.add(file);
    }

    public void insertReplicationDegreeOnFileChunk(String fileId, int chunkNo, int perceivedReplicationDegree) {
        for (BackedUpFile file : this.files) {
            if (file.correspondsTo(fileId)) {
                file.putChunk(chunkNo, perceivedReplicationDegree);
            }
        }
    }

    public void removeFile(String fileId) {
        for (BackedUpFile file : this.files) {
            if (file.correspondsTo(fileId)) {
                this.files.remove(file);
                break;
            }
        }

        List<BackedUpChunk> removedChunks = new ArrayList<>();
        for (BackedUpChunk chunk : this.chunks) {
            if (chunk.belongsTo(fileId)) {
                removedChunks.add(chunk);
            }
        }
        for (BackedUpChunk chunk : removedChunks) {
            this.removeChunkFromPeerStorage(chunk);
        }
    }

    public boolean insertChunk(String fileId, int chunkNo, int size, int replicationDegree) {
        BackedUpChunk chunk = new BackedUpChunk(fileId, chunkNo, size, replicationDegree);
        if (this.fits(chunk.getSize())) {
            this.chunks.add(chunk);
            currentCapacity = currentCapacity + chunk.getSize();
            return true;
        }
        return false;
    }

    public void peerHasChunk(String fileId, int chunkNo, int peerId) {
        for (BackedUpChunk chunk : this.chunks) {
            if (chunk.correspondsTo(fileId, chunkNo)) {
                chunk.peerHasChunk(peerId);
                break;
            }
        }
    }

    public void removeChunk(String fileId, int chunkNo) {
        for (BackedUpChunk chunk : this.chunks) {
            if (chunk.correspondsTo(fileId, chunkNo)) {
                this.removeChunkFromPeerStorage(chunk);
                break;
            }
        }
    }

    public void removeChunkFromPeerStorage(BackedUpChunk chunk) {
        this.currentCapacity = this.currentCapacity - chunk.getSize();
        this.chunks.remove(chunk);
    }

    public String toString() {
        String peerState = "";
        peerState += "----------------------------------------\n";
        peerState += "Current Capacity: " + this.currentCapacity + "\n";
        peerState += "Storage Capacity: " + this.storageCapacity + "\n";
        peerState += "----------------------------------------\n";
        peerState += "Files:\n";
        for (BackedUpFile file : this.files) {
            peerState += file.toString();
        }
        peerState += "----------------------------------------\n";
        peerState += "Chunks:\n";
        for (BackedUpChunk chunk : this.chunks) {
            peerState += chunk.toString();
        }
        peerState += "----------------------------------------\n";
        return peerState;
    }
}
