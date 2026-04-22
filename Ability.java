/*
 * ─────────────────────────────────────────────────────────────────────────────
 * Ability.java  —  THE CONTRACT ALL ABILITIES MUST FOLLOW
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A Java interface defining every method that any ability class must provide.
 *   GenericPlayer stores abilities as a List<Ability>, so it can call
 *   a.update() or a.activate() on any ability without knowing its specific type.
 *
 * Lifecycle (called by GenericPlayer.movementLogic() every frame):
 *   1. update(p, world)  — tick timers, handle ongoing effects.
 *   2. If keybind held → activate(p, world)  — try to start the ability.
 *
 * UI contract:
 *   getActivePercent()  → orange outer wheel (drains while ability is active)
 *   getCooldownPercent()→ blue outer wheel (fills as cooldown progresses)
 *   getSecondaryCooldownPercent() → inner blue ring (for Sticky Fingers portal)
 *
 * Time Machine contract:
 *   captureState() / restoreState() — every ability must save and restore
 *   all its timer state so rewind works correctly.
 *
 * Default methods (pre-implemented, override only if needed):
 *   shouldShowIcon()              → true   (Mandom overrides to false)
 *   shouldHidePlayer()            → false  (StickyFingers overrides to true)
 *   getSecondaryCooldownPercent() → 0.0    (StickyFingers overrides)
 *
 * Implementations:
 *   Ability_StandPunch, Ability_MadeInHeaven, Ability_Mandom, Ability_StickyFingers
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public interface Ability {

    // ─────────────────────────────────────────────────────────────────────────
    // CORE LOGIC
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Attempts to activate the ability.  Should be a no-op if already active
     * or on cooldown (guard against accidental re-triggering).
     * Called every frame the keybind is held — debounce internally if needed.
     */
    void activate(Player p, MyWorld world);

    /**
     * Updates the ability state each frame.  Countdown timers, spawn afterimages,
     * check for ability end, etc.  Always called (even if inactive) so cooldowns run.
     */
    void update(Player p, MyWorld world);

    /**
     * Force-stops the ability.  Called when the player dies so active effects
     * clean up immediately (e.g., TheWorldStand disappears).
     */
    void cancel();

    /** @return True while the ability is doing something (not just on cooldown). */
    boolean isActive();

    // ─────────────────────────────────────────────────────────────────────────
    // BINDING AND UI
    // ─────────────────────────────────────────────────────────────────────────

    /** @return The single-character label shown in the ability icon (e.g., "E", "S"). */
    String getDisplayLabel();

    /**
     * Whether this ability should show a UI icon.
     * Default: true.  Mandom returns false (uses the RewindBar instead).
     */
    default boolean shouldShowIcon() { return true; }

    /** @return The keyboard key string that activates this ability (e.g., "e", "r"). */
    String getKeybind();

    /** @return True while the cooldown is counting down (ability cannot be re-used yet). */
    boolean isCooldownActive();

    /**
     * Returns 0.0–1.0 representing how much of the active phase remains.
     * Used by UI_AbilityIcon to draw the ORANGE outer wheel (drains as ability runs).
     * Return 0.0 if the ability is not active.
     */
    double getActivePercent();

    /**
     * Returns 0.0–1.0 representing how far through the cooldown we are.
     * Used by UI_AbilityIcon to draw the BLUE outer wheel (fills as cooldown passes).
     * Return 0.0 if not on cooldown.
     */
    double getCooldownPercent();

    // ─────────────────────────────────────────────────────────────────────────
    // TIME MACHINE HOOKS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Captures all timer and state data needed to restore this ability after a rewind.
     * Typically returns an int[] with: [remainingFrames, isActive(0/1)] per timer.
     */
    Object captureState();

    /**
     * Restores the ability to the state captured by captureState().
     * Called by GenericPlayer.restoreState() during a rewind.
     */
    void restoreState(Object state);

    // ─────────────────────────────────────────────────────────────────────────
    // OPTIONAL OVERRIDES (with defaults)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * If true, the player sprite is made invisible and invincible.
     * Default: false.  Ability_StickyFingers returns true while underground.
     */
    default boolean shouldHidePlayer() { return false; }

    /**
     * Returns 0.0–1.0 for a SECONDARY cooldown, drawn as the inner ring in
     * UI_AbilityIcon.  Default: 0.0 (no inner ring).
     * Ability_StickyFingers uses this for the portal warp cooldown.
     */
    default double getSecondaryCooldownPercent() { return 0.0; }
    
    /*
     * Default speedmovement multiplier to be overwriten by MIH
     */
    default double getMovementMultiplier() {
        return 1.0;
    }
}
