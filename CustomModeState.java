import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

public class CustomModeState implements GameState {
    private List<Actor> uiElements = new ArrayList<>();
    private int selectedIndex = 0;
    private int slideCooldown = 0;

    private int baseIndex = 0;
    private int roadIndex = 0;
    private int bgmIndex = 0;
    private boolean[] abilitySelected = new boolean[8];

    private CharacterConfig[] bases = {
        CharacterConfig.MoonKnight, CharacterConfig.DIO, 
        CharacterConfig.Ringo, CharacterConfig.DIAVOLO
    };
    
    private String[] roads = {
        "white_road.png", "red_road.png", 
        "standard_road.png", "punk_road.png","racetrack.png"
    };
    
    private String[] bgms = {
        "dio_bgm", "gothic_bgm", 
        "ringo_theme", "diavolo_theme","sbr_theme"
    };
    
    private String[] abilityClasses = {
        "Ability_TheWorld", "Ability_StandPunch", "Ability_MadeInHeaven", 
        "Ability_Mandom", "Ability_StickyFingers", "Ability_KingCrimson", 
        "Ability_DarkSpell01", "Ability_DarkSpell02"
    };
    
    private String[] abilityNames = {
        "The World", "Stand Punch", "Made In Heaven", 
        "Mandom", "Sticky Fingers", "King Crimson", 
        "Dark Spell 01", "Dark Spell 02"
    };

    private UIText[] menuTexts = new UIText[12];
    private UIText moneyDisplay;
    
    @Override
    public void enter(MyWorld world) {
        world.setBackground(new GreenfootImage("background_image.jpg"));
        world.getBackground().scale(GameConfig.WORLD_WIDTH+685, GameConfig.WORLD_HEIGHT+300);
        
        int midX = world.getWidth() / 2;
        addUI(world, new UI_Panel(world.getWidth(), world.getHeight(), new Color(0, 0, 0, 200)), midX, world.getHeight() / 2);
        
        addUI(world, new UIText("CUSTOM CREATOR", GameConfig.s(26), Color.CYAN), midX, GameConfig.s(30));
        addUI(world, new UIText("[ ESC : Back ]", GameConfig.s(14), Color.WHITE), GameConfig.s(60), GameConfig.s(20));
        addUI(world, new UIText("Select abilities you OWN to build your ultimate character.", GameConfig.s(14), Color.LIGHT_GRAY), midX, GameConfig.s(60));

        // Wallet display so they can buy Custom Mode
        moneyDisplay = new UIText("WALLET: $" + SaveManager.getInt("money"), GameConfig.s(16), Color.YELLOW);
        addUI(world, moneyDisplay, world.getWidth() - GameConfig.s(80), GameConfig.s(20));

        int startY = GameConfig.s(100);
        int spacing = GameConfig.s(20); 
        
        for(int i=0; i<12; i++) {
            menuTexts[i] = new UIText("", GameConfig.s(16), Color.WHITE);
            addUI(world, menuTexts[i], midX, startY + (i * spacing));
        }
        
        updateDisplay();
    }

    @Override
    public void update(MyWorld world) {
        if (slideCooldown > 0) slideCooldown--;
        
        String key = Greenfoot.getKey();
        if (key != null && slideCooldown == 0) {
            if (key.equals("escape")) {
                world.getGSM().changeState(new MenuState()); // Or CharacterSelectState
                return;
            }
            
            if (key.equals("down")) {
                selectedIndex = (selectedIndex + 1) % 12;
                updateDisplay();
            } else if (key.equals("up")) {
                selectedIndex = (selectedIndex - 1 + 12) % 12;
                updateDisplay();
            } else if (key.equals("right")) {
                adjustValue(1);
            } else if (key.equals("left")) {
                adjustValue(-1);
            } else if (key.equals("enter") || key.equals("space")) {
                handleEnter(world);
            }
            slideCooldown = 10;
        }
    }
    
    private void adjustValue(int dir) {
        if (selectedIndex == 0) {
            baseIndex = (baseIndex + dir + bases.length) % bases.length;
        } else if (selectedIndex == 1) {
            roadIndex = (roadIndex + dir + roads.length) % roads.length;
        } else if (selectedIndex == 2) {
            bgmIndex = (bgmIndex + dir + bgms.length) % bgms.length;
        } else if (selectedIndex >= 3 && selectedIndex <= 10) {
            toggleAbility(selectedIndex - 3);
        }
        updateDisplay();
    }

    private void handleEnter(MyWorld world) {
        if (selectedIndex >= 3 && selectedIndex <= 10) {
            toggleAbility(selectedIndex - 3);
        } else if (selectedIndex == 11) {
            // THE START / BUY LOGIC
            if (ShopManager.isUnlocked("char_custom")) {
                startGame(world);
            } else {
                if (ShopManager.buy("char_custom")) {
                    AudioManager.playPool("buy_success_sound");
                    moneyDisplay.setText("WALLET: $" + SaveManager.getInt("money"));
                    updateDisplay();
                } else {
                    AudioManager.playPool("error_buzzer"); // Not enough money!
                }
            }
        }
    }

    private void toggleAbility(int abIndex) {
        String shopKey = "ability_" + abilityClasses[abIndex];
        // Only allow checking the box if they own the ability!
        if (ShopManager.isUnlocked(shopKey)) {
            abilitySelected[abIndex] = !abilitySelected[abIndex];
            updateDisplay();
        } else {
            AudioManager.playPool("error_buzzer"); // Denied
        }
    }

    private void updateDisplay() {
        menuTexts[0].setText("Sprite: < " + bases[baseIndex].displayName + " >");
        menuTexts[1].setText("Road: < " + roads[roadIndex] + " >");
        menuTexts[2].setText("Music: < " + bgms[bgmIndex] + " >");
        
        // Render Abilities
        for(int i=0; i<8; i++) {
            String shopKey = "ability_" + abilityClasses[i];
            if (ShopManager.isUnlocked(shopKey)) {
                String box = abilitySelected[i] ? "[X] " : "[ ] ";
                menuTexts[i+3].setText(box + abilityNames[i]);
            } else {
                // If they don't own it, show it locked and force uncheck
                abilitySelected[i] = false; 
                menuTexts[i+3].setText("- LOCKED: " + abilityNames[i] + " -");
            }
        }
        
        // Render Bottom Button
        if (ShopManager.isUnlocked("char_custom")) {
            menuTexts[11].setText(">> START GAME <<");
        } else {
            int price = ShopManager.getPrice("char_custom");
            menuTexts[11].setText(">> BUY CUSTOM MODE ($" + price + ") <<");
        }
        
        // Apply Colors
        for(int i=0; i<12; i++) {
            if (i == selectedIndex) {
                menuTexts[i].setColor(Color.YELLOW);
            } else if (i >= 3 && i <= 10 && !ShopManager.isUnlocked("ability_" + abilityClasses[i-3])) {
                // Dim locked abilities
                menuTexts[i].setColor(Color.DARK_GRAY);
            } else if (i == 11 && !ShopManager.isUnlocked("char_custom")) {
                // Keep the Buy button visible even when not selected
                menuTexts[i].setColor(Color.ORANGE);
            } else {
                menuTexts[i].setColor(Color.WHITE);
            }
        }
    }

    private void startGame(MyWorld world) {
        CharacterConfig base = bases[baseIndex];
        CharacterConfig.CUSTOM.displayName = "Custom " + base.displayName;
        CharacterConfig.CUSTOM.folderName = base.folderName;
        CharacterConfig.CUSTOM.portraitImage = base.portraitImage;
        CharacterConfig.CUSTOM.roadImage = roads[roadIndex];
        CharacterConfig.CUSTOM.animNames = base.animNames;
        CharacterConfig.CUSTOM.defaultAnim = base.defaultAnim;
        CharacterConfig.CUSTOM.moveSpeed = base.moveSpeed;
        CharacterConfig.CUSTOM.scale = base.scale;
        CharacterConfig.CUSTOM.bgmKey = bgms[bgmIndex];
        CharacterConfig.CUSTOM.deathSoundKey = base.deathSoundKey;
        CharacterConfig.CUSTOM.selectSoundKey = base.selectSoundKey;
        CharacterConfig.CUSTOM.bossConfig = base.bossConfig;
        
        List<String> chosenAbilities = new ArrayList<>();
        for(int i=0; i<8; i++) {
            if(abilitySelected[i]) chosenAbilities.add(abilityClasses[i]);
        }
        CharacterConfig.CUSTOM.abilityClassNames = chosenAbilities.toArray(new String[0]);
        
        GameConfig.ACTIVE_CHARACTER = CharacterConfig.CUSTOM;
        AudioManager.stopAll();
        world.getGSM().changeState(new PlayingState());
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