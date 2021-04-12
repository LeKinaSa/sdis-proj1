package peer.state;

import peer.ClientEndpoint;
import peer.PeerDebugger;
import peer.Utils;
import peer.messages.Message;
import peer.messages.ReclaimReceiverMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class PeerState {
    // {files:[],chunks:[{fileId:-1147906284,chunkNo:0,size:8,desiredReplicationDegree:1,perceivedReplicationDegree:[2,3,4}],removed:[],currentCapacity:8,storageCapacity:-1}
    // {files:[{pathname:test.txt,fileId:-1147906284,desiredReplicationDegree:1,perceivedReplicationDegreePerChunk:{0:[2,3,4]}}],chunks:[],removed:[],currentCapacity:0,storageCapacity:-1}
    private static final int UNLIMITED_STORAGE = -1;

    private final List<BackedUpFile> files;
    private final List<BackedUpChunk> chunks;
    private final List<RemovedFile> removed;
    private int currentCapacity; // Bytes
    private int storageCapacity; // Bytes

    public PeerState() {
        this.files = new ArrayList<>();
        this.chunks = new ArrayList<>();
        this.storageCapacity = PeerState.UNLIMITED_STORAGE;
        this.removed = new ArrayList<>();
    }

    public static PeerState fromJson(String fileInfo) {
        Pattern pattern = Pattern.compile("");
        // TODO
        return new PeerState();
    }

    public String toJson() {
        StringBuilder info = new StringBuilder();
        info.append("{");

        // Files
        info.append("files:[");
        for (BackedUpFile file : this.files) {
            info.append(file.toJson()).append(",");
        }
        if (info.lastIndexOf(",") == (info.length() - 1)) {
            info.deleteCharAt(info.length() - 1);
        }
        info.append("],");

        // Chunks
        info.append("chunks:[");
        for (BackedUpChunk chunk : this.chunks) {
            info.append(chunk.toJson()).append(",");
        }
        if (info.lastIndexOf(",") == (info.length() - 1)) {
            info.deleteCharAt(info.length() - 1);
        }
        info.append("],");

        // Removed
        info.append("removed:[");
        for (RemovedFile file : this.removed) {
            info.append(file.toJson()).append(",");
        }
        if (info.lastIndexOf(",") == (info.length() - 1)) {
            info.deleteCharAt(info.length() - 1);
        }
        info.append("],");

        // Current Capacity
        info.append("currentCapacity:").append(this.currentCapacity).append(",");

        // Storage Capacity
        info.append("storageCapacity:").append(this.storageCapacity);

        info.append("}");
        return info.toString();
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

    public void readjustCapacity(ClientEndpoint peer, int newStorageCapacity) {
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
            this.alertOtherPeersOfChunkDeletion(peer, safeRemove);
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
            this.alertOtherPeersOfChunkDeletion(peer, unsafeRemove);
            // Verify if More Removals are Needed
            if (this.storageIsStable()) {
                return;
            }
        }
    }

    public void alertOtherPeersOfChunkDeletion(ClientEndpoint peer, BackedUpChunk chunk) {
        final int delay = 50;
        Message message = new ReclaimReceiverMessage(peer.getMC(), peer.getMDB(), peer.getMDR(), peer.getVersion(), peer.getId(), chunk.getFileId(), chunk.getChunkNo());

        // Send Message
        Utils.sendMessage(message);
        for (int n = 0; n < ClientEndpoint.REPETITIONS - 1; n ++) {
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

    public void chunkClearReplication(String fileId, int chunkNo) {
        for (BackedUpChunk chunk : this.chunks) {
            if (chunk.correspondsTo(fileId, chunkNo)) {
                chunk.clearReplication();
                break;
            }
        }
    }

    public Set<Integer> chunkGetReplication(String fileId, int chunkNo) {
        for (BackedUpChunk chunk : this.chunks) {
            if (chunk.correspondsTo(fileId, chunkNo)) {
                return chunk.backupAnswers();
            }
        }
        return null;
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
                // File Already Backed Up in the System
                file.changeReplicationDegree(replicationDegree);
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

    public boolean hasChunk(String fileId, int chunkNo) {
        for (BackedUpChunk chunk : this.chunks) {
            if (chunk.correspondsTo(fileId, chunkNo)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        StringBuilder peerState = new StringBuilder();
        peerState.append("----------------------------------------\n");
        peerState.append("Used    Capacity: ").append(this.currentCapacity).append(" Bytes\n");
        peerState.append("Maximum Capacity: ");
        if (this.storageCapacity == PeerState.UNLIMITED_STORAGE) {
            peerState.append("Unlimited\n");
        }
        else {
            peerState.append(this.storageCapacity).append(" Bytes\n");
        }
        peerState.append("----------------------------------------\n");
        peerState.append("Files:\n");
        for (BackedUpFile file : this.files) {
            peerState.append(file.toString()).append("\n");
        }
        peerState.append("----------------------------------------\n");
        peerState.append("Chunks:\n");
        for (BackedUpChunk chunk : this.chunks) {
            peerState.append(chunk.toString()).append("\n");
        }
        peerState.append("----------------------------------------\n");
        return peerState.toString();
    }

    public void fileIsBeingRemoved(String fileId) {
        // Delete enhancement

        // Verify that the file isn't already stored in the removed files
        for (RemovedFile file : this.removed) {
            if (file.correspondsTo(fileId)) {
                return;
            }
        }

        // Collect the peers that have this file backed up
        Set<Integer> peers = new HashSet<>();
        for (BackedUpFile file : this.files) {
            if (file.correspondsTo(fileId)) {
                peers.addAll(file.getPeersThatBackedUpTheFile());
                break;
            }
        }
        for (BackedUpChunk chunk : this.chunks) {
            if (chunk.belongsTo(fileId)) {
                peers.addAll(chunk.getPeersThatBackedUpTheChunk());
            }
        }

        // Add this file to the removed files
        if (peers.size() != 0) {
            this.removed.add(new RemovedFile(fileId, peers));
        }
    }

    public void fileRemovedFromPeer(String fileId, int peerId) {
        // Delete enhancement

        for (RemovedFile file : this.removed) {
            if (file.correspondsTo(fileId)) {
                // This Peer Has Deleted this File
                file.peerHasDeletedFile(peerId);

                // All Peer Storage has been Cleaned
                if (file.fileHasBeenDeletedFromAllPeers()) {
                    this.removed.remove(file);
                }

                return;
            }
        }
    }

    public Set<String> removedFilesFromPeer(int peerId) {
        Set<String> fileIds = new HashSet<>();
        for (RemovedFile file : this.removed) {
            if (file.wasOnPeer(peerId)) {
                fileIds.add(file.getFileId());
            }
        }
        return fileIds;
    }
}
