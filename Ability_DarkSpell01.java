import greenfoot.*;

public class Ability_DarkSpell01 implements Ability {

    // instance variables - replace the example below with your own
    private boolean isActive = false;
    private boolean onCooldown = false;
    
    private int activeTimer = 0;
    private int cooldownTimer = 0;
    private boolean keyWasDown = false;
    
    private static final int ACTIVE_DURATION = 54;
    private static final int COOLDOWN_DURATION = 300; // 5 seconds

    @Override
    public void activate (Player p, MyWorld world) {
        if (isActive || onCooldown || keyWasDown) return;
        
        keyWasDown = true;
        isActive   = true;
        activeTimer = ACTIVE_DURATION;
        
        //play the spell animation
        if (p instanceof GenericPlayer) {
            ((GenericPlayer) p).setAnimation("DarkSpell_01");
        }
        
        // Destroy all obstacles currently on screen
        java.util.List<Obstacles> obstacles = 
            world.getObjects(Obstacles.class);
        for (Obstacles obs : obstacles) {
            obs.destroy();
        }
        
    }
    
    @Override
    public void update(Player p, MyWorld world){
        if (!Greenfoot.isKeyDown(getKeybind())) {
            keyWasDown = false;
        }
        
        // Count down active timer
        if (isActive) {
            activeTimer--;
            if (activeTimer <= 0) {
                isActive = false;
                onCooldown = true;
                cooldownTimer = COOLDOWN_DURATION;
                // Return to default animation when spell ends
                if (p instanceof GenericPlayer) {
                    ((GenericPlayer) p).setAnimation("Dash");
                }
            }
        }
        
        // Count down cooldown
        if (onCooldown) {
            cooldownTimer--;
            if (cooldownTimer <= 0) {
                onCooldown = false;
            }
        }
    }
    
    @Override
    public void cancel() {
        isActive = false;
        onCooldown = false;
        activeTimer = 0;
        cooldownTimer = 0;
        keyWasDown = false;
    }
    
    @Override public String getKeybind()    { return "v"; }
    @Override public String getDisplayLabel() { return "V"; }
    @Override public boolean isActive()       { return isActive; }
    @Override public boolean isCooldownActive() { return onCooldown; }
    
    @Override
    public double getCooldownPercent() {
        // Calculate cooldown percentage manually
        if (!onCooldown) return 0.0;
        // Returns how much of cooldown is remaining
        return (double) cooldownTimer / COOLDOWN_DURATION;
    }
    
    @Override
    public double getActivePercent() {
        if (!isActive) return 0.0;
        return (double) activeTimer / ACTIVE_DURATION;
    }
    
    @Override
    public Object captureState() {
        return new int[]{
            activeTimer, cooldownTimer,
            isActive ? 1 : 0,
            onCooldown ? 1 : 0
        };
    }
    
    @Override
    public void restoreState(Object state) {
        int[] s     = (int[]) state;
        activeTimer = s[0];
        cooldownTimer = s[1];
        isActive      = s[2] == 1;
        onCooldown    = s[3] == 1;
    }
}

