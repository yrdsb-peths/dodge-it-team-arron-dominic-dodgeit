import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

public class ShopState implements GameState {

    private List<Actor> uiElements = new ArrayList<>();
    private UIText moneyDisplay;
    private List<UIText> itemDisplays = new ArrayList<>();
    private int selectedIndex = 0;

    // Define everything for sale here
    private static class ShopItem {
        String key; String displayName;
        ShopItem(String k, String d) { key = k; displayName = d; }
    }

    private ShopItem[] inventory = {
        new ShopItem("char_dio", "Character: DIO"),
        new ShopItem("char_diavolo", "Character: Diavolo"),
        new ShopItem("char_ringo", "Character: Ringo Roadagain"),
        new ShopItem("char_omnipotentdio", "Character: Omnipotent DIO"),
        new ShopItem("ability_ability_darkspell01", "Ability: Dark Spell 01"),
        new ShopItem("ability_ability_theworld", "Ability: The World"),
        new ShopItem("ability_ability_madeinheaven", "Ability: Made in Heaven"),
        new ShopItem("ability_ability_standpunch", "Ability: Stand Punch"),
        new ShopItem("ability_ability_stickyfingers", "Ability: Sticky Fingers (Portals)")
    };

    @Override
    public void enter(MyWorld world) {
        int midX = world.getWidth() / 2;
        
        // Background Panel
        addUI(world, new UI_Panel(world.getWidth(), world.getHeight(), new Color(20, 20, 20)), midX, world.getHeight()/2);
        
        // Header
        addUI(world, new UIText("--- THE SHOP ---", GameConfig.s(40), Color.ORANGE), midX, GameConfig.s(40));
        moneyDisplay = new UIText("WALLET: $" + SaveManager.getInt("money"), GameConfig.s(25), Color.YELLOW);
        addUI(world, moneyDisplay, midX, GameConfig.s(80));

        // Generate the List
        int startY = GameConfig.s(130);
        int spacing = GameConfig.s(22);
        
        for (int i = 0; i < inventory.length; i++) {
            UIText itemText = new UIText("", GameConfig.s(18), Color.WHITE);
            itemDisplays.add(itemText);
            addUI(world, itemText, midX, startY + (i * spacing));
        }

        // Footer
        addUI(world, new UIText("[ UP/DOWN: Select ]   [ ENTER: Buy ]   [ ESC: Back ]", GameConfig.s(16), Color.CYAN), midX, world.getHeight() - GameConfig.s(25));

        updateUI();
    }

    @Override
    public void update(MyWorld world) {
        String key = Greenfoot.getKey();
        if (key == null) return;

        if (key.equals("escape")) {
            world.getGSM().changeState(new CharacterSelectState());
            return;
        }

        if (key.equals("down")) {
            selectedIndex = (selectedIndex + 1) % inventory.length;
            updateUI();
        } else if (key.equals("up")) {
            selectedIndex = (selectedIndex - 1 + inventory.length) % inventory.length;
            updateUI();
        } else if (key.equals("enter")) {
            ShopItem selected = inventory[selectedIndex];
            if (!ShopManager.isUnlocked(selected.key)) {
                if (ShopManager.buy(selected.key)) {
                    AudioManager.play("coin"); // Ca-ching!
                    updateUI(); // Refresh to show it as UNLOCKED
                } else {
                    System.out.println("Not enough money!");
                }
            }
        }
    }

    private void updateUI() {
        moneyDisplay.setText("WALLET: $" + SaveManager.getInt("money"));

        for (int i = 0; i < inventory.length; i++) {
            ShopItem item = inventory[i];
            boolean unlocked = ShopManager.isUnlocked(item.key);
            int price = ShopManager.getPrice(item.key);

            String prefix = (i == selectedIndex) ? "► " : "  ";
            String status = unlocked ? "[UNLOCKED]" : "[$" + price + "]";
            
            itemDisplays.get(i).setText(prefix + item.displayName + "  " + status);

            // Coloring logic
            if (i == selectedIndex) {
                itemDisplays.get(i).setColor(Color.YELLOW);
            } else if (unlocked) {
                itemDisplays.get(i).setColor(Color.GREEN);
            } else {
                itemDisplays.get(i).setColor(Color.LIGHT_GRAY);
            }
        }
    }

    @Override
    public void exit(MyWorld world) {
        world.removeObjects(uiElements);
        uiElements.clear();
    }

    private void addUI(MyWorld world, Actor a, int x, int y) {
        world.addObject(a, x, y);
        uiElements.add(a);
    }
}