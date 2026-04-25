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
        addPrice("char_diavolo", 800);
        addPrice("char_ringo", 1000);
        addPrice("char_omnipotent_dio", 5000);

        // --- ABILITY PRICES ---
        addPrice("ability_ability_darkspell02", 0); // Free
        addPrice("ability_ability_darkspell01", 300);
        addPrice("ability_ability_theworld", 400); 
        addPrice("ability_ability_kingcrimson", 600);
        addPrice("ability_ability_mandom", 600);
        addPrice("ability_ability_madeinheaven", 1000);
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
            System.out.println("Lol, nice try hacking the prices! Change them back! (I fixed them for you)");
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
        return authenticPrices.getOrDefault(key.toLowerCase(), 9999);
    }

    public static boolean isUnlocked(String key) {
        if (getPrice(key) == 0) return true; 
        return SaveManager.getInt("unlock_" + key.toLowerCase()) == 1; 
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