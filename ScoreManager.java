/*
 * ─────────────────────────────────────────────────────────────────────────────
 * ScoreManager.java  —  TRACKS THE CURRENT SCORE AND SESSION HIGH SCORE
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A static utility class.  There is no ScoreManager object — all methods and
 *   fields are static, accessed as ScoreManager.addScore(1) etc.
 *
 * Lifecycle within one game session:
 *   1. PlayingState.enter() calls reset() → score resets to 0, highScore preserved.
 *   2. During play, Roadroller/Train call addScore() when they pass the player.
 *   3. GameOverState.enter() calls updateHighScore() and then reads both values.
 *   4. Time_RewindManager calls setScore() to restore past score during rewind.
 *
 * Note:
 *   High score is NOT persisted to disk — it resets when the Greenfoot
 *   project is closed.  Adding file persistence would require Greenfoot's
 *   UserInfo API or Java file I/O.
 *
 * Interacts with:
 *   PlayingState, GameOverState, Roadroller, Train, TheWorldStand,
 *   Time_RewindManager, UIText (score display)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class ScoreManager {

    /** The player's current score in the ongoing game session. */
    private static int score = 0;

    /** The best score achieved since the project was launched. Not persisted to disk. */
    private static int highScore = 0;

    // ─────────────────────────────────────────────────────────────────────────
    // SCORE MANIPULATION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Adds points to the current score.
     * Called by Roadroller.checkRemove() (+1 per car dodged),
     * Train.checkRemove() (+5 per ambulance dodged),
     * and TheWorldStand.act() (+2 per obstacle destroyed by the Stand).
     *
     * @param amount  Number of points to add (should be positive).
     */
    public static void addScore(int amount) {
        score += amount;
    }

    /**
     * Directly sets the score.
     * Used ONLY by the time-rewind system (Time_RewindManager) to restore
     * the score to its value at a past frame.  Do not call this from gameplay code.
     *
     * @param s  The score value to restore.
     */
    public static void setScore(int s) { score = s; }

    /**
     * Resets the current score to 0, copying it to highScore first if it's
     * a new record.  Called at the start of each new PlayingState.
     */
    public static void reset() {
        if (score > highScore) highScore = score;
        score = 0;
    }

    /**
     * Updates highScore if the current score exceeds it.
     * Called by GameOverState.enter() to freeze the best score for display.
     * reset() already does this, but GameOverState calls this separately
     * for clarity.
     */
   public static void updateHighScore() {
        // 1. Ignore "Cheater" Dio scores
        if (GameConfig.ACTIVE_CHARACTER == CharacterConfig.omnipotent_DIO|| 
            GameConfig.ACTIVE_CHARACTER == CharacterConfig.CUSTOM) {
            return; 
        }
        
        if (score > highScore) highScore = score;
        
        // 2. THE FIX: Switch from DataManager to SaveManager
        int allTimeBest = SaveManager.getInt("all_time_high"); 
        if (score > allTimeBest) {
            SaveManager.setInt("all_time_high", score);
            SaveManager.save(); // Save to user_stats.txt
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────────────────────────────────────

    /** @return The current running score. */
    public static int getScore()     { return score; }

    /** @return The best score achieved this session. */
    public static int getHighScore() { return highScore; }
    
    
}
