/*
 * ─────────────────────────────────────────────────────────────────────────────
 * Time_FrameSnapshot.java  —  A COMPLETE SNAPSHOT OF ONE GAME FRAME
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   Holds everything needed to fully restore the game to a specific past frame.
 *   One of these is created every frame by Time_RewindManager.record() and
 *   pushed into the history deque.
 *
 * Contents:
 *   - actorStates   : a memento for every Time_Snapshottable actor in the world
 *   - score         : the ScoreManager score at this frame
 *   - spawnTimer    : the SpawnManager's global frame counter
 *   - difficultyTimer: the SpawnManager's level-up interval (currently unused on restore)
 *   - roadrollerRate: the current roadroller spawn interval
 *   - trainRate     : the current train spawn interval
 *   - rngState      : the GameRNG seed — critical for deterministic replay
 *
 * Why the rngState matters:
 *   If we restore the SpawnManager's timer but not the RNG seed, future spawns
 *   would generate different random lanes than the original playthrough.
 *   Saving and restoring the seed guarantees the same lane sequence.
 *
 * All fields are final — snapshots are immutable after creation.
 *
 * Interacts with:
 *   Time_RewindManager (creates and consumes these),
 *   Time_ActorMemento (list of, stored here),
 *   SpawnManager (timers saved/restored), ScoreManager (score saved/restored),
 *   GameRNG (seed saved/restored)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import java.util.List;

public class Time_FrameSnapshot {

    /** One memento per Time_Snapshottable actor that existed in this frame. */
    
    public final Time_ActorMemento[] actorStates;

    /** The player's score at this frame. */
    public final int score;

    /** SpawnManager's roadroller spawn interval at this frame. */
    public int roadrollerRate;

    /** SpawnManager's train spawn interval at this frame. */
    public int trainRate;

    /** SpawnManager's global frame counter at this frame (controls all spawn timing). */
    public final int spawnTimer;

    /** SpawnManager's difficulty level-up interval (saved for completeness). */
    public final int difficultyTimer;

    /**
     * The GameRNG seed at this frame.
     * Restoring this guarantees that any future calls to GameRNG.getRandomNumber()
     * produce the same sequence as the original playthrough from this point.
     */
    public final long rngState;

    /**
     * Creates a complete frame snapshot.
     *
     * @param actorStates     Mementos of every snapshottable actor.
     * @param score           The score at this frame.
     * @param spawnTimer      SpawnManager's frame counter.
     * @param difficultyTimer SpawnManager's level-up interval.
     * @param rRate           Current roadroller spawn interval.
     * @param tRate           Current train spawn interval.
     * @param rngState        The GameRNG seed value.
     */
    public Time_FrameSnapshot(
            Time_ActorMemento[] actorStates, int score,
            int spawnTimer, int difficultyTimer,
            int rRate, int tRate, long rngState) {
        this.actorStates     = actorStates;
        this.score           = score;
        this.spawnTimer      = spawnTimer;
        this.difficultyTimer = difficultyTimer;
        this.roadrollerRate  = rRate;
        this.trainRate       = tRate;
        this.rngState        = rngState;
    }
    
    
}
