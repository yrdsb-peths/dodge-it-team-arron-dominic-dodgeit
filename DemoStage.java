import greenfoot.*;
import java.util.*;

public class DemoStage {
    
    // The functional interface that evaluates if the player did the right thing
    public interface WaitCondition {
        boolean isMet(GenericPlayer player, MyWorld world);
    }
    
    private static class SpawnEntry {
        int frame, laneIndex, speed; boolean isTrain;
        SpawnEntry(int f, boolean t, int l, int s) { frame = f; isTrain = t; laneIndex = l; speed = s; }
    }
    
    private static class WaitPoint {
        int frame; WaitCondition condition; String prompt;
        WaitPoint(int f, WaitCondition c, String p) { frame = f; condition = c; prompt = p; }
    }
    
    private List<SpawnEntry> spawns = new ArrayList<>();
    private List<WaitPoint> waitPoints = new ArrayList<>();
    private String defaultText;
    
    public DemoStage(String defaultText) {
        this.defaultText = defaultText;
    }
    
    public DemoStage spawnRoadrollerAt(int frame, int laneIndex) {
        spawns.add(new SpawnEntry(frame, false, laneIndex, GameConfig.ROADROLLER_SPEED));
        return this;
    }
    
    public DemoStage addWaitPoint(int frame, WaitCondition cond, String prompt) {
        waitPoints.add(new WaitPoint(frame, cond, prompt));
        return this;
    }
    
    // Checks if the timeline is blocked by a WaitPoint. Returns the prompt text if blocked, or null if clear.
    public String evaluateWait(int currentFrame, GenericPlayer player, MyWorld world) {
        for (WaitPoint wp : waitPoints) {
            if (wp.frame == currentFrame && !wp.condition.isMet(player, world)) {
                return wp.prompt; // Blocked! Player needs to do the action.
            }
        }
        return null; // Not blocked!
    }
    
    // Spawns obstacles scheduled for this exact frame
    public void fireSpawns(int frame, MyWorld world) {
        for (SpawnEntry s : spawns) {
            if (s.frame == frame) {
                int laneY = GameConfig.LANES[s.laneIndex % GameConfig.LANES.length];
                world.addObject(new Roadroller(s.speed), world.getWidth(), laneY);
            }
        }
    }
    
    public String getDefaultText() { return defaultText; }
    
    // Returns true if all spawns and wait points have passed + 120 frames (2 seconds of padding)
    public boolean isComplete(int currentFrame) {
        int maxFrame = 0;
        for (SpawnEntry s : spawns) maxFrame = Math.max(maxFrame, s.frame);
        for (WaitPoint w : waitPoints) maxFrame = Math.max(maxFrame, w.frame);
        return currentFrame > (maxFrame + 120); 
    }
    
    
}