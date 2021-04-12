package peer;

import java.net.InetAddress;

public class Channel {
    public InetAddress ip;
    public int port;

    public Channel(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }
}
