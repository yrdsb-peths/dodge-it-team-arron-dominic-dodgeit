
import greenfoot.*;
import java.util.ArrayList;
import java.util.List;
public class CharacterSelectState implements GameState {
    private List<Actor> uiElements = new ArrayList<>();
    private int currentIndex = 0;
    private CharacterConfig[] roster = CharacterConfig.values();
    
    private UIText nameDisplay;
    private UIText abilitiesDisplay;
    private UI_Preview currentPreview; // <--- ADD THIS

    public void enter(MyWorld world) {
        int midX = world.getWidth() / 2;
        
        // Title
        addUI(world, new UIText("SELECT YOUR CHARACTER", GameConfig.s(40), Color.YELLOW), midX, GameConfig.s(80));
        
        // Character Name (Starts blank, we update it immediately)
        nameDisplay = new UIText("", GameConfig.s(60), Color.WHITE);
        addUI(world, nameDisplay, midX, GameConfig.s(180));
        
        // Instructions
        addUI(world, new UIText("< LEFT ARROW       RIGHT ARROW >", GameConfig.s(20), Color.CYAN), midX, GameConfig.s(240));


        // Name Display
        nameDisplay = new UIText("", GameConfig.s(45), Color.WHITE);
        addUI(world, nameDisplay, midX, GameConfig.s(220)); // Moved down a bit
        
        // Abilities Display
        abilitiesDisplay = new UIText("", GameConfig.s(18), Color.ORANGE);
        addUI(world, abilitiesDisplay, midX, GameConfig.s(260));

        updateScreen(world); // Pass the world so we can add the preview
    }

    public void update(MyWorld world) {
        String key = Greenfoot.getKey();
        if (key != null) {
            if (key.equals("right")) {
                currentIndex = (currentIndex + 1) % roster.length;
                updateScreen(world);
            } 
            else if (key.equals("left")) {
                currentIndex = (currentIndex - 1 + roster.length) % roster.length;
                updateScreen(world);
            }
            else if (key.equals("enter")) {
                GameConfig.ACTIVE_CHARACTER = roster[currentIndex];
                world.getGSM().changeState(new PlayingState());
            }
        }
    }

    private void updateScreen(MyWorld world) {
        CharacterConfig selected = roster[currentIndex];
        
        // 1. Update Text
        nameDisplay.setText(selected.displayName); // Use the new Stage Name!
        
        // 2. Update the Image Preview
        if (currentPreview != null) {
            world.removeObject(currentPreview);
        }
        currentPreview = new UI_Preview(selected);
        world.addObject(currentPreview, world.getWidth()/2, GameConfig.s(140));
        // We don't add currentPreview to uiElements list because 
        // we manually manage its removal when switching characters
        
        // 3. Update Abilities Text
        String abilityText = "Abilities: ";
        for (String s : selected.abilityClassNames) {
            abilityText += s.replace("Ability_", "") + " ";
        }
        abilitiesDisplay.setText(abilityText);
    }

    public void exit(MyWorld world) {
        world.removeObjects(uiElements);
        if (currentPreview != null) world.removeObject(currentPreview); // Clean up
        uiElements.clear();
    }

    private void addUI(MyWorld world, Actor a, int x, int y) {
        world.addObject(a, x, y);
        uiElements.add(a);
    }
}