import java.util.List;
import java.util.ArrayDeque;
import java.util.ArrayList;
import greenfoot.*;

public class Time_RewindManager {
    
    // Public so UI_RewindBar can read them
    public static final int MAX_HISTORY = GameConfig.MAX_REWIND_TIME;       // 15 seconds at 60fps (360)
    public static final int REWIND_COST_FRAMES = GameConfig.REWIND_TIME; // 2 seconds per use
    private static final int REWIND_SPEED = 3;        // frames popped per tick (4x speed)
    
    private ArrayDeque<Time_FrameSnapshot> history = new ArrayDeque<>();
    private boolean isRewinding = false;
    private int framesRewound = 0;
    
    public void record(MyWorld world, SpawnManager spawnManager) {
        if (isRewinding) return;
        
        List<Time_ActorMemento> mementos = new ArrayList<>();
        for (Actor a : world.getObjects(Actor.class)) {
            if (a instanceof Time_Snapshottable) {
                mementos.add(((Time_Snapshottable) a).captureState());
            }
        }
        
        history.push(new Time_FrameSnapshot(
            mementos,
            ScoreManager.getScore(),
            spawnManager.getSpawnTimer(),
            spawnManager.getDifficultyTimer(),
            spawnManager.getRoadrollerRate(),
            spawnManager.getTrainRate(),
            GameRNG.getState() //RNG state
        ));
        
        if (history.size() > MAX_HISTORY) history.pollLast();
    }
    
    // Returns true if a rewind can be started
    public boolean canRewind() {
        return !isRewinding && history.size() >= REWIND_COST_FRAMES;
    }
    
    // Call once to kick off a rewind
    public void startRewind() {
        if (!canRewind()) return;
        isRewinding = true;
        framesRewound = 0;
    }
    
    // Call every frame while rewinding. Returns true while still going.
    public boolean rewindStep(MyWorld world, SpawnManager spawnManager) {
        if (!isRewinding) return false;
        
        for (int i = 0; i < REWIND_SPEED; i++) {
            if (framesRewound >= REWIND_COST_FRAMES || history.isEmpty()) {
                stopRewinding();
                return false;
            }
            rewindOneFrame(world, spawnManager);
            framesRewound++;
        }
        return true;
    }
    
    public void rewindOneFrame(MyWorld world, SpawnManager spawnManager) {
        if (history.isEmpty()) { stopRewinding(); return; }
        restoreSnapshot(history.pop(), world, spawnManager);
    }
    
    private void restoreSnapshot(Time_FrameSnapshot snap, MyWorld world,
                                  SpawnManager spawnManager) {
        // 1. Restore global state
        ScoreManager.setScore(snap.score);
        spawnManager.restoreState(snap.spawnTimer, snap.roadrollerRate, snap.trainRate);
        GameRNG.restoreState(snap.rngState);//Restores the RNG
        
        // 2. Which actors should exist?
        List<Actor> shouldExist = new ArrayList<>();
        for (Time_ActorMemento m : snap.actorStates) shouldExist.add(m.actor);
        
        // 3. Remove actors spawned after this snapshot
        for (Actor a : world.getObjects(Actor.class)) {
            if (a instanceof Time_Snapshottable && !shouldExist.contains(a)) {
                world.removeObject(a);
            }
        }
        
        // 4. Re-add actors that existed then but are gone now
        List<Actor> currentActors = world.getObjects(null);
        for (Time_ActorMemento m : snap.actorStates) {
            if (!currentActors.contains(m.actor)) {
                world.addObject(m.actor, m.x, m.y);
            }
        }
        
        // 5. Restore each actor's state
        for (Time_ActorMemento m : snap.actorStates) {
            m.actor.setLocation(m.x, m.y);
            ((Time_Snapshottable) m.actor).restoreState(m);
        }
    }
    
    public void stopRewinding() { isRewinding = false; }
    public boolean isRewinding() { return isRewinding; }
    public boolean hasHistory() { return !history.isEmpty(); }
    public void clearHistory() { history.clear(); }
    public int getHistorySize() { return history.size(); }
}