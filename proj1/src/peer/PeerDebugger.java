package peer;

public class PeerDebugger {
    private static int id;

    public static void setId(int x) {
        id = x;
    }

    public static synchronized void println(String x) {
        System.out.println("[peer" + id + "] " + x);
    }
}
