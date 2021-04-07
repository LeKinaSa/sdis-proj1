package peer;

import java.net.InetAddress;

public class ListenerThread extends Thread {
    InetAddress ip;
    int port;

    public ListenerThread(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void run() {
        System.out.println("Thread listening to " + ip + ":" + port);
        // TODO
    }
}
