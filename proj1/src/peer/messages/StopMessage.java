package peer.messages;

import peer.Channel;
import peer.ChannelName;
import peer.ListenerThread;

public class StopMessage extends Message {
    public StopMessage(Channel mc, Channel mdb, Channel mdr, String version, int messagePeerId) {
        super(ChannelName.MC, mc, mdb, mdr, version, messagePeerId);
    }

    public String toString() {
        return "[peer" + this.messagePeerId + "] v" + this.version + " - Stop";
    }

    @Override
    public byte[] assemble() {
        // Header: "<Version> STOP <SenderId> "
        String message = this.version + " STOP " + this.messagePeerId + " ";

        // Get Message Bytes (Header)
        return generateMessageWithoutBody(message);
    }

    @Override
    public void answer(int id) {
        ListenerThread.STOP = true;
    }
}
