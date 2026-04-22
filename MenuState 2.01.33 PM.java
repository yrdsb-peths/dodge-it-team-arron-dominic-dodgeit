/*
 * ─────────────────────────────────────────────────────────────────────────────
 * MenuState.java  —  THE MAIN MENU
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   The first state shown when the project starts.
 *   Displays the game title, control instructions, and a "Press ENTER" prompt.
 *   Pressing ENTER switches to CharacterSelectState.
 *
 * Layout strategy:
 *   Most text is centre-aligned (x = world.getWidth()/2).
 *   Control instructions are left-aligned relative to the centre:
 *   addUI places them at leftAlign + GameConfig.s(120) so the TEXT BLOCK
 *   as a whole appears centred even though individual lines start from a
 *   consistent left edge.
 *
 * Interacts with:
 *   GameStateManager (changeState), CharacterSelectState (next state),
 *   UIText (text labels), GameConfig (scaling)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

public class MenuState implements GameState {

    /** All actors created by this state — removed in exit(). */
    private List<Actor> uiElements = new ArrayList<>();

    @Override
    public void enter(MyWorld world) {
        int middle = world.getWidth() / 2;

        // ── Title ─────────────────────────────────────────────────────────────
        addUI(world, new UIText("DIO-DGE IT",                      GameConfig.s(55), Color.YELLOW),         middle,          GameConfig.s(60));
        addUI(world, new UIText("The World is your playground.",   GameConfig.s(18), Color.WHITE),           middle,          GameConfig.s(95));

        // ── Controls (left-aligned block, visually centred) ───────────────────
        int leftAlign = middle - GameConfig.s(120);
        int rowStart  = GameConfig.s(150);
        int spacing   = GameConfig.s(35);

        addUI(world, new UIText("ARROWS : Move Dio",                            GameConfig.s(22), Color.WHITE),               leftAlign, rowStart);
        addUI(world, new UIText("W : THE WORLD (Stop Time)",                    GameConfig.s(22), new Color(200, 255, 0)),     leftAlign, rowStart + spacing);
        addUI(world, new UIText("R : MANDOM (Rewind 2s)",                       GameConfig.s(22), new Color(100, 200, 255)),   leftAlign, rowStart + spacing * 2);
        addUI(world, new UIText("S : MADE IN HEAVEN (Speed)",                   GameConfig.s(22), new Color(255, 100, 255)),   leftAlign, rowStart + spacing * 3);
        addUI(world, new UIText("E : Summon Stand (Fights for You)",             GameConfig.s(22), new Color(255, 100, 255)),   leftAlign, rowStart + spacing * 4);

        // ── Objective ─────────────────────────────────────────────────────────
        addUI(world, new UIText("Dodge the Road Rollers and Ambulances!", GameConfig.s(18), Color.ORANGE), middle, GameConfig.s(330));

        // ── Start prompt ──────────────────────────────────────────────────────
        addUI(world, new UIText("[ Press ENTER to Begin ]",               GameConfig.s(24), Color.CYAN),   middle, GameConfig.s(375));
    }

    /** Waits for ENTER, then transitions to the character select screen. */
    @Override
    public void update(MyWorld world) {
        if ("enter".equals(Greenfoot.getKey())) {
            world.getGSM().changeState(new CharacterSelectState());
        }
    }

    /** Removes all text labels when leaving the menu. */
    @Override
    public void exit(MyWorld world) {
        world.removeObjects(uiElements);
        uiElements.clear();
    }

    /**
     * Adds an actor to the world and tracks it for cleanup.
     * Left-aligned text (x < world centre) is shifted right by GameConfig.s(120)
     * so the text block appears visually centred as a group.
     */
    private void addUI(MyWorld world, Actor a, int x, int y) {
        int drawX = (a instanceof UIText && x < world.getWidth() / 2)
            ? x + GameConfig.s(120) : x;
        world.addObject(a, drawX, y);
        uiElements.add(a);
    }
}
