package peer.state;

import java.util.ArrayList;
import java.util.List;

public class PeerState {
    private static final int UNLIMITED_STORAGE = -1;

    public static final PeerState INSTANCE = new PeerState(); // TODO: is this the best way to go, or is this a bad cut?

    private final List<BackedUpFile> files;
    private final List<BackedUpChunk> chunks;
    private int currentCapacity; // TODO: im storing in Bytes but the project specification says KBytes)
    private int storageCapacity; // TODO: im storing in Bytes but the project specification says KBytes)

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

    public void readjustCapacity(int newStorageCapacity) {
        // TODO: may be used by reclaim protocol
    }

    public void insertFile(BackedUpFile file) {
        this.files.add(file);
    }

    public boolean insertChunk(BackedUpChunk chunk) {
        if (this.fits(chunk.getSize())) {
            this.chunks.add(chunk);
            currentCapacity = currentCapacity + chunk.getSize();
            return true;
        }
        return false;
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
