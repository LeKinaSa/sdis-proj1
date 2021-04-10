package peer.state;

import peer.ClientEndpoint;
import peer.Utils;
import peer.messages.Message;
import peer.messages.ReclaimReceiverMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PeerState {
    private static final int UNLIMITED_STORAGE = -1;

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

    public void readjustCapacity(ClientEndpoint peer, int newStorageCapacity, int repetitions) {
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
            // Remove the Chunk from the Peer State
            this.removeChunkFromPeerStorage(safeRemove);
            // Remove the Chunk from the Peer Storage
            Utils.deleteChunk(peer.getId(), safeRemove.getFileId(), safeRemove.getChunkNo());
            // Alert the Other Peers
            this.alertOtherPeersOfChunkDeletion(peer, safeRemove, repetitions);
            // Verify if More Removals are Needed
            if (this.storageIsStable()) {
                return;
            }
        }

        // Then, Remove the Other Ones (since the peer is still out of storage space)
        for (BackedUpChunk unsafeRemove : unsafeRemoves) {
            // Remove the Chunk from the Peer State
            this.removeChunkFromPeerStorage(unsafeRemove);
            // Remove the Chunk from the Peer Storage
            Utils.deleteChunk(peer.getId(), unsafeRemove.getFileId(), unsafeRemove.getChunkNo());
            // Alert the Other Peers
            this.alertOtherPeersOfChunkDeletion(peer, unsafeRemove, repetitions);
            // Verify if More Removals are Needed
            if (this.storageIsStable()) {
                return;
            }
        }
    }

    public void alertOtherPeersOfChunkDeletion(ClientEndpoint peer, BackedUpChunk chunk, int repetitions) {
        final int delay = 50;
        Message message = new ReclaimReceiverMessage(peer.getMC(), peer.getMDB(), peer.getMDR(), peer.getVersion(), peer.getId(), chunk.getFileId(), chunk.getChunkNo());

        // Send Message
        Utils.sendMessage(message);
        for (int n = 0; n < repetitions - 1; n ++) {
            Utils.pause(delay);
            Utils.sendMessage(message);
        }
    }

    public boolean chunkWithSufficientReplication(String fileId, int chunkNo) {
        for (BackedUpChunk chunk : this.chunks) {
            if (chunk.correspondsTo(fileId, chunkNo)) {
                return chunk.hasSufficientReplication();
            }
        }
        return true;
    }

    public int getReplicationDegreeForChunk(String fileId, int chunkNo) {
        for (BackedUpChunk chunk : this.chunks) {
            if (chunk.correspondsTo(fileId, chunkNo)) {
                return chunk.getDesiredReplicationDegree();
            }
        }
        return 0;
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

    public boolean hasFile(String fileId) {
        for (BackedUpFile file : this.files) {
            if (file.correspondsTo(fileId)) {
                return true;
            }
        }
        return false;
    }

    public boolean insertChunk(String fileId, int chunkNo, int size, int replicationDegree) {
        BackedUpChunk chunk = new BackedUpChunk(fileId, chunkNo, size, replicationDegree);
        if (this.hasFile(fileId)) {
            return false;
        }
        if (this.fits(chunk.getSize())) {
            this.chunks.add(chunk);
            currentCapacity = currentCapacity + chunk.getSize();
            return true;
        }
        return false;
    }

    public void peerAddedChunk(String fileId, int chunkNo, int peerId) {
        for (BackedUpChunk chunk : this.chunks) {
            if (chunk.correspondsTo(fileId, chunkNo)) {
                chunk.peerAddedChunk(peerId);
                return;
            }
        }
        for (BackedUpFile file : this.files) {
            if (file.correspondsTo(fileId)) {
                file.putChunk(chunkNo, peerId);
                return;
            }
        }
    }

    public void peerRemovedChunk(String fileId, int chunkNo, int peerId) {
        for (BackedUpChunk chunk : this.chunks) {
            if (chunk.correspondsTo(fileId, chunkNo)) {
                chunk.peerRemovedChunk(peerId);
                return;
            }
        }

        for (BackedUpFile file : this.files) {
            if (file.correspondsTo(fileId)) {
                file.peerRemovedChunk(chunkNo, peerId);
                return;
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
