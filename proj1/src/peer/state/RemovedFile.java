package peer.state;

import java.util.Set;

public class RemovedFile {
    private final String fileId;
    private final Set<Integer> peers;

    public RemovedFile(String fileId, Set<Integer> peers) {
        this.fileId = fileId;
        this.peers = peers;
    }

    public String getFileId() {
        return this.fileId;
    }

    public boolean correspondsTo(String fileId) {
        return this.fileId.equals(fileId);
    }

    public void peerHasDeletedFile(int peer) {
        this.peers.remove(peer);
    }

    public boolean fileHasBeenDeletedFromAllPeers() {
        return this.peers.size() == 0;
    }

    public boolean wasOnPeer(int peerId) {
        return this.peers.contains(peerId);
    }
}
