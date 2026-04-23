import java.util.Properties;
import java.io.*;

public class SaveManager {
    private static final String SAVE_FILE = "user_stats.txt";
    private static Properties stats = new Properties();

    /** Call this ONCE in MyWorld's constructor to load existing data */
    public static void load() {
        try (FileInputStream in = new FileInputStream(SAVE_FILE)) {
            stats.load(in);
        } catch (IOException e) {
            // No file yet? No problem.
            System.out.println("New archive created.");
        }
    }

    /** Writes current data to the text file. Call this on Game Over or Exit. */
    public static void save() {
        try (FileOutputStream out = new FileOutputStream(SAVE_FILE)) {
            stats.store(out, "DIO-DGE IT PERSISTENT STATS");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- HELPER METHODS ---

    /** Get an integer stat (default is 0 if not found) */
    public static int getInt(String key) {
        return Integer.parseInt(stats.getProperty(key, "0"));
    }

    /** Set an integer stat */
    public static void setInt(String key, int value) {
        stats.setProperty(key, String.valueOf(value));
    }

    /** Increment a stat (e.g., adding +1 to ability usage) */
    public static void addInt(String key, int amount) {
        setInt(key, getInt(key) + amount);
    }

    /** Calculates which character has the most play time */
    public static String getFavoriteCharacter() {
        String fav = "None";
        int maxTime = -1;
        // Loop through all character names to find the highest playtime
        for (CharacterConfig config : CharacterConfig.values()) {
            int time = getInt("time_" + config.name());
            if (time > maxTime) {
                maxTime = time;
                fav = config.displayName;
            }
        }
        return fav;
    }
}