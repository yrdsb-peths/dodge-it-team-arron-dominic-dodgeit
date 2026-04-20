import java.util.List;
import java.util.ArrayList;

public class Time_FrameSnapshot {
    public final List<Time_ActorMemento> actorStates;
    public final int score;
    public int roadrollerRate;
    public int trainRate;
    public final int spawnTimer;
    public final int difficultyTimer;
    public final long rngState;//Stores the RNG
    
    public Time_FrameSnapshot(List<Time_ActorMemento> actorStates, int score, 
                          int spawnTimer, int difficultyTimer,int rRate, int tRate, long rngState) {
        this.actorStates = actorStates;
        this.score = score;
        this.spawnTimer = spawnTimer;
        this.difficultyTimer = difficultyTimer;
        this.roadrollerRate = rRate;
        this.trainRate = tRate;
        this.rngState = rngState;
    }
}