import java.util.*;
import java.io.*;

public class DataManager {
    private static final String SAVE_FILE = "game_stats.txt";
    private static Properties stats = new Properties();

    static {
        load(); // Load immediately when the game starts
    }

    public static void load() {
        try (FileInputStream in = new FileInputStream(SAVE_FILE)) {
            stats.load(in);
        } catch (IOException e) {
            System.out.println("No save file found. Creating fresh profile!");
        }
    }

    public static void save() {
        try (FileOutputStream out = new FileOutputStream(SAVE_FILE)) {
            stats.store(out, "DIO-DGE IT Persistent Stats");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- ACCESSORS ---

    public static int getInt(String key) {
        return Integer.parseInt(stats.getProperty(key, "0"));
    }

    public static void setInt(String key, int value) {
        stats.setProperty(key, String.valueOf(value));
    }

    public static void addInt(String key, int amount) {
        setInt(key, getInt(key) + amount);
    }

    public static String getString(String key) {
        return stats.getProperty(key, "None");
    }

    public static void setString(String key, String value) {
        stats.setProperty(key, value);
    }
}