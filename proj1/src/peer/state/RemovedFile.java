package peer.state;

import java.util.Set;

public class RemovedFile {
    private final String fileId;
    private final Set<Integer> peers;

    public RemovedFile(String fileId, Set<Integer> peers) {
        this.fileId = fileId;
        this.peers = peers;
    }

    public static RemovedFile fromJson() {
        // TODO
        return new RemovedFile(null, null);
    }

    public String toJson() {
        StringBuilder removedFileInfo = new StringBuilder();
        removedFileInfo.append("{");

        removedFileInfo.append("fileId:").append(this.fileId).append(",");

        removedFileInfo.append("peers:[");
        for (int peer : this.peers) {
            removedFileInfo.append(peer).append(",");
        }
        if (removedFileInfo.lastIndexOf(",") == (removedFileInfo.length() - 1)) {
            removedFileInfo.deleteCharAt(removedFileInfo.length() - 1);
        }
        removedFileInfo.append("]");

        removedFileInfo.append("}");
        return removedFileInfo.toString();
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
