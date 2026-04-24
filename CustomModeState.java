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
    
    @Override
    public void enter(MyWorld world) {
        world.setBackground(new GreenfootImage("background_image.jpg"));
        world.getBackground().scale(GameConfig.WORLD_WIDTH+685, GameConfig.WORLD_HEIGHT+300);
        
        int midX = world.getWidth() / 2;
        addUI(world, new UI_Panel(world.getWidth(), world.getHeight(), new Color(0, 0, 0, 200)), midX, world.getHeight() / 2);
        
        addUI(world, new UIText("CUSTOM MODE", GameConfig.s(26), Color.CYAN), midX, GameConfig.s(30));
        addUI(world, new UIText("[ ESC : Back ]", GameConfig.s(14), Color.WHITE), GameConfig.s(60), GameConfig.s(20));
        addUI(world, new UIText("Use UP/DOWN to select, LEFT/RIGHT or ENTER to change", GameConfig.s(14), Color.LIGHT_GRAY), midX, GameConfig.s(60));

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
                world.getGSM().changeState(new MenuState());
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
                if (selectedIndex >= 3 && selectedIndex <= 10) {
                    abilitySelected[selectedIndex - 3] = !abilitySelected[selectedIndex - 3];
                    updateDisplay();
                } else if (selectedIndex == 11) {
                    startGame(world);
                }
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
            abilitySelected[selectedIndex - 3] = !abilitySelected[selectedIndex - 3];
        }
        updateDisplay();
    }

    private void updateDisplay() {
        menuTexts[0].setText("Sprite: < " + bases[baseIndex].displayName + " >");
        menuTexts[1].setText("Road: < " + roads[roadIndex] + " >");
        menuTexts[2].setText("Music: < " + bgms[bgmIndex] + " >");
        
        for(int i=0; i<8; i++) {
            String box = abilitySelected[i] ? "[X] " : "[ ] ";
            menuTexts[i+3].setText(box + abilityNames[i]);
        }
        
        menuTexts[11].setText(">> START GAME <<");
        
        for(int i=0; i<12; i++) {
            if(i == selectedIndex) {
                menuTexts[i].setColor(Color.YELLOW);
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