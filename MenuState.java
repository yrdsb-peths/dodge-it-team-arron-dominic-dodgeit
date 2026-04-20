import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

public class MenuState implements GameState {
    private List<Actor> uiElements = new ArrayList<>();

    public void enter(MyWorld world) {
        int middle = world.getWidth() / 2;
        
        // 1. Header
        addUI(world, new UIText("DIO-DGE IT", GameConfig.s(55), Color.YELLOW), middle, GameConfig.s(60));
        addUI(world, new UIText("The World is your playground.", GameConfig.s(18), Color.WHITE), middle, GameConfig.s(95));

        // 2. Control Layout (Left Aligned for readability)
        int leftAlign = middle - GameConfig.s(120);
        int rowStart = GameConfig.s(150);
        int spacing = GameConfig.s(35);

        addUI(world, new UIText("ARROWS : Move Dio", GameConfig.s(22), Color.WHITE), leftAlign, rowStart);
        
        // Explain 'W' - Time Stop
        addUI(world, new UIText("W : THE WORLD (Stop Time)", GameConfig.s(22), new Color(200, 255, 0)), leftAlign, rowStart + spacing);
        
        // Explain 'R' - Mandom (Rewind)
        addUI(world, new UIText("R : MANDOM (Rewind 2s)", GameConfig.s(22), new Color(100, 200, 255)), leftAlign, rowStart + spacing * 2);
        
        // Explain 'S' - Made in Heaven (Speed)
        addUI(world, new UIText("S : MADE IN HEAVEN (Speed)", GameConfig.s(22), new Color(255, 100, 255)), leftAlign, rowStart + spacing * 3);
        
        addUI(world, new UIText("E : Summon Stand (Fights for You)", GameConfig.s(22), new Color(255, 100, 255)), leftAlign, rowStart + spacing * 4);

        // 3. Hint/Objective
        addUI(world, new UIText("Dodge the Road Rollers and Ambulances!", GameConfig.s(18), Color.ORANGE), middle, GameConfig.s(330));

        // 4. Start Prompt (Flashing Cyan)
        addUI(world, new UIText("[ Press ENTER to Begin ]", GameConfig.s(24), Color.CYAN), middle, GameConfig.s(375));
    }

    public void update(MyWorld world) {
        if ("enter".equals(Greenfoot.getKey())) {
            world.getGSM().changeState(world.playingState);
        }
    }

    public void exit(MyWorld world) {
        world.removeObjects(uiElements);
        uiElements.clear();
    }

    private void addUI(MyWorld world, Actor a, int x, int y) {
        // We adjust x slightly for the left-aligned text so the center of the block is aligned
        int drawX = (a instanceof UIText && x < world.getWidth()/2) ? x + GameConfig.s(120) : x;
        world.addObject(a, drawX, y);
        uiElements.add(a);
    }
}