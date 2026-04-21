/*
 * ─────────────────────────────────────────────────────────────────────────────
 * IActiveGameState.java  —  MARKER INTERFACE FOR ANY "LIVE GAMEPLAY" STATE
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A small interface that both PlayingState and AbilityDisplayState implement.
 *   Its purpose is to let every actor's guard check work correctly in BOTH states
 *   without changing 7 actor files to mention AbilityDisplayState by name.
 *
 * The key trick:
 *   GameStateManager.isState(Class) uses stateClass.isInstance(peek), which
 *   works for interfaces.  So actors can do:
 *       if (!world.getGSM().isState(IActiveGameState.class)) return;
 *   ...and that line returns true whether we are in PlayingState OR AbilityDisplayState.
 *
 * Two methods are declared here so Ability_Mandom (and any future ability that
 * needs to communicate with the hosting state) doesn't need to know the concrete
 * state class — it just talks to the interface.
 *
 * Classes that implement this:
 *   PlayingState, AbilityDisplayState
 *
 * Classes that USE this interface:
 *   MyWorld          (isRewinding() delegates to it)
 *   Obstacles        (act() guard)
 *   ScrollingRoad    (act() guard)
 *   TheWorldStand    (act() guard)
 *   PathWarning      (act() guard)
 *   FX_ZipperGround  (act() guard)
 *   Ability_Mandom   (triggerRewind delegation)
 * ─────────────────────────────────────────────────────────────────────────────
 */
public interface IActiveGameState {

    /**
     * Returns true while the time-rewind system is actively rewinding.
     * Called by MyWorld.isRewinding() so actors can check it without knowing
     * which concrete state they are in.
     */
    boolean isRewinding();

    /**
     * Starts a time rewind if enough history is available.
     * Called by Ability_Mandom.activate() so the ability doesn't need
     * to cast to a specific state class.
     *
     * @param world  The game world (needed to add the rewind overlay actor).
     */
    void triggerRewind(MyWorld world);
}
