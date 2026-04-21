/*
 * ─────────────────────────────────────────────────────────────────────────────
 * CharacterSelectState.java  —  CHARACTER SELECTION SCREEN
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   Shows the roster of characters (from CharacterConfig.values()),
 *   lets the player browse with LEFT/RIGHT arrows, displays the character's
 *   name, abilities, and a spinning preview animation.
 *   Pressing ENTER sets GameConfig.ACTIVE_CHARACTER and starts PlayingState.
 *
 * How the roster is driven:
 *   CharacterConfig.values() returns all defined enum constants.
 *   currentIndex tracks which one is selected.  LEFT/RIGHT wrap around.
 *   updateScreen() refreshes the name display, ability list, and preview sprite.
 *
 * UI_Preview management:
 *   currentPreview is NOT in the uiElements list.  It is removed manually
 *   in updateScreen() each time the player switches characters (so it is
 *   replaced, not accumulated) and in exit() for final cleanup.
 *
 * Interacts with:
 *   CharacterConfig (roster data), GameConfig (sets ACTIVE_CHARACTER),
 *   PlayingState (next state), UIText, UI_Preview, GameStateManager
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

public class CharacterSelectState implements GameState {

    /** All static UI actors — removed together in exit(). */
    private List<Actor> uiElements = new ArrayList<>();

    /** Index into CharacterConfig.values() for the currently highlighted character. */
    private int currentIndex = 0;

    /** The full roster of available characters. */
    private CharacterConfig[] roster = CharacterConfig.values();

    /** Dynamic text label updated when the selection changes. */
    private UIText nameDisplay;
    /** Dynamic text label listing the selected character's abilities. */
    private UIText abilitiesDisplay;

    /**
     * The animated sprite preview for the selected character.
     * Managed separately from uiElements because it is replaced on every
     * character switch, not just at exit.
     */
    private UI_Preview currentPreview;

    @Override
    public void enter(MyWorld world) {
        int midX = world.getWidth() / 2;

        addUI(world, new UIText("SELECT YOUR CHARACTER", GameConfig.s(40), Color.YELLOW), midX, GameConfig.s(80));
        addUI(world, new UIText("< LEFT ARROW       RIGHT ARROW >", GameConfig.s(20), Color.CYAN), midX, GameConfig.s(240));

        // Name and ability labels start empty — updateScreen() fills them.
        nameDisplay = new UIText("", GameConfig.s(45), Color.WHITE);
        addUI(world, nameDisplay, midX, GameConfig.s(220));

        abilitiesDisplay = new UIText("", GameConfig.s(18), Color.ORANGE);
        addUI(world, abilitiesDisplay, midX, GameConfig.s(260));

        updateScreen(world); // populate with the first character's data
    }

    @Override
    public void update(MyWorld world) {
        String key = Greenfoot.getKey();
        if (key != null) {
            if (key.equals("right")) {
                // Wrap forward through the roster
                currentIndex = (currentIndex + 1) % roster.length;
                updateScreen(world);
            } else if (key.equals("left")) {
                // Wrap backward through the roster
                currentIndex = (currentIndex - 1 + roster.length) % roster.length;
                updateScreen(world);
            } else if (key.equals("enter")) {
                GameConfig.ACTIVE_CHARACTER = roster[currentIndex];
                world.getGSM().changeState(new PlayingState());
            }
        }
    }

    /**
     * Refreshes all dynamic UI to reflect the currently selected character:
     *   1. Updates the name label.
     *   2. Replaces the animated preview sprite.
     *   3. Updates the ability list text.
     */
    private void updateScreen(MyWorld world) {
        CharacterConfig selected = roster[currentIndex];

        // 1. Name
        nameDisplay.setText(selected.displayName);

        // 2. Animated preview (remove old, add new)
        if (currentPreview != null) {
            world.removeObject(currentPreview);
        }
        currentPreview = new UI_Preview(selected);
        world.addObject(currentPreview, world.getWidth() / 2, GameConfig.s(140));
        // NOT added to uiElements — managed manually here and in exit()

        // 3. Abilities (strip the "Ability_" prefix for cleaner display)
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
