/*
 * ─────────────────────────────────────────────────────────────────────────────
 * Time_Snapshottable.java  —  INTERFACE FOR ACTORS THAT CAN BE REWOUND
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A "tag interface" that any Actor implements to declare that it participates
 *   in the time-rewind system.
 *
 *   Time_RewindManager iterates all actors in the world every frame.
 *   Only those that implement Time_Snapshottable are captured.
 *   Actors that do NOT implement this interface are completely ignored by
 *   the rewind system.
 *
 * Analogy:
 *   captureState() = "Take a photo of yourself right now."
 *   restoreState() = "Go back to looking like this old photo."
 *
 * Currently implements:
 *   GenericPlayer, Roadroller, Train, ScrollingRoad, TheWorldStand,
 *   Exclaimation, PathWarning
 *
 * What is NOT snapshottable (ignored by rewind):
 *   UIText, UI_AbilityIcon, UI_RewindBar, Banner, FX_* classes.
 *   These are visual effects that either manage themselves or
 *   are too transient to need rewinding.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public interface Time_Snapshottable {

    /**
     * Captures the actor's complete state at this moment.
     * Returns a Time_ActorMemento containing the actor reference,
     * its X/Y position, and any extra data needed to fully restore it.
     *
     * Called every frame by Time_RewindManager.record().
     */
    Time_ActorMemento captureState();

    /**
     * Restores the actor to the state described by the given memento.
     * The memento was previously returned by captureState() from a past frame.
     * The actor's X/Y position is restored by the manager before this is called.
     *
     * Called during rewind by Time_RewindManager.restoreSnapshot().
     *
     * @param memento  The past state to restore from.
     */
    void restoreState(Time_ActorMemento memento);
}
