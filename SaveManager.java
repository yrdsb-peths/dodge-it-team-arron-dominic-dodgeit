import java.util.Properties;
import java.io.*;

public class SaveManager {
    private static final String SAVE_FILE = "user_stats.txt";
    private static final String CHECKSUM_KEY = "integrity";
    private static Properties stats = new Properties();

    public static void load() {
        try (FileInputStream in = new FileInputStream(SAVE_FILE)) {
            stats.load(in);
            if (!verifyChecksum()) {
                System.out.println("Save file tampered — resetting.");
                stats.clear();
                save();
            }
        } catch (IOException e) {
            System.out.println("New archive created.");
        }
    }

    public static void save() {
        stats.remove(CHECKSUM_KEY);           // strip old checksum before hashing
        stats.setProperty(CHECKSUM_KEY, String.valueOf(computeChecksum()));
        try (FileOutputStream out = new FileOutputStream(SAVE_FILE)) {
            stats.store(out, "DIO-DGE IT PERSISTENT STATS");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int computeChecksum() {
        // Hash every key=value pair except the checksum itself
        StringBuilder sb = new StringBuilder();
        for (String key : stats.stringPropertyNames()) {
            if (!key.equals(CHECKSUM_KEY))
                sb.append(key).append("=").append(stats.getProperty(key)).append(";");
        }
        return sb.toString().hashCode() ^ 0xDEADBEEF; // XOR makes it less obvious
    }

    private static boolean verifyChecksum() {
        if (!stats.containsKey(CHECKSUM_KEY)) return false;
        try {
            int stored = Integer.parseInt(stats.getProperty(CHECKSUM_KEY));
            stats.remove(CHECKSUM_KEY);           // remove before recomputing
            boolean valid = (computeChecksum() == stored);
            stats.setProperty(CHECKSUM_KEY, String.valueOf(stored)); // put back
            return valid;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static int    getInt(String key)            { return Integer.parseInt(stats.getProperty(key, "0")); }
    public static void   setInt(String key, int value) { stats.setProperty(key, String.valueOf(value)); }
    public static void   addInt(String key, int amount){ setInt(key, getInt(key) + amount); }

    public static String getFavoriteCharacter() {
        String fav = "None";
        int maxTime = -1;
        for (CharacterConfig config : CharacterConfig.values()) {
            int time = getInt("time_" + config.name());
            if (time > maxTime) { maxTime = time; fav = config.displayName; }
        }
        return fav;
    }
    
}