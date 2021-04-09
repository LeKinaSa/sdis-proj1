package peer;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static int getRandomNumber(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public static void pause(int ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        }
        catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    public static boolean fileExists(int id, String fileId, int chunkNo) {
        // TODO
        return false;
    }

    public static void store(int id, String fileId, int chunkNo, byte[] chunkContent) {
        // TODO
    }
}
