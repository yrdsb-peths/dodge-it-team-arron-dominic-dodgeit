import greenfoot.*;


public class ScoreManager {
    private static int score = 0;
    private static int highScore = 0;

    public static void addScore(int amount) {
        score += amount;
    }

    public static int getScore() {
        return score;
    }

    public static void reset() {
        if (score > highScore) highScore = score;
        score = 0;
    }

    public static int getHighScore() {
        return highScore;
    }
    
    public static void updateHighScore() {
        if (score > highScore) {
            highScore = score;
        }
    }
    
    public static void setScore(int s) { score = s; }

}