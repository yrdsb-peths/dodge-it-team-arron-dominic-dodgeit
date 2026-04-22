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

    @Override
    public void enter(MyWorld world) {
        int midX = world.getWidth() / 2;

        // 1. Menu Background
        world.setBackground(new GreenfootImage("background_image.jpg"));
        world.getBackground().scale(GameConfig.WORLD_WIDTH+685, GameConfig.WORLD_HEIGHT+300);
        
        // 2. Dark Panel for the bottom half text UI
        addUI(world, new UI_Panel(world.getWidth(), GameConfig.s(160), new Color(0, 0, 0, 180)), midX, GameConfig.s(320));

        // 3. Static Text
        addUI(world, new UIText("SELECT YOUR CHARACTER", GameConfig.s(40), Color.YELLOW), midX, GameConfig.s(40));
        addUI(world, new UIText("< LEFT ARROW               RIGHT ARROW >", GameConfig.s(18), Color.CYAN), midX, GameConfig.s(260));
        addUI(world, new UIText("[ L : Learn Abilities ]", GameConfig.s(20), Color.GREEN), midX - GameConfig.s(130), GameConfig.s(370));
        addUI(world, new UIText("[ ENTER : START GAME ]", GameConfig.s(20), Color.RED), midX + GameConfig.s(130), GameConfig.s(370));

        // 4. Dynamic Text Elements
        nameDisplay = new UIText("", GameConfig.s(40), Color.WHITE);
        addUI(world, nameDisplay, midX, GameConfig.s(295));

        abilitiesDisplay = new UIText("", GameConfig.s(16), Color.ORANGE);
        addUI(world, abilitiesDisplay, midX, GameConfig.s(330));

        // 5. Spawn the first character (no slide on initial load)
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

        // 1. Play Voice Line (Using a failsafe for both Voice Pool and Single Audio)
        if (!isInitial) {
            AudioManager.playPool(selected.selectSoundKey);
            AudioManager.play(selected.selectSoundKey); 
        }

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