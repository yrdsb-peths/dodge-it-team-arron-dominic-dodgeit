import greenfoot.*;

public interface IActiveGameState {
    boolean isRewinding();
    void triggerRewind(MyWorld world);
    boolean isGameFrozen(); // NEW: Tells actors when the demo is waiting for the player
    SpawnManager getSpawnManager();
    Time_RewindManager getRewindManager();
}

