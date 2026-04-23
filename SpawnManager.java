/*
 * ─────────────────────────────────────────────────────────────────────────────
 * SpawnManager.java  —  CONTROLS OBSTACLE SPAWNING AND DIFFICULTY SCALING
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A plain Java class (not an Actor) managed by PlayingState.
 *   Its update() is called manually from PlayingState.update() each frame.
 *   It maintains a frame counter (globalTimer) and spawns obstacles at regular
 *   intervals.  As time passes, it increases the frequency and speed of spawns.
 *
 * Difficulty curve:
 *   Every LEVEL_UP_TIME (200) frames, increaseDifficulty() is called:
 *     - Roadroller interval decreases (more frequent), floor = roadrollerMin
 *     - Roadroller speed increases, cap = roadrollerSpeedMax
 *     - Train interval decreases (more frequent), floor = trainMin
 *     - Train speed increases, cap = trainSpeedMax
 *     - Background scroll speed increases by 1
 *
 * Lane selection:
 *   Uses GameRNG (not Greenfoot.getRandomNumber) for deterministic lane picks
 *   so the rewind system can reproduce the same spawn pattern after rewinding.
 *
 * Rewind support:
 *   The rewind system saves and restores globalTimer, roadrollerRate, and trainRate
 *   so that after a rewind the obstacle spawn timing is exactly correct.
 *
 * Interacts with:
 *   PlayingState (calls update()), Roadroller, Train, PathWarning, Exclaimation,
 *   ScrollingRoad (speed increases), GameRNG (lane selection),
 *   Time_RewindManager (state save/restore), GameConfig (all rate constants)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class SpawnManager {

    /**
     * Frame counter that advances every time update() is called (every game frame).
     * Obstacle spawning and difficulty increases are triggered by this counter.
     */
    private int globalTimer = 0;

    // ─────────────────────────────────────────────────────────────────────────
    // DIFFICULTY TRACKING
    // ─────────────────────────────────────────────────────────────────────────

    /** Current difficulty level (unused directly; difficulty is implicit in the rates). */
    private int difficultyLevel = 0;

    /** How many frames between difficulty increases. Decreases by 5 each level (unused tweak). */
    private int levelUpTime = GameConfig.LEVEL_UP_TIME;

    // ── Roadroller parameters (adjusted each difficulty increase) ─────────────
    /** Current interval between Roadroller spawns (frames). Decreases with difficulty. */
    private int roadrollerRate  = GameConfig.ROADROLLER_RATE;
    /** Minimum spawn interval — Roadrollers can never appear faster than this. */
    private final int roadrollerMin      = GameConfig.ROADROLLER_MIN_RATE;
    /** Current Roadroller speed in pixels/frame. Increases with difficulty. */
    private int roadrollerSpeed = GameConfig.ROADROLLER_SPEED;
    /** Speed cap — Roadrollers can never go faster than this. */
    private final int roadrollerSpeedMax = GameConfig.ROADROLLER_MAX_SPEED;

    // ── Train parameters (adjusted each difficulty increase) ──────────────────
    /** Current interval between Train spawns (frames). Decreases with difficulty. */
    private int trainRate       = GameConfig.TRAIN_RATE;
    /** Minimum spawn interval — Trains can never appear faster than this. */
    private final int trainMin  = GameConfig.TRAIN_MIN_RATE;
    /** Current Train charge speed in pixels/frame. Increases with difficulty. */
    private int trainSpeed      = GameConfig.TRAIN_SPEED;
    /** Speed cap — Trains can never go faster than this. */
    private final int trainSpeedMax = GameConfig.TRAIN_MAX_SPEED;

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────────────

    public SpawnManager() {
        // masterSeed was here for a planned feature — currently unused.
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MAIN UPDATE — called every frame from PlayingState.update()
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Advances the global timer and triggers spawns and difficulty increases
     * at the appropriate intervals.
     *
     * @param world  The game world to add obstacle actors to.
     */
    public void update(MyWorld world) {
        globalTimer++;

        // Increase difficulty every LEVEL_UP_TIME frames
        if (globalTimer % levelUpTime == 0) {
            increaseDifficulty();
        }

        // Spawn a Roadroller at the current rate
        if (globalTimer % roadrollerRate == 0) {
            spawnRoadroller(world);
        }

        // Spawn a Train (with warning) at the current rate
        if (globalTimer % trainRate == 0) {
            spawnTrain(world, trainSpeed);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DIFFICULTY INCREASE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Incrementally increases the challenge:
     *   - Roadrollers spawn more frequently and move faster.
     *   - Trains spawn more frequently and move faster.
     *   - The scrolling road background speeds up (visual pressure).
     * All increases are bounded by their configured minimum/maximum values.
     */
    private void increaseDifficulty() {
        if (roadrollerRate  > roadrollerMin)      roadrollerRate  -= 5;
        if (roadrollerSpeed < roadrollerSpeedMax) roadrollerSpeed += 1;
        if (trainRate       > trainMin)           trainRate       -= 5;
        if (trainSpeed      < trainSpeedMax)      trainSpeed      += 5;
        ScrollingRoad.increaseSpeed(1); // speed up the background scroll
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SPAWN METHODS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Spawns a single Roadroller at a random lane on the right edge of the screen.
     */
    private void spawnRoadroller(MyWorld world) {
        int spawnY = getRandomLane();
        world.addObject(new Roadroller(roadrollerSpeed), world.getWidth(), spawnY);

    }

    /**
     * Spawns a Train with its warning system:
     *   1. Exclaimation (!) mark — appears immediately at the right edge.
     *   2. PathWarning (red zone) — highlights the full lane width.
     *   3. Train — waits 65 frames before charging (long enough for the warning).
     *
     * All three are placed at the same Y (the chosen lane centre).
     */
    private void spawnTrain(MyWorld world, int speed) {
        int spawnY = getRandomLane();

        int pathHeight    = GameConfig.LANE_HEIGHT;
        int exclaimXOffset = GameConfig.s(20); // slightly inset from right edge
        int trainXOffset   = GameConfig.s(50); // starts slightly off-screen right

        world.addObject(new Exclaimation(),                            world.getWidth() - exclaimXOffset, spawnY);
        world.addObject(new PathWarning(world.getWidth(), pathHeight), world.getWidth() / 2,              spawnY);
        world.addObject(new Train(speed),                              world.getWidth() + trainXOffset,   spawnY);
    }

    /**
     * Picks a random lane Y-coordinate from the LANES array.
     * Uses GameRNG (deterministic) instead of Greenfoot.getRandomNumber()
     * so that rewinding reproduces the same lane sequence.
     *
     * @return  A Y coordinate from GameConfig.LANES.
     */
    private int getRandomLane() {
        return GameConfig.LANES[GameRNG.getRandomNumber(GameConfig.LANES.length)];
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REWIND SUPPORT  (save / restore state for Time_RewindManager)
    // ─────────────────────────────────────────────────────────────────────────

    /** @return  The current global frame timer (saved in each FrameSnapshot). */
    public int getSpawnTimer()       { return globalTimer; }

    /** @return  Current Roadroller spawn interval (saved in each FrameSnapshot). */
    public int getRoadrollerRate()   { return roadrollerRate; }

    /** @return  Current Train spawn interval (saved in each FrameSnapshot). */
    public int getTrainRate()        { return trainRate; }

    /** @return  The current difficulty timer interval. */
    public int getDifficultyTimer()  { return levelUpTime; }

    /**
     * Restores the spawn manager's state from a past snapshot.
     * Called by Time_RewindManager.restoreSnapshot() during rewind.
     *
     * @param timer  The global frame timer to restore.
     * @param rRate  The roadroller spawn rate to restore.
     * @param tRate  The train spawn rate to restore.
     */
    public void restoreState(int timer, int rRate, int tRate) {
        this.globalTimer      = timer;
        this.roadrollerRate   = rRate;
        this.trainRate        = tRate;
    }

    public void setSpawnTimer(int time)       { this.globalTimer    = time; }
    public void setRoadrollerRate(int rate)   { this.roadrollerRate = rate; }
    public void setTrainRate(int rate)        { this.trainRate      = rate; }
    public void setDifficultyTimer(int l)     { this.levelUpTime    = l;   }
}
