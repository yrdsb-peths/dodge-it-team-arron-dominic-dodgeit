/*
 * ─────────────────────────────────────────────────────────────────────────────
 * GameRNG.java  —  DETERMINISTIC RANDOM NUMBER GENERATOR FOR REWIND CORRECTNESS
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A custom random number generator that produces reproducible sequences.
 *   Unlike Greenfoot.getRandomNumber(), this generator's internal state (its
 *   "seed") can be SAVED and RESTORED, which is essential for the time-rewind
 *   system.
 *
 * WHY THIS EXISTS — The Rewind Problem:
 *   If SpawnManager used Greenfoot.getRandomNumber(), then after a rewind,
 *   re-running the same frames would produce DIFFERENT random lane choices
 *   for new enemy spawns.  The enemies would appear in wrong positions,
 *   breaking the "true time reversal" illusion.
 *
 *   GameRNG fixes this by:
 *     1. Storing the seed in every Time_FrameSnapshot.
 *     2. Restoring the seed when rewinding to that snapshot.
 *     3. Any call to getRandomNumber() after the restore produces the
 *        exact same sequence as the original timeline.
 *
 * Algorithm:
 *   Linear Congruential Generator (LCG) — a classic, simple pseudo-random
 *   formula: seed = (seed × 1103515245 + 12345).
 *   The output is extracted from the middle bits to reduce bias.
 *
 * IMPORTANT:
 *   ALWAYS use GameRNG.getRandomNumber() instead of Greenfoot.getRandomNumber()
 *   for anything that affects gameplay (lane selection, spawn timing, etc.).
 *
 * Interacts with:
 *   SpawnManager (lane selection), Time_RewindManager (save/restore state),
 *   Time_FrameSnapshot (stores the seed)
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class GameRNG {

    /** The current state of the generator.  All randomness derives from this. */
    private static long currentSeed;

    // ─────────────────────────────────────────────────────────────────────────
    // INITIALISATION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Seeds the generator with the current system clock time.
     * Call this ONCE at the start of a new game session (in PlayingState.enter()).
     * Different sessions get different sequences; within one session the
     * sequence is fully deterministic and rewind-safe.
     */
    public static void randomize() {
        currentSeed = System.currentTimeMillis();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TIME MACHINE SUPPORT
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the current seed state.  Called by Time_RewindManager when
     * recording a Time_FrameSnapshot so the RNG state can be saved.
     */
    public static long getState() {
        return currentSeed;
    }

    /**
     * Restores the seed to a previously saved value.  Called by
     * Time_RewindManager when replaying a snapshot, guaranteeing that any
     * subsequent calls to getRandomNumber() produce the same results as
     * the original playthrough.
     *
     * @param oldSeed  The seed value saved from a past Time_FrameSnapshot.
     */
    public static void restoreState(long oldSeed) {
        currentSeed = oldSeed;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CORE USAGE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns a pseudo-random integer in the range [0, max).
     * Advances the internal seed by one step.
     *
     * USE THIS instead of Greenfoot.getRandomNumber() for all gameplay logic.
     *
     * @param max  The exclusive upper bound.  getRandomNumber(5) returns 0–4.
     * @return     A pseudo-random integer in [0, max), or 0 if max ≤ 0.
     */
    public static int getRandomNumber(int max) {
        if (max <= 0) return 0;
        // LCG step: advance the seed by the standard multiplier + increment
        currentSeed = (currentSeed * 1103515245L + 12345L);
        // Extract bits 16–46 (the most statistically uniform bits in an LCG)
        return (int)(Math.abs(currentSeed / 65536) % max);
    }
}
