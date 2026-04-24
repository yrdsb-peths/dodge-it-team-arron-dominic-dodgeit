import greenfoot.*;

public class Ability_TheWorld implements Ability {

    public static boolean TIME_STOPPED = false;
    private boolean keyWasDown = false;

    @Override
    public void activate(Player p, MyWorld world) {
        if (keyWasDown) return; // Debounce so it only fires once per tap
        
        keyWasDown = true;
        TIME_STOPPED = !TIME_STOPPED; // Toggle the state
        
        if (TIME_STOPPED) {
            // --- STOP TIME ---
            world.addObject(new FX_TimeStopOverlay(), world.getWidth() / 2, world.getHeight() / 2);
            
            if (GameConfig.ACTIVE_CHARACTER.bossConfig != null) {
                world.addObject(new Banner(GameConfig.ACTIVE_CHARACTER.bossConfig), GameConfig.s(1120), GameConfig.s(200));
            }
            
            // STRIKE THE POSE!
            if (p instanceof GenericPlayer) {
                ((GenericPlayer) p).setAnimation("Wry");
            }
        } else {
            // --- RESUME TIME ---
            if (p instanceof GenericPlayer) {
                ((GenericPlayer) p).setAnimation("Dash");
            }
        }
    }

    @Override
    public void update(Player p, MyWorld world) {
        // Reset the key lock when the player releases W
        if (!Greenfoot.isKeyDown(getKeybind())) {
            keyWasDown = false;
        }
    }

    @Override public void cancel() { TIME_STOPPED = false; }
    @Override public boolean isActive() { return TIME_STOPPED; }
    @Override public boolean isCooldownActive() { return false; }
    
    // Shows solid orange when stopped, empty when running
    @Override public double getActivePercent() { return TIME_STOPPED ? 1.0 : 0.0; }
    @Override public double getCooldownPercent() { return 0.0; }
    
    @Override public String getKeybind() { return "w"; }
    @Override public String getDisplayLabel() { return "W"; }
    @Override public boolean shouldShowIcon() { return true; }

    @Override
    public Object captureState() {
        return new int[]{ TIME_STOPPED ? 1 : 0, keyWasDown ? 1 : 0 };
    }

    @Override
    public void restoreState(Object state) {
        int[] d = (int[]) state;
        TIME_STOPPED = (d[0] == 1);
        keyWasDown = (d[1] == 1);
    }
}