/*
 * ─────────────────────────────────────────────────────────────────────────────
 * Ability_MadeInHeaven.java  —  SPEED BOOST (S KEY)
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   Temporarily makes Dio appear to move faster than the world around him
 *   by slowing the game's tick rate.  Spawns motion-blur afterimage trails.
 *
 * HOW THE SPEED TRICK WORKS:
 *   Greenfoot.setSpeed() controls how many times per second act() is called.
 *   Normal speed is 50 (≈60fps).  MiH sets it to 48 (≈48fps).
 *   Dio moves the same number of pixels PER FRAME, but fewer frames happen
 *   per second — so obstacles move slower while Dio's reaction window is larger.
 *   This creates the illusion of Dio "accelerating time" around himself.
 *
 * Afterimage effect:
 *   Every frame while active, an FX_Afterimage is spawned at Dio's position.
 *   Each FX_Afterimage is an inverted (negative) copy of Dio's sprite that
 *   fades out over ~20 frames.
 *
 * Keybind: S  (GameConfig.MIH_BUTTON)
 * Duration: 4.8 seconds  (matches the sound effect length)
 * Cooldown: 3 seconds  (GameConfig.MIH_COOLDOWN)
 *
 * Time Machine:
 *   captureState() saves both timers.
 *   Note: Greenfoot.setSpeed() is NOT reversible via rewind — the tick speed
 *   is a global setting, not a per-actor state.  The rewind overlay visually
 *   masks this inconsistency.
 *
 * Interacts with:
 *   GenericPlayer (loaded as an ability), FX_Afterimage (spawned every frame),
 *   Greenfoot (tick speed), GameTimer (duration + cooldown)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class Ability_MadeInHeaven implements Ability {

    /** How long the speed effect lasts.  Set to match the "speed_up_time" audio clip. */
    private GameTimer durationTimer = new GameTimer(4.8, false);

    /** Counter used to gate afterimage spawning (currently spawns every frame). */
    private int afterimageCounter = 0;

    /** How much faster Dio moves during MiH (applied in GenericPlayer.handleStandardMovement). */
    private double speedMultiplier = 2;

    /** Tick speed while MiH is active — slower global tick rate. */
    private final int ACCELERATED_TICK_SPEED = GameConfig.MIH_TICK_SPEED;
    /** Normal tick speed to restore after MiH ends. */
    private final int NORMAL_TICK_SPEED      = GameConfig.NORMAL_TICK_SPEED;

    /** Prevents re-activating while the cooldown is running. */
    private GameTimer cooldownTimer = new GameTimer(GameConfig.MIH_COOLDOWN, false);

    // ─────────────────────────────────────────────────────────────────────────
    // ABILITY INTERFACE IMPLEMENTATION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Starts MiH if no duration or cooldown is currently running.
     * Slows the global tick rate and plays the speed-up sound.
     */
    @Override
    public void activate(Player p, MyWorld world) {
        boolean notRunning = !durationTimer.isActive() || durationTimer.isExpired();
        if (notRunning && !cooldownTimer.isActive()) {
            SpawnManager sm = ((PlayingState)world.getGSM().peekState()).getSpawnManager();
            boolean isMax = sm.getRoadrollerRate() <= GameConfig.ROADROLLER_MIN_RATE;
            durationTimer.setDuration(isMax ? 2.5 : 4.8); // 2.5s if Max, 4.8s if Normal
            durationTimer.start(); 
            speedMultiplier = (isMax ? 3 : 2);
            Greenfoot.setSpeed(isMax ? GameConfig.MIH_TICK_SPEED_MAX: GameConfig.MIH_TICK_SPEED); // World is even slower at Max diff
            AudioManager.play("speed_up_time");
        }
    }

    /**
     * Runs every frame.  Ticks both timers and, while active:
     *   - Spawns an FX_Afterimage at the player's location.
     *   - Restores tick speed when the duration expires.
     *   - Does NOT spawn afterimages while the player is underground.
     */
    @Override
    public void update(Player p, MyWorld world) {
        if (cooldownTimer.isActive()) cooldownTimer.update(world);
        if (!durationTimer.isActive()) return;

        // Don't draw afterimages while the player is invisible underground
        if (p instanceof GenericPlayer && ((GenericPlayer) p).isHidden()) return;

        durationTimer.update(world);

        // Spawn afterimage trail every frame (counter is here in case you want
        // to thin it out: e.g., afterimageCounter % 2 == 0 → every other frame)
        afterimageCounter++;
        if (afterimageCounter % 1 == 0) {
            world.addObject(new FX_Afterimage(p.getImage()), p.getX(), p.getY());
        }

        // Check if the duration has run out
        if (durationTimer.isExpired()) {
            durationTimer.stop();
            Greenfoot.setSpeed(NORMAL_TICK_SPEED); // restore normal game speed
            p.startIFrame(0.8);//Grant 0.8 seconds of iframe
            cooldownTimer.reset();
            cooldownTimer.start();
        }
    }

    /** Returns the speed multiplier (2.0) if active, or 1.0 if not. Used by GenericPlayer. */
    public double getSpeedMultiplier() {
        return (durationTimer.isActive() && !durationTimer.isExpired()) ? speedMultiplier : 1.0;
    }

    @Override
    public void cancel() {
        durationTimer.stop();
        Greenfoot.setSpeed(GameConfig.NORMAL_TICK_SPEED); // always restore speed on cancel
    }

    @Override public boolean isActive()         { return durationTimer.isActive(); }
    @Override public boolean isCooldownActive() { return cooldownTimer.isActive(); }

    /** Orange wheel: full at ability start, drains as the duration runs out. */
    @Override
    public double getActivePercent() {
        return durationTimer.isActive() ? (1.0 - durationTimer.getPercentComplete()) : 0.0;
    }

    /** Blue wheel: fills from 0 to 1 as the cooldown progresses. */
    @Override
    public double getCooldownPercent() {
        return cooldownTimer.isActive() ? cooldownTimer.getPercentComplete() : 0.0;
    }

    @Override public String getKeybind()      { return GameConfig.MIH_BUTTON; }
    @Override public String getDisplayLabel() { return "S"; }

    // ─────────────────────────────────────────────────────────────────────────
    // TIME MACHINE
    // ─────────────────────────────────────────────────────────────────────────

    /** Format: [durationFrames, cooldownFrames, durationActive(0/1), cooldownActive(0/1)] */
    @Override
    public Object captureState() {
        return new int[]{
            durationTimer.getRemainingFrames(), cooldownTimer.getRemainingFrames(),
            durationTimer.isActive() ? 1 : 0,  cooldownTimer.isActive() ? 1 : 0
        };
    }

    @Override
    public void restoreState(Object state) {
        int[] data = (int[]) state;
        durationTimer.setRemainingFrames(data[0]);
        cooldownTimer.setRemainingFrames(data[1]);
        if (data[2] == 1) durationTimer.start(); else durationTimer.stop();
        if (data[3] == 1) cooldownTimer.start(); else cooldownTimer.stop();
    }

    // Extra getters/setters for advanced inspection (currently unused externally)
    public int     getRemainingFrames()   { return durationTimer.getRemainingFrames(); }
    public void    setRemainingFrames(int f) { durationTimer.setRemainingFrames(f); }
    public void    startTimer()           { durationTimer.start(); }
    public void    stopTimer()            { durationTimer.stop(); }
    public double getMovementMultiplier() {
        return (durationTimer.isActive() && !durationTimer.isExpired()) ? speedMultiplier : 1.0;
    }
}
