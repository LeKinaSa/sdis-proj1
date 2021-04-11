package peer.messages;

import peer.Channel;
import peer.ChannelName;
import peer.ClientEndpoint;
import peer.Utils;

import java.util.Set;

public class StartMessage extends Message {

    public StartMessage(Channel mc, Channel mdb, Channel mdr, String version, int messagePeerId) {
        super(ChannelName.MC, mc, mdb, mdr, version, messagePeerId);
    }

    public String toString() {
        return "[peer" + this.messagePeerId + "] v" + this.version + " - Start";
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> START <SenderId> "
        String message = this.version + " START " + this.messagePeerId + " ";

        // Get Message Bytes (Header)
        return generateMessageWithoutBody(message);
    }

    @Override
    public void answer(int id) {
        // New Thread to deal with the answer
        Thread thread = new Thread(() -> {
            Set<String> files = ClientEndpoint.state.removedFilesFromPeer(this.messagePeerId);
            Message message;
            for (String fileId : files) {
                message = new DeleteTargetMessage(this.mc, this.mdb, this.mdr, this.version, id, fileId, this.messagePeerId);
                for (int n = 0; n < ClientEndpoint.REPETITIONS; n ++) {
                    Utils.pause(Utils.getRandomNumber(0, 401));
                    Utils.sendMessage(message);
                }
            }
        });
        thread.start();
    }
}
