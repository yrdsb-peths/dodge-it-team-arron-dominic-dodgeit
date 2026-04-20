import greenfoot.*;

public interface Ability {
    // Core logic
    void activate(Player p, MyWorld world);
    void update(Player p, MyWorld world);
    void cancel();
    boolean isActive();

    // Binding and UI
    String getDisplayLabel();
    String getKeybind();
    boolean isCooldownActive();
    double getActivePercent();   // Returns 0.0 to 1.0 for the orange drain bar
    double getCooldownPercent(); // Returns 0.0 to 1.0 for the blue fill bar
    
    
    // Time Machine hooks!
    Object captureState();
    void restoreState(Object state);
    
    //Sticky Finger
    default boolean shouldHidePlayer() { return false; }
}