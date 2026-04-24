/*
 * ─────────────────────────────────────────────────────────────────────────────
 * GameTimer.java  —  A REUSABLE COUNTDOWN TIMER FOR ABILITIES AND EVENTS
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   GameTimer is one of the most widely used utility classes in the project.
 *   It converts "seconds" into "frames" (since Greenfoot runs at ~60fps) and
 *   provides start/stop/reset controls, expiry checks, and percentage helpers.
 *
 * CRITICAL FEATURE: Game-state awareness.
 *   The update() method ONLY ticks while the GSM is in PlayingState.
 *   This means ALL timers automatically freeze during:
 *     - The main menu
 *     - The character select screen
 *     - The time-stop pause (PausedState)
 *     - The game-over screen
 *   You get this for free — no extra code needed in any ability or actor.
 *
 * Typical usage patterns:
 *
 *   ONE-SHOT ABILITY DURATION:
 *     private GameTimer durationTimer = new GameTimer(3.5, false);
 *     // To fire: durationTimer.reset(); durationTimer.start();
 *     // To check: if (durationTimer.isExpired()) { ... stop the ability ... }
 *
 *   COOLDOWN AFTER ABILITY:
 *     private GameTimer cooldownTimer = new GameTimer(5.0, false);
 *     // After ability ends: cooldownTimer.reset(); cooldownTimer.start();
 *     // Gate: if (!cooldownTimer.isActive()) { ... allow activation ... }
 *
 *   LOOPING SPAWN TIMER (SpawnManager equivalent if used here):
 *     private GameTimer spawnTimer = new GameTimer(1.5, true);
 *     spawnTimer.start();
 *     spawnTimer.update(world);
 *     if (spawnTimer.isExpired()) { spawnSomething(); } // auto-resets because loop=true
 *
 * Time Machine support:
 *   getRemainingFrames() and setRemainingFrames() allow the rewind system to
 *   save and restore timer progress precisely across snapshots.
 *
 * Interacts with:
 *   Every ability class, GenericPlayer, Exclaimation, and any class
 *   that needs a duration, cooldown, or life timer.
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class GameTimer {

    /** The full duration of the timer, converted to frames (seconds × 60). */
    private int totalFrames;

    /** How many frames remain before the timer expires.  Counts down to 0. */
    private int remainingFrames;

    /** Whether the timer is currently counting down. */
    private boolean active;

    /**
     * If true, the timer resets itself to totalFrames when it reaches 0
     * (like an alarm that keeps ringing).
     * If false, it expires once and stays at 0 until manually reset.
     */
    private boolean loop;

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new timer, but does NOT start it.  Call start() when ready.
     *
     * @param seconds  The duration in real-world seconds. Converted internally to frames.
     * @param loop     True = auto-reset on expiry (repeating). False = one-shot.
     */
    public GameTimer(double seconds, boolean loop) {
        this.totalFrames     = (int)(seconds * 60); // 60 acts per second
        this.remainingFrames = totalFrames;
        this.loop            = loop;
        this.active          = false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONTROLS
    // ─────────────────────────────────────────────────────────────────────────

    /** Starts the timer ticking.  Does not reset remainingFrames. */
    public void start() { this.active = true; }

    /** Stops the timer.  Remaining frames are preserved. */
    public void stop()  { this.active = false; }

    /** Resets remainingFrames back to totalFrames.  Does not start or stop. */
    public void reset() { this.remainingFrames = totalFrames; }

    // ─────────────────────────────────────────────────────────────────────────
    // THE ENGINE — must be called every frame from an actor's act() or a
    //              state's update() for the timer to count down.
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Ticks the timer down by one frame.
     * ONLY ticks if: (a) the timer is active, AND (b) the game is in PlayingState.
     * This automatic game-state check is what freezes all timers during pause/menu.
     *
     * When remainingFrames reaches 0:
     *   - If loop=true : resets and continues running.
     *   - If loop=false: sets active=false and stays at 0 (expired).
     *
     * @param world  Needed to check the current game state via the GSM.
     */
    public void update(MyWorld world) {
        if (active && world.getGSM().isState(IActiveGameState.class)) {
            IActiveGameState state = (IActiveGameState) world.getGSM().peekState();
            
            // --- THE FIX: Stop the timer if the demo is showing a prompt ---
            if (state.isGameFrozen()) return; 

            if (remainingFrames > 0) {
                remainingFrames--;
            } else if (loop) {
                reset(); 
            } else {
                active = false; 
            }
        }
    }

    /**
     * Force-ticks the timer regardless of game state (ignores pause/menu).
     * Currently unused in main code, but available for special cases.
     */
    public void forceTick() {
        if (remainingFrames > 0) remainingFrames--;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // QUERY METHODS
    // ─────────────────────────────────────────────────────────────────────────

    /** @return True when the timer has counted all the way down to 0. */
    public boolean isExpired() { return remainingFrames <= 0; }

    /** @return True while the timer is actively counting down. */
    public boolean isActive()  { return active; }

    /** @return Remaining time in seconds (remainingFrames / 60). */
    public double getSecondsRemaining() { return remainingFrames / 60.0; }

    /**
     * Returns how far through the timer we are, from 0.0 (just started)
     * to 1.0 (fully expired).  Useful for drawing progress/cooldown bars.
     *
     * Example — a cooldown bar that shrinks as time passes:
     *   double progress = cooldownTimer.getPercentComplete();
     *   int barWidth = (int)(100 * (1.0 - progress)); // shrinks from 100 to 0
     */
    public double getPercentComplete() {
        return (double)(totalFrames - remainingFrames) / totalFrames;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TIME MACHINE SUPPORT  (save/restore for the rewind system)
    // ─────────────────────────────────────────────────────────────────────────

    /** @return The exact number of frames left.  Saved into rewind snapshots. */
    public int getRemainingFrames() { return remainingFrames; }

    /**
     * Directly sets remaining frames.  Used by restoreState() in ability classes
     * to rewind the timer to its past value.
     */
    public void setRemainingFrames(int frames) { this.remainingFrames = frames; }

    /** @return The full duration in frames (totalFrames). Rarely needed externally. */
    public int getTotalFrames() { return totalFrames; }

    /**
     * Changes the timer's duration and immediately resets remainingFrames to match.
     * Useful when you want to reuse a timer with a different duration.
     *
     * @param seconds  The new duration in real-world seconds.
     */
    public void setDuration(double seconds) {
        this.totalFrames = (int)(seconds * 60);
        reset();
    }
    
    public int getFramesElapsed() {
        // totalFrames is the starting time, remainingFrames is what's left.
        // The difference is how much time has passed!
        return totalFrames - remainingFrames;
    }
}
