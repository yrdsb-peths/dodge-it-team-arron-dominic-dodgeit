/*
 * ─────────────────────────────────────────────────────────────────────────────
 * Time_ActorMemento.java  —  A SNAPSHOT OF ONE ACTOR AT ONE MOMENT IN TIME
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A simple data container (the "Memento" in the Memento design pattern).
 *   Holds everything needed to restore a single actor to a past state:
 *     - A direct reference to the actor object.
 *     - Its X and Y coordinates at the time of capture.
 *     - A customData slot for any extra actor-specific data.
 *
 * About customData:
 *   Each Time_Snapshottable actor decides what its "extra data" is.
 *   It can be anything — a private inner class, an int[], a boolean, etc.
 *   The type is Object so any type can be stored.  The actor itself knows
 *   how to cast it back when restoreState() is called.
 *
 *   Examples:
 *     Roadroller   → RoadrollerData (inner class with speed + scoreToAdd)
 *     Train        → TrainData (inner class with speed, state, waitTimer)
 *     GenericPlayer→ PlayerMemento (inner class with dead flag, anim, timers, abilities)
 *     ScrollingRoad→ null (only needs position, no extra data)
 *
 * All fields are final — the memento is immutable after creation.
 *
 * Interacts with:
 *   Time_Snapshottable (actors create/consume these),
 *   Time_FrameSnapshot (holds a list of these),
 *   Time_RewindManager (creates from captureState(), feeds to restoreState())
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class Time_ActorMemento {

    /**
     * A direct reference to the actual Actor object in the world.
     * Used by the rewind manager to: check if the actor is still in the world,
     * re-add it if it was removed, and call restoreState() on it.
     */
    public final Actor actor;

    /** The X coordinate of the actor at the time of capture. */
    public final int x;

    /** The Y coordinate of the actor at the time of capture. */
    public final int y;

    /**
     * Actor-specific extra state.  Cast to the appropriate type in restoreState().
     * May be null for simple actors that only need position (e.g., ScrollingRoad).
     */
    public final Object customData;

    /**
     * Creates a new memento for the given actor.
     *
     * @param actor       The actor being captured.
     * @param x           Its current X position.
     * @param y           Its current Y position.
     * @param customData  Any extra actor-specific data (may be null).
     */
    public Time_ActorMemento(Actor actor, int x, int y, Object customData) {
        this.actor      = actor;
        this.x          = x;
        this.y          = y;
        this.customData = customData;
    }
}
