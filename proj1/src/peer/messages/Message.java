package peer.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

public abstract class Message {
    private static final byte[] CRLF = {0xD, 0xA};

    protected String version;
    protected int peerId;

    public Message(String version, int peerId) {
        this.version = version;
        this.peerId = peerId;
    }

    protected byte[] generateMessageWithBody(String header, byte[] body) {
        ByteArrayOutputStream aux = new ByteArrayOutputStream();
        try {
            aux.write(header.getBytes());
            aux.write(CRLF);
            aux.write(CRLF);
            aux.write(body);
        }
        catch (IOException exception) {
            return null;
        }
        return aux.toByteArray();
    }

    protected byte[] generateMessageWithoutBody(String header) {
        ByteArrayOutputStream aux = new ByteArrayOutputStream();
        try {
            aux.write(header.getBytes());
            aux.write(CRLF);
            aux.write(CRLF);
        }
        catch (IOException exception) {
            return null;
        }
        return aux.toByteArray();
    }

    public abstract byte[] assemble();

    public static Message parse(DatagramPacket packet) {
        return null;
    }
}
