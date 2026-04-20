public class GameRNG {
    private static long currentSeed;

    // Call this once when the game starts
    public static void randomize() {
        currentSeed = System.currentTimeMillis();
    }

    // Get the current state of the math (to save in snapshots)
    public static long getState() {
        return currentSeed;
    }

    // Restore the math (when rewinding)
    public static void restoreState(long oldSeed) {
        currentSeed = oldSeed;
    }

    // USE THIS INSTEAD OF Greenfoot.getRandomNumber()
    public static int getRandomNumber(int max) {
        if (max <= 0) return 0;
        // The magic math that generates a random sequence
        currentSeed = (currentSeed * 1103515245L + 12345L);
        return (int)(Math.abs(currentSeed / 65536) % max);
    }
}