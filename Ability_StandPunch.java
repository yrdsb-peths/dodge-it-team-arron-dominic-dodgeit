/*
 * ─────────────────────────────────────────────────────────────────────────────
 * Ability_StandPunch.java  —  THE WORLD STAND (E KEY)
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   Summons TheWorldStand — a Stand actor that hovers beside Dio, plays a
 *   punch animation, and destroys any Obstacles it touches for 3.5 seconds.
 *   Dio is invincible while the Stand is active.
 *
 * How it works:
 *   Activate: spawn TheWorldStand, start the 3.5-second duration timer.
 *   Update:   count down the duration; stop if expired or if player dies.
 *   TheWorldStand.act() calls ability.isActive() every frame — if the ability
 *   ends for any reason, the Stand removes itself from the world automatically.
 *
 * Keybind: E  (GameConfig.STAND_PUNCH_BUTTON)
 * Duration: 3.5 seconds (GameConfig.WORLD_PUNCH_DURATION)
 * Cooldown: 5.0 seconds (GameConfig.WORLD_PUNCH_COOLDOWN)
 *
 * Time Machine:
 *   captureState() saves both timers as an int[4].
 *   TheWorldStand handles its own position in its captureState().
 *
 * Interacts with:
 *   GenericPlayer (ability is loaded by), TheWorldStand (reads isActive()),
 *   Obstacles (destroyed by TheWorldStand), GameTimer (two timers)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class Ability_StandPunch implements Ability {

    /** How long the Stand remains active. Configured in GameConfig. */
    private GameTimer durationTimer = new GameTimer(GameConfig.WORLD_PUNCH_DURATION, false);

    /** How long before the Stand can be summoned again. Configured in GameConfig. */
    private GameTimer cooldownTimer = new GameTimer(GameConfig.WORLD_PUNCH_COOLDOWN, false);

    /**
     * Animation speed for TheWorldStand's punch animation.
     * Lower = faster animation.  Read by TheWorldStand's Animator.
     */
    public int standAnimSpeed = 2;

    // ─────────────────────────────────────────────────────────────────────────
    // ABILITY INTERFACE IMPLEMENTATION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Activates the Stand if neither duration nor cooldown is running.
     * Spawns TheWorldStand next to the player and starts the duration timer.
     */
    @Override
    public void activate(Player p, MyWorld world) {
        if (!durationTimer.isActive() && !cooldownTimer.isActive()) {
            durationTimer.reset();
            durationTimer.start();

            p.setAnimation("Idle"); // Dio stands still while the Stand does the work
            world.addObject(new TheWorldStand(this), p.getX(), p.getY());

            AudioManager.play("muda_barrage"); // MUDA MUDA MUDA
        }
    }

    /**
     * Updates the duration timer each frame.
     * Also runs the cooldown timer down while it is active.
     * Stops the ability if the duration expires or the player died.
     */
    @Override
    public void update(Player p, MyWorld world) {
        if (cooldownTimer.isActive()) cooldownTimer.update(world);
        if (!durationTimer.isActive()) return;

        durationTimer.update(world);

        if (durationTimer.isExpired() || p.isDead()) {
            stopAbility(p);
        }
    }

    /**
     * Cleanly shuts down the ability: stops the duration timer, switches
     * Dio back to "Dash", and starts the cooldown.
     * TheWorldStand will detect isActive()==false on its next frame and remove itself.
     */
    private void stopAbility(Player p) {
        durationTimer.stop();
        if (!p.isDead()) {
            cooldownTimer.reset();
            cooldownTimer.start();
            p.setAnimation("Dash");
        }
    }

    /**
     * Force-cancels the Stand (e.g., when the player dies).
     * Stops the duration timer; TheWorldStand removes itself next frame
     * because isActive() will return false.
     */
    @Override
    public void cancel() {
        durationTimer.stop();
        // TheWorldStand's act() checks isActive() — it will self-destruct next frame.
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STATE QUERIES
    // ─────────────────────────────────────────────────────────────────────────

    /** @return True while the Stand is summoned and the duration is running. */
    @Override
    public boolean isActive() {
        return durationTimer.isActive() && !durationTimer.isExpired();
    }

    @Override public boolean isCooldownActive() { return cooldownTimer.isActive(); }

    /** Orange wheel: full at start of active phase, drains to 0 as Stand expires. */
    @Override
    public double getActivePercent() {
        return durationTimer.isActive() ? (1.0 - durationTimer.getPercentComplete()) : 0.0;
    }

    /** Blue wheel: fills from 0 to 1 as the cooldown counts down. */
    @Override
    public double getCooldownPercent() {
        return cooldownTimer.isActive() ? cooldownTimer.getPercentComplete() : 0.0;
    }

    @Override public String getKeybind()      { return GameConfig.STAND_PUNCH_BUTTON; }
    @Override public String getDisplayLabel() { return "E"; }

    // ─────────────────────────────────────────────────────────────────────────
    // TIME MACHINE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Saves the state of both timers.
     * Format: [durationFrames, cooldownFrames, durationActive(0/1), cooldownActive(0/1)]
     */
    @Override
    public Object captureState() {
        return new int[]{
            durationTimer.getRemainingFrames(),
            cooldownTimer.getRemainingFrames(),
            durationTimer.isActive() ? 1 : 0,
            cooldownTimer.isActive() ? 1 : 0
        };
    }

    /** Restores both timers from the saved int[4] array. */
    @Override
    public void restoreState(Object state) {
        int[] data = (int[]) state;
        durationTimer.setRemainingFrames(data[0]);
        cooldownTimer.setRemainingFrames(data[1]);
        if (data[2] == 1) durationTimer.start(); else durationTimer.stop();
        if (data[3] == 1) cooldownTimer.start(); else cooldownTimer.stop();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EXTRA GETTERS / SETTERS  (used by TheWorldStand to inspect the ability)
    // ─────────────────────────────────────────────────────────────────────────
    public int     getDurFrames()  { return durationTimer.getRemainingFrames(); }
    public void    setDurFrames(int f) { durationTimer.setRemainingFrames(f); }
    public boolean isDurActive()   { return durationTimer.isActive(); }
    public void    startDur()      { durationTimer.start(); }
    public void    stopDur()       { durationTimer.stop(); }
    public int     getCoolFrames() { return cooldownTimer.getRemainingFrames(); }
    public void    setCoolFrames(int f) { cooldownTimer.setRemainingFrames(f); }
    public boolean isCoolActive()  { return cooldownTimer.isActive(); }
    public void    startCool()     { cooldownTimer.start(); }
    public void    stopCool()      { cooldownTimer.stop(); }
}
