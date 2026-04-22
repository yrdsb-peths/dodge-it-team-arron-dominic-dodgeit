/*
 * ─────────────────────────────────────────────────────────────────────────────
 * Time_RewindManager.java  —  THE ENGINE OF THE TIME-REWIND SYSTEM
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   Records the game's state every frame (a deque of Time_FrameSnapshots),
 *   and replays those snapshots backwards when a rewind is triggered.
 *
 * ── RECORDING (forward pass) ─────────────────────────────────────────────────
 *   Every frame during normal play, record() is called.
 *   It iterates every Actor in the world, captures all that implement
 *   Time_Snapshottable, and pushes a Time_FrameSnapshot to the front of
 *   the deque.  Old snapshots beyond MAX_HISTORY are discarded from the back.
 *
 * ── REWINDING (backward pass) ────────────────────────────────────────────────
 *   startRewind() sets isRewinding=true.
 *   Each frame, rewindStep() pops REWIND_SPEED (3) snapshots from the deque
 *   and calls restoreSnapshot() on each.  This makes the rewind play at 3×
 *   normal speed (fast rewind effect).
 *   After REWIND_COST_FRAMES (120) snapshots are consumed, rewind stops.
 *
 * ── WHAT restoreSnapshot() DOES ──────────────────────────────────────────────
 *   1. Restore global state: score, spawn timer/rate, RNG seed.
 *   2. Determine which actors SHOULD exist (those in the snapshot's list).
 *   3. REMOVE actors that exist in the world but NOT in the snapshot
 *      (these were spawned after the snapshot → they should not exist in the past).
 *   4. RE-ADD actors that ARE in the snapshot but not in the world
 *      (these were destroyed → they should be alive in the past).
 *   5. Restore each actor's position and call restoreState() on it.
 *
 * ── REWIND SPEED ─────────────────────────────────────────────────────────────
 *   REWIND_SPEED = 3: three snapshots are consumed per frame → 3× faster than
 *   real time.  The FX_RewindOverlay provides the visual feedback.
 *
 * Interacts with:
 *   PlayingState (owns and calls this), SpawnManager (state save/restore),
 *   ScoreManager (score save/restore), GameRNG (seed save/restore),
 *   every Time_Snapshottable actor (captureState / restoreState)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import java.util.List;
import java.util.ArrayDeque;
import java.util.ArrayList;
import greenfoot.*;
import java.util.Set;        
import java.util.HashSet;     

public class Time_RewindManager {

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTANTS  (public so UI_RewindBar can read them)
    // ─────────────────────────────────────────────────────────────────────────

    /** Maximum number of past frames stored. 360 frames = 6 seconds at 60fps. */
    public static final int MAX_HISTORY = GameConfig.MAX_REWIND_TIME;

    /** Each rewind use consumes this many frames from the history. 120 = 2 seconds. */
    public static final int REWIND_COST_FRAMES = GameConfig.REWIND_TIME;

    /**
     * How many snapshots are popped per frame during rewind.
     * 3 = rewind plays at 3× real-time speed.
     */

    // ─────────────────────────────────────────────────────────────────────────
    // STATE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * The circular history buffer.  ArrayDeque is used as a double-ended queue:
     * new snapshots are push()ed to the front (head).
     * Old snapshots beyond MAX_HISTORY are pollLast()ed from the back (tail).
     * During rewind, snapshots are pop()ped from the front (most recent first).
     */
    private ArrayDeque<Time_FrameSnapshot> history = new ArrayDeque<>();

    /** True while rewind is in progress. Checked by world.isRewinding(). */
    private boolean isRewinding = false;

    /** Counts how many snapshots have been consumed in the current rewind session. */
    private int framesRewound = 0;

    // ─────────────────────────────────────────────────────────────────────────
    // RECORDING
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Saves a snapshot of the entire world state for this frame.
     * Called every frame during normal play (not during rewind).
     *
     * @param world         The game world (to iterate all actors).
     * @param spawnManager  The spawn manager (to save timer/rate state).
     */
    public void record(MyWorld world, SpawnManager spawnManager) {
        if (isRewinding) return; // never record during rewind

        List<Actor> allActors = world.getObjects(Actor.class);
        
        // 1. Count snapshottables (creates zero garbage!)
        int count = 0;
        for (Actor a : allActors) {
            if (a instanceof Time_Snapshottable) count++;
        }

        // 2. Pre-allocate exact array size (avoids ArrayList resizing overhead)
        Time_ActorMemento[] mementos = new Time_ActorMemento[count];
        int index = 0;
        for (Actor a : allActors) {
            if (a instanceof Time_Snapshottable) {
                mementos[index++] = ((Time_Snapshottable) a).captureState();
            }
        }

        // Bundle with global state into one frame snapshot
        history.push(new Time_FrameSnapshot(
            mementos,
            ScoreManager.getScore(),
            spawnManager.getSpawnTimer(),
            spawnManager.getDifficultyTimer(),
            spawnManager.getRoadrollerRate(),
            spawnManager.getTrainRate(),
            GameRNG.getState()      
        ));

        // Trim history to the maximum allowed size
        if (history.size() > MAX_HISTORY) history.pollLast();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REWIND CONTROL
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns true if there is enough history stored to perform a rewind.
     * Requires at least REWIND_COST_FRAMES snapshots in the deque.
     */
    public boolean canRewind() {
        return !isRewinding && history.size() >= REWIND_COST_FRAMES;
    }

    /**
     * Starts a rewind session.  Should only be called if canRewind() is true.
     * Sets the rewinding flag and resets the consumed-frames counter.
     */
    public void startRewind() {
        if (!canRewind()) return;
        isRewinding  = true;
        framesRewound = 0;
    }

    /**
     * Advances the rewind by REWIND_SPEED snapshots.
     * Called every frame from PlayingState.update() while rewinding.
     *
     * @param world         The game world (for actor manipulation).
     * @param spawnManager  The spawn manager (for state restoration).
     * @return              True if the rewind is still in progress; false if done.
     */
    public boolean rewindStep(MyWorld world, SpawnManager spawnManager, int speed) {
        if (!isRewinding) return false;

        // Pop multiple snapshots per frame to create the fast-rewind effect
        for (int i = 0; i < speed; i++) {
            if (framesRewound >= REWIND_COST_FRAMES || history.isEmpty()) {
                stopRewinding();
                return false; // rewind is complete
            }
            rewindOneFrame(world, spawnManager);
            framesRewound++;
        }
        return true; // still rewinding
    }

    /** Pops and restores one snapshot.  Used internally by rewindStep(). */
    public void rewindOneFrame(MyWorld world, SpawnManager spawnManager) {
        if (history.isEmpty()) { stopRewinding(); return; }
        restoreSnapshot(history.pop(), world, spawnManager);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SNAPSHOT RESTORATION — the most complex method in the project
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Restores the game world to the state described by a past snapshot.
     * Performs five steps in order:
     *
     *   Step 1: Restore global state (score, spawn timer, RNG seed).
     *   Step 2: Build the list of actors that SHOULD exist in this past frame.
     *   Step 3: Remove actors that exist NOW but were NOT in the past snapshot
     *           (they were spawned after the snapshot was taken).
     *   Step 4: Re-add actors that WERE in the past snapshot but are GONE now
     *           (they were destroyed after the snapshot was taken).
     *   Step 5: For every snapshotted actor, restore its position and call
     *           restoreState() with its saved data.
     *
     * @param snap          The snapshot to restore.
     * @param world         The game world.
     * @param spawnManager  The spawn manager.
     */
    private void restoreSnapshot(Time_FrameSnapshot snap, MyWorld world,
                                  SpawnManager spawnManager) {
        // ── STEP 1: Global state ──────────────────────────────────────────────
        ScoreManager.setScore(snap.score);
        spawnManager.restoreState(snap.spawnTimer, snap.roadrollerRate, snap.trainRate);
        GameRNG.restoreState(snap.rngState); // restore RNG seed for deterministic replay

        // ── STEP 2: Build "should exist" set ─────────────────────────────────
        Set<Actor> shouldExist = new HashSet<>();
        for (Time_ActorMemento m : snap.actorStates) shouldExist.add(m.actor);

        // ── STEP 3: Remove actors spawned AFTER this snapshot ─────────────────
        // Only remove snapshottable actors — UI/FX actors manage themselves.
        for (Actor a : world.getObjects(Actor.class)) {
            if (a instanceof Time_Snapshottable && !shouldExist.contains(a)) {
                world.removeObject(a);
            }
        }

        // ── STEP 4: Re-add actors that were alive then but are gone now ───────
        Set<Actor> currentActors = new HashSet<>(world.getObjects(null));
        for (Time_ActorMemento m : snap.actorStates) {
            if (!currentActors.contains(m.actor)) {
                world.addObject(m.actor, m.x, m.y);
            }
        }

        // ── STEP 5: Restore position and state for every actor ────────────────
        for (Time_ActorMemento m : snap.actorStates) {
            m.actor.setLocation(m.x, m.y);
            ((Time_Snapshottable) m.actor).restoreState(m);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STATE QUERIES
    // ─────────────────────────────────────────────────────────────────────────

    public void    stopRewinding()  { isRewinding = false; }
    public boolean isRewinding()    { return isRewinding; }
    public boolean hasHistory()     { return !history.isEmpty(); }
    public void    clearHistory()   { history.clear(); }

    /**
     * @return  The number of frames currently stored in history.
     *          Used by UI_RewindBar to draw the fill level.
     */
    public int getHistorySize() { return history.size(); }
}
