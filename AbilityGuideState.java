import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

public class AbilityGuideState implements GameState {
    private List<Actor> ui = new ArrayList<>();
    private Class<? extends Ability> abilityClass;
    private AbilityDisplayState parentState; // Reference to show UI again on exit

    public AbilityGuideState(Class<? extends Ability> abilityClass, AbilityDisplayState parent) {
        this.abilityClass = abilityClass;
        this.parentState = parent;
    }

    @Override
    public void enter(MyWorld world) {
        int midX = world.getWidth() / 2;
        int midY = world.getHeight() / 2;

        // 1. FULL DIM: Covers the entire screen to hide cars/road clearly
        addUI(world, new FX_DimOverlay(world.getWidth(), world.getHeight()), midX, midY);

        // 2. CENTER PANEL: This is where the text lives
        addUI(world, new UI_Panel(world.getWidth() - GameConfig.s(60), world.getHeight() - GameConfig.s(60), new Color(10, 10, 15, 240)), midX, midY);

        // 3. THE GUIDE TEXT: Positioned inside the panel
        String text = AbilityGuideContent.get(abilityClass);
        UIText manualText = new UIText(text, GameConfig.s(16), Color.WHITE);
        addUI(world, manualText, midX, midY - GameConfig.s(20));

        // 4. THE ONLY BUTTON: Placed at the bottom of the panel
        addUI(world, new UIText("[ ESC : Return to Sandbox ]", GameConfig.s(20), Color.CYAN), midX, world.getHeight() - GameConfig.s(20));
    }

    @Override
    public void update(MyWorld world) {
        if ("escape".equals(Greenfoot.getKey())) {
            world.getGSM().popState(); 
        }
    }

    @Override
    public void exit(MyWorld world) {
        world.removeObjects(ui);
        // RESTORE the Sandbox UI when the manual closes!
        if (parentState != null) {
            parentState.setSandboxUIVisible(true);
        }
    }

    private void addUI(MyWorld world, Actor a, int x, int y) {
        world.addObject(a, x, y);
        ui.add(a);
    }
}