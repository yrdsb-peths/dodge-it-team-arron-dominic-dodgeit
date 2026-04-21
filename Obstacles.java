/*
 * ─────────────────────────────────────────────────────────────────────────────
 * Obstacles.java  —  THE ABSTRACT BASE CLASS FOR ALL HAZARDS
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   Like Player, Obstacles is an abstract blueprint.  Concrete obstacle types
 *   (Roadroller, Train) extend it and implement the three abstract methods.
 *
 * METHOD CALL ORDER IN act() — this order is intentional and important:
 *   1. movementLogic()  — move the obstacle (change X/Y position).
 *   2. collisionLogic() — check if it hit the player.
 *   3. checkRemove()    — check if it should be deleted (went off-screen).
 *
 * WHY checkRemove() is last:
 *   If checkRemove() calls world.removeObject(this), the object is removed from
 *   the world.  Any call to getWorld() after that returns null, causing a
 *   NullPointerException.  By checking removal LAST, we ensure nothing runs
 *   after the self-removal.
 *
 * The standard GSM guard:
 *   act() starts with the PlayingState check so all obstacles freeze during
 *   menus, pause, and game-over screens.
 *
 * Interacts with:
 *   Roadroller, Train (subclasses), Player (collision target),
 *   GameStateManager (state check)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public abstract class Obstacles extends Actor {

    /**
     * The speed of this obstacle in pixels per frame.
     * Set by the SpawnManager when spawning, based on current difficulty.
     * Protected so subclasses can read and write it directly.
     */
    protected int speed;

    /**
     * Called by Greenfoot each frame.
     * Applies the standard PlayingState guard, then runs the three
     * sub-logic methods in the correct order.
     */
    @Override
    public void act() {
        MyWorld world = (MyWorld) getWorld();
        // Safety check: if we are not in a world or not in PlayingState, do nothing.
        if (world == null || !world.getGSM().isState(PlayingState.class)) return;

        movementLogic();  // 1. Move
        collisionLogic(); // 2. Check hits
        checkRemove();    // 3. Remove if off-screen (MUST be last)
    }

    /**
     * Moves the obstacle each frame.
     * Typically: move(-speed) to travel left across the screen.
     */
    protected abstract void movementLogic();

    /**
     * Checks if this obstacle is touching the player and handles the hit.
     * Call player.die() if a valid collision is detected.
     */
    protected abstract void collisionLogic();

    /**
     * Checks if the obstacle should be removed (usually: went off the left edge).
     * If removing, also add score if the player is alive.
     * Must call world.removeObject(this) at the END of this method — never before.
     */
    protected abstract void checkRemove();
}
