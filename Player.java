/*
 * ─────────────────────────────────────────────────────────────────────────────
 * Player.java  —  THE ABSTRACT BASE CLASS FOR ALL PLAYABLE CHARACTERS
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   An "abstract" class is a blueprint that cannot be instantiated directly.
 *   You can never write "new Player()".  Instead, concrete subclasses like
 *   GenericPlayer extend it and provide real implementations.
 *
 * What Player provides:
 *   - The act() method that Greenfoot calls each frame.  Player decides WHEN
 *     to call movementLogic() and animationLogic() (only in PlayingState).
 *   - The isDead flag (shared by all characters).
 *   - The bannerSpawned flag (used by Dio during PausedState).
 *   - onPauseUpdate() — a hook that does nothing by default but Dio overrides
 *     to play the boss intro banner during time-stop.
 *
 * Abstract methods (subclasses MUST implement these):
 *   - die()                              — handle death
 *   - movementLogic()                    — movement each frame
 *   - animationLogic()                   — sprite update each frame
 *   - checkCustomHitbox(attacker, pad)   — custom collision check
 *   - setAnimation(name)                 — switch animation by name
 *   - setAnimation(name, speed)          — switch animation with custom speed
 *   - startIFrame(seconds)               — grant temporary invincibility
 *
 * Interacts with:
 *   GenericPlayer (concrete implementation), Dio (override of onPauseUpdate),
 *   GameStateManager (checks PlayingState / PausedState)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public abstract class Player extends Actor {

    /**
     * Tracks whether this player has died.
     * Protected so subclasses can read and set it directly.
     * true → death animation plays and GameOverState will be entered.
     */
    protected boolean isDead = false;

    /**
     * Prevents the boss banner from spawning more than once per time-stop session.
     * Reset to false every frame in PlayingState so it can trigger again next pause.
     * Only meaningful for characters with a bossConfig (currently only Dio).
     */
    protected boolean bannerSpawned = false;

    // ─────────────────────────────────────────────────────────────────────────
    // GREENFOOT LOOP — the entry point for all player logic
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called by Greenfoot each frame.  Player decides when to act:
     *   - In PlayingState: call movementLogic() and animationLogic().
     *   - In PausedState: call onPauseUpdate() (hook for special pause behaviour).
     *   - In any other state (Menu, GameOver, etc.): do nothing.
     */
    @Override
    public void act() {
        MyWorld world = (MyWorld) getWorld();
        if (world == null) return;

        if (!world.getGSM().isState(IActiveGameState.class)) {
            // We are NOT in the main game — check for the special pause case.
            // We are NOT in the main game — check for the special pause case.
            if (world.getGSM().isState(PausedState.class)) {
                onPauseUpdate(world);
            }
            return; // do nothing in menus, game-over, etc.
        }

        // We ARE in PlayingState — allow the banner to play again next pause.
        bannerSpawned = false;

        movementLogic();
        animationLogic();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ABSTRACT METHODS — every concrete character must implement all of these
    // ─────────────────────────────────────────────────────────────────────────

    /** Handle the player dying (play sound, start death timer, switch animation). */
    public abstract void die();

    /** Move the player based on input; update ability timers; handle death timer. */
    protected abstract void movementLogic();

    /** Update the current animation frame; handle i-frame blinking. */
    protected abstract void animationLogic();

    /**
     * Custom circular hitbox check — more forgiving than the default rectangle.
     *
     * @param attacker  The obstacle actor that may be hitting us.
     * @param padding   A scale factor for the hitbox size.  <1 = smaller (more forgiving).
     * @return          True if the attacker is close enough to count as a hit.
     */
    public abstract boolean checkCustomHitbox(Actor attacker, double padding);

    /** Switch to the named animation immediately, restarting from frame 0. */
    public abstract void setAnimation(String name);

    /** Switch to the named animation at a custom frame speed. */
    public abstract void setAnimation(String name, int speed);

    /** Grant the player invincibility for the given number of seconds (i-frames). */
    public abstract void startIFrame(double seconds);

    // ─────────────────────────────────────────────────────────────────────────
    // QUERIES
    // ─────────────────────────────────────────────────────────────────────────

    /** @return True if this player has died and the death sequence is underway. */
    public boolean isDead() { return isDead; }

    // ─────────────────────────────────────────────────────────────────────────
    // PAUSE HOOK
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called once per frame while the game is in PausedState.
     * Default implementation does nothing (most characters freeze during pause).
     * Dio overrides this to play his boss intro banner animation.
     *
     * @param world  The game world.
     */
    protected void onPauseUpdate(MyWorld world) {
        // Default: do nothing during pause.
    }
}
