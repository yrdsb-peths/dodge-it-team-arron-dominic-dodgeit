import greenfoot.*;

public class Ability_Mandom implements Ability {
    public boolean shouldShowIcon() { return false; }//Doesnt show icon: has a bar already
    public void activate(Player p, MyWorld world) {
        if (world.getGSM().peekState() instanceof PlayingState) {
            PlayingState ps = (PlayingState) world.getGSM().peekState();
            // Trigger the rewind safely through the Playing State
            ps.triggerRewind(world);
        }
    }

    // Rewind is a global state, so the player ability doesn't need to track timers
    public void update(Player p, MyWorld world) {}
    public void cancel() {}
    public boolean isActive() { return false; }
    public String getKeybind() { return GameConfig.REWIND_TIME_BUTTON; }
    public boolean isCooldownActive() { return false; }
    public double getActivePercent() { return 0.0; }
    public double getCooldownPercent() { return 0.0; }
    
    // Time Machine hooks (Not needed, as the RewindManager handles itself)
    public Object captureState() { return null; }
    public void restoreState(Object state) {}
    public String getDisplayLabel() { return "R"; }
}