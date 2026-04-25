import java.util.HashMap;
import java.io.*;
import java.util.Properties;

public class ShopManager {
    private static final String SHOP_FILE = "shop_prices.txt";
    private static HashMap<String, Integer> authenticPrices = new HashMap<>();
    private static Properties filePrices = new Properties();

    static {
        // --- CHARACTER PRICES ---
        addPrice("char_moonknight", 0); // Free
        addPrice("char_dio", 500);
        addPrice("char_diavolo", 2000);
        addPrice("char_ringo", 1000);
        addPrice("char_omnipotent_dio", 2000);
 
        addPrice("char_custom", 100);

        // --- ABILITY PRICES ---
        addPrice("ability_ability_darkspell02", 0); // Free
        addPrice("ability_ability_darkspell01", 200);
        addPrice("ability_ability_madeinheaven", 1000);
        addPrice("ability_ability_standpunch", 500); 
        addPrice("ability_ability_stickyfingers", 800); 
    }

    // Helper to make sure we don't get case-sensitivity bugs
    private static void addPrice(String key, int price) {
        authenticPrices.put(key.toLowerCase(), price);
    }

    public static void init() {
        boolean tampered = false;
        try (FileInputStream in = new FileInputStream(SHOP_FILE)) {
            filePrices.load(in);
            for (String key : authenticPrices.keySet()) {
                if (!filePrices.containsKey(key) || Integer.parseInt(filePrices.getProperty(key)) != authenticPrices.get(key)) {
                    tampered = true;
                }
            }
        } catch (Exception e) { tampered = true; }

        if (tampered) {
            System.out.println("Lol, nice try hacking the prices! Don't do that again (I fixed them for you)");
            filePrices.clear();
            for (String key : authenticPrices.keySet()) {
                filePrices.setProperty(key, String.valueOf(authenticPrices.get(key)));
            }
            try (FileOutputStream out = new FileOutputStream(SHOP_FILE)) {
                filePrices.store(out, "DO NOT CHEAT THE PRICES LOL. THE GAME KNOWS.");
            } catch (IOException e) {}
        }
    }

    public static int getPrice(String key) {
        return authenticPrices.getOrDefault(key.toLowerCase(), 2500);
    }

    public static boolean isUnlocked(String key) {
        String k = key.toLowerCase();
        
        // 1. Free items are always unlocked (e.g., Moon Knight and Dark Spell 02)
        if (getPrice(k) == 0) return true; 
        
        // 2. Did the player buy this specific item directly?
        if (SaveManager.getInt("unlock_" + k) == 1) return true; 

        // 3. --- BUNDLE DEALS (Character Defaults) ---
        // If they own the Character, they automatically own their signature ability!
        if (k.equals("ability_ability_theworld") && isUnlocked("char_dio")) return true;
        if (k.equals("ability_ability_kingcrimson") && isUnlocked("char_diavolo")) return true;
        if (k.equals("ability_ability_mandom") && isUnlocked("char_ringo")) return true;

        // Note: Omnipotent Dio is basically a cheat character, so you can decide 
        // later if buying him unlocks literally everything. For now, we stick to the basics.

        return false; 
    }

    public static boolean buy(String key) {
        int price = getPrice(key);
        int money = SaveManager.getInt("money");
        if (money >= price && !isUnlocked(key)) {
            SaveManager.addInt("money", -price);
            SaveManager.setInt("unlock_" + key.toLowerCase(), 1);
            SaveManager.save();
            return true;
        }
        return false;
    }
}