/*
 * ─────────────────────────────────────────────────────────────────────────────
 * PausedState.java  —  THE TIME-STOP PAUSE STATE
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   Pushed ON TOP of PlayingState when the player presses W (time-stop).
 *   While this state is on top, every actor's act() checks isState(PlayingState)
 *   and returns immediately — they all freeze.
 *
 * How "time-stop" works technically:
 *   PausedState is pushed; PlayingState stays frozen underneath.
 *   All GameTimers (abilities, etc.) check isState(PlayingState) in update() —
 *   since the top state is PausedState, ALL timers freeze automatically.
 *   Pressing W pops PausedState → PlayingState resumes from exactly where it stopped.
 *
 * Special exception — Dio during pause:
 *   Player.act() is still called by Greenfoot even during PausedState.
 *   It detects the PausedState and calls onPauseUpdate(), which Dio overrides
 *   to play his boss banner animation and "Wry" pose.
 *
 * Interacts with:
 *   GameStateManager (pushed/popped), PlayingState (frozen underneath),
 *   Dio (onPauseUpdate hook), UIText (PAUSED label)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

public class PausedState implements GameState {

    /** Actors added by this state (cleaned up in exit()). */
    private List<Actor> uiElements = new ArrayList<>();

    /**
     * Adds the "PAUSED" label to the screen.
     * Everything else is frozen because isState(PlayingState.class) returns false.
     */
    @Override
    public void enter(MyWorld world) {
        UIText title = new UIText("PAUSED", GameConfig.s(80), Color.RED);
        addUI(world, title, world.getWidth() / 2, GameConfig.s(150));
    }

    /**
     * Handles input during the pause.
     * Pressing W again pops this state, resuming PlayingState.
     * (Any other keys are ignored — the game is truly frozen.)
     */
    @Override
    public void update(MyWorld world) {
        if ("w".equals(Greenfoot.getKey())) {
            world.getGSM().popState(); // remove PausedState → PlayingState resumes
        }
    }

    /** Removes the "PAUSED" label when unpausing. */
    @Override
    public void exit(MyWorld world) {
        world.removeObjects(uiElements);
    }

    private void addUI(MyWorld world, Actor a, int x, int y) {
        world.addObject(a, x, y);
        uiElements.add(a);
    }
}
