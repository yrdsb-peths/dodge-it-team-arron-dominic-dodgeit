import greenfoot.*;

public class Ability_DarkSpell02 implements Ability {  

    // instance variables - replace the example below with your own
    private boolean isActive   = false;
    private boolean onCooldown = false;

    private int activeTimer   = 0;
    private int cooldownTimer = 0;
    private boolean keyWasDown = false;
    
    //DarkSpell_02 has 8 frames
    
    private static final int ACTIVE_DURATION = 48;
    private static final int FREEZE_DURATION = 100;
    private static final int COOLDOWN_DURATION = 250;

    @Override
    public void activate(Player p, MyWorld world) {
        if (isActive || onCooldown || keyWasDown) return;

        keyWasDown  = true;
        isActive    = true;
        activeTimer = ACTIVE_DURATION;
        
        //Play the spell animation
        if ( p instanceof GenericPlayer) {
            ((GenericPlayer) p).setAnimation("DarkSpell_02");
        }

        // Freeze all obstacles currently on screen
        java.util.List<Obstacles> obstacles =
            world.getObjects(Obstacles.class);
        for (Obstacles obs : obstacles) {
            obs.freeze(FREEZE_DURATION);
        }
    
    }
    
    @Override
    public void update(Player p, MyWorld world) {
        // Reset key latch when key is released
        if (!Greenfoot.isKeyDown(getKeybind())) {
            keyWasDown = false;
        }

        // Count down active timer
        if (isActive) {
            activeTimer--;
            if (activeTimer <= 0) {
                isActive   = false;
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
    
    public void cancel() {
        isActive      = false;
        onCooldown    = false;
        activeTimer   = 0;
        cooldownTimer = 0;
        keyWasDown    = false; // reset keywasdown to allow reactivation after cancel
    }
    
    @Override public String getKeybind()        { return "c"; }
    @Override public String getDisplayLabel()   { return "C"; }
    @Override public boolean isActive()         { return isActive; }
    @Override public boolean isCooldownActive() { return onCooldown; }
    
    @Override
    public double getActivePercent() {
        if (!isActive) return 0.0;
        return (double) activeTimer / ACTIVE_DURATION;
    }

    @Override
    public double getCooldownPercent() {
        if (!onCooldown) return 0.0;
        return 1.0 - ((double) cooldownTimer / COOLDOWN_DURATION);
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
        int[] s       = (int[]) state;
        activeTimer   = s[0];
        cooldownTimer = s[1];
        isActive      = s[2] == 1;
        onCooldown    = s[3] == 1;
    }
}
