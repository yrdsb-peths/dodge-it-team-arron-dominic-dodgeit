import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

public class CharacterSelectState implements GameState {

    private List<Actor> uiElements = new ArrayList<>();
    private int currentIndex = 0;
    private CharacterConfig[] roster = CharacterConfig.values();

    private UIText nameDisplay;
    private UIText abilitiesDisplay;
    private UI_Preview currentPreview;

    // Prevents spamming the arrow keys and breaking the slide animation
    private int slideCooldown = 0;
    
    private String[] availableRoads = {"standard_road.png", "punk_road.png", "white_road.png", "red_road.png", "racetrack.png"};
    private String[] availableBgms  = {"dio_bgm", "gothic_bgm", "ringo_theme", "diavolo_theme"};
    private int roadIdx = 0;
    private int bgmIdx = 0;
    
    private UIText roadDisplay;
    private UIText bgmDisplay;


    @Override
    public void enter(MyWorld world) {
        int midX = world.getWidth() / 2;
    
        // 1. Menu Background
        world.setBackground(new GreenfootImage("background_image.jpg"));
        world.getBackground().scale(GameConfig.WORLD_WIDTH + 685, GameConfig.WORLD_HEIGHT + 300);
        
        // 2. Dark Panel - Moved slightly lower and made slightly taller to fit everything
        addUI(world, new UI_Panel(world.getWidth(), GameConfig.s(175), new Color(0, 0, 0, 180)), midX, GameConfig.s(325));
    
        // 3. Static Titles
        addUI(world, new UIText("SELECT YOUR CHARACTER", GameConfig.s(40), Color.BLACK), midX, GameConfig.s(40));
        addUI(world, new UIText("< LEFT               RIGHT >", GameConfig.s(18), Color.CYAN), midX, GameConfig.s(230));
    
        // 4. Customization Displays (Now inside the dark panel for clarity)
        roadDisplay = new UIText("ROAD: Default", GameConfig.s(16), Color.LIGHT_GRAY);
        bgmDisplay  = new UIText("MUSIC: Default", GameConfig.s(16), Color.LIGHT_GRAY);
        addUI(world, roadDisplay, midX, GameConfig.s(305));
        addUI(world, bgmDisplay, midX, GameConfig.s(320));
        
        // Help text for customization keys
        addUI(world, new UIText("[ R : Cycle Road ]    [ M : Cycle Music ]", GameConfig.s(14), Color.GRAY), midX, GameConfig.s(335));
    
        // 5. Dynamic Character Info
        nameDisplay = new UIText("", GameConfig.s(36), Color.WHITE);
        addUI(world, nameDisplay, midX, GameConfig.s(280));
    
        abilitiesDisplay = new UIText("", GameConfig.s(16), Color.ORANGE);
        addUI(world, abilitiesDisplay, midX, GameConfig.s(355));
    
        // 6. Navigation Buttons
        addUI(world, new UIText("[ L : Learn Abilities ]", GameConfig.s(20), Color.GREEN), midX - GameConfig.s(130), GameConfig.s(385));
        addUI(world, new UIText("[ ENTER : START GAME ]", GameConfig.s(20), Color.RED), midX + GameConfig.s(130), GameConfig.s(385));
    
        // 7. Spawn the first character (Moved slightly up to stay out of the panel)
        spawnNewCharacter(world, true, true);
    }

    @Override
    public void update(MyWorld world) {
        if (slideCooldown > 0) slideCooldown--;

        String key = Greenfoot.getKey();
        if (key != null && slideCooldown == 0) {
            
            if (key.equals("right")) {
                slideOutCurrent(true); // slide out to the left
                currentIndex = (currentIndex + 1) % roster.length;
                spawnNewCharacter(world, true, false); // spawn coming from right
                slideCooldown = 15;
                
            } else if (key.equals("left")) {
                slideOutCurrent(false); // slide out to the right
                currentIndex = (currentIndex - 1 + roster.length) % roster.length;
                spawnNewCharacter(world, false, false); // spawn coming from left
                slideCooldown = 15;
                
            } else if (key.equals("l")) {
                GameConfig.ACTIVE_CHARACTER = roster[currentIndex];
                world.getGSM().pushState(new AbilityDisplayState());
                
            } else if (key.equals("enter")) {
                GameConfig.ACTIVE_CHARACTER = roster[currentIndex];
                AudioManager.stopAll();
                world.getGSM().changeState(new PlayingState());
            }
            if (key.equals("escape")) {
                world.getGSM().changeState(new MenuState());
                return;
            }
            if (key.equals("r")) {
                roadIdx = (roadIdx + 1) % availableRoads.length;
                GameConfig.SESSION_ROAD = availableRoads[roadIdx];
                roadDisplay.setText("ROAD: " + GameConfig.SESSION_ROAD);
                roadDisplay.setColor(Color.CYAN);
            } 
            else if (key.equals("m")) {
                bgmIdx = (bgmIdx + 1) % availableBgms.length;
                GameConfig.SESSION_BGM = availableBgms[bgmIdx];
                bgmDisplay.setText("MUSIC: " + GameConfig.SESSION_BGM);
                bgmDisplay.setColor(Color.MAGENTA);
            }
        }
    }

    /** Tells the current portrait to exit the screen. */
    private void slideOutCurrent(boolean toLeft) {
        if (currentPreview != null) {
            currentPreview.slideOut(toLeft);
        }
    }

    /** Creates the new portrait, plays sound, and updates text. */
    private void spawnNewCharacter(MyWorld world, boolean fromRight, boolean isInitial) {
        CharacterConfig selected = roster[currentIndex];

        // 1. Stop any lingering voices first so they don't stack
        AudioManager.stopAllPools(); 

        // 2. Play the selection voice line immediately
        // (Checking both pools and single sounds for safety)
        AudioManager.playPool(selected.selectSoundKey);
        AudioManager.play(selected.selectSoundKey); 

        // 2. Spawn Sliding Preview Image
        currentPreview = new UI_Preview(selected, fromRight, world.getWidth() / 2);
        world.addObject(currentPreview, currentPreview.getStartX(), GameConfig.s(145));

        // 3. Update UI Text Details
        nameDisplay.setText(selected.displayName);
        String abilityText = "Abilities: ";
        for (String s : selected.abilityClassNames) {
            abilityText += s.replace("Ability_", "") + "  ";
        }
        abilitiesDisplay.setText(abilityText);
    }

    @Override
    public void exit(MyWorld world) {
        world.removeObjects(uiElements);
        if (currentPreview != null) world.removeObject(currentPreview);
        uiElements.clear();
    }

    private void addUI(MyWorld world, Actor a, int x, int y) {
        world.addObject(a, x, y);
        uiElements.add(a);
    }
}