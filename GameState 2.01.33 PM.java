/*
 * ─────────────────────────────────────────────────────────────────────────────
 * GameState.java  —  THE CONTRACT ALL GAME STATES MUST FOLLOW
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A Java "interface" is a contract.  Any class that says
 *   "implements GameState" MUST provide all three methods below,
 *   or the code will not compile.
 *
 * The three-method lifecycle mirrors how a real game state works:
 *   enter()  — "Set up the scene."  Called once when the state becomes active.
 *   update() — "Run the scene."  Called every frame (~60 times/sec) while active.
 *   exit()   — "Clean up the scene."  Called once when the state is removed.
 *
 * Current implementations:
 *   MenuState, CharacterSelectState, PlayingState, PausedState, GameOverState
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public interface GameState {

    /**
     * Called ONCE when this state is pushed onto the GameStateManager stack.
     * Use this to: add UI actors, start music, reset variables, spawn the player.
     *
     * @param world  The game world; use it to add/remove actors.
     */
    void enter(MyWorld world);

    /**
     * Called EVERY FRAME (~60 times/sec) while this state is the top of the stack.
     * This is the main logic loop for the state.
     * Handle input, move things, check collisions — all here.
     *
     * @param world  The game world; use it to add/remove actors or query state.
     */
    void update(MyWorld world);

    /**
     * Called ONCE when this state is popped off the GameStateManager stack.
     * Use this to: remove actors, stop music, save data.
     * Always clean up what enter() created.
     *
     * @param world  The game world; use it to remove actors.
     */
    void exit(MyWorld world);
}
