/*
 * ─────────────────────────────────────────────────────────────────────────────
 * Ability_Mandom.java  —  TIME REWIND (R KEY)
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   The simplest ability — it is purely a keybind-to-action bridge.
 *   Pressing R delegates the rewind operation entirely to PlayingState,
 *   which owns the Time_RewindManager.  This ability has no timers of its own.
 *
 * Why not handle rewind here?
 *   Rewinding is a WORLD-LEVEL operation — it affects every actor, the score,
 *   the SpawnManager, and the RNG state.  PlayingState is the right owner
 *   because it has access to all of those systems.  Ability_Mandom just
 *   provides the keybind hookup and plays the "no icon needed" role.
 *
 * shouldShowIcon() returns false because the UI_RewindBar already shows
 * how much rewind time is available — a second icon would be redundant.
 *
 * captureState() / restoreState() are no-ops because the rewind system
 * itself handles rewinding — it doesn't need to save the rewind manager's
 * own state through this ability.
 *
 * Keybind: R  (GameConfig.REWIND_TIME_BUTTON)
 *
 * Interacts with:
 *   PlayingState (calls triggerRewind()), Time_RewindManager (does the work),
 *   GenericPlayer (loads and calls this ability)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class Ability_Mandom implements Ability {

    /**
     * No icon is shown for this ability — the UI_RewindBar handles the display.
     */
    @Override
    public boolean shouldShowIcon() { return false; }

    /**
     * Delegates the rewind to PlayingState.triggerRewind().
     * That method checks if there is enough history, starts the rewind animation,
     * and mutes audio — all things this ability should not do itself.
     */
    @Override
    public void activate(Player p, MyWorld world) {
        if (world.getGSM().peekState() instanceof PlayingState) {
            PlayingState ps = (PlayingState) world.getGSM().peekState();
            ps.triggerRewind(world);
        }
    }

    // This ability has no ongoing update, no timers, and no state to save.
    @Override public void update(Player p, MyWorld world) {}
    @Override public void cancel() {}
    @Override public boolean isActive()         { return false; }
    @Override public boolean isCooldownActive() { return false; }
    @Override public double  getActivePercent()  { return 0.0; }
    @Override public double  getCooldownPercent(){ return 0.0; }
    @Override public String  getKeybind()        { return GameConfig.REWIND_TIME_BUTTON; }
    @Override public String  getDisplayLabel()   { return "R"; }

    // No state to save — the RewindManager handles itself.
    @Override public Object captureState()           { return null; }
    @Override public void   restoreState(Object s)   {}
}
