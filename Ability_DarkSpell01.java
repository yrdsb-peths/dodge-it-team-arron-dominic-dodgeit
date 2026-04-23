import greenfoot.*;
import java.util.List;

public class Ability_DarkSpell01 implements Ability {
    private boolean isActive = false;
    private boolean onCooldown = false;
    private int activeTimer = 0;
    private int cooldownTimer = 0;
    private boolean keyWasDown = false;
    private boolean hasResized = false;

    // --- MATH CONSTANTS ---
    public static final int IMAGE_SIZE = 600; // Total width/height in pixels
    public static final int BLAST_RADIUS = (int)IMAGE_SIZE*2/3;

    private static final int ACTIVE_DURATION = 54;
    private static final int COOLDOWN_DURATION = 60;

    @Override
    public void activate(Player p, MyWorld world) {
        if (isActive || onCooldown || keyWasDown) return;
        
        keyWasDown = true;
        isActive   = true;
        activeTimer = ACTIVE_DURATION;
        
        if (p instanceof GenericPlayer) {
            GenericPlayer gp = (GenericPlayer) p;
            // SCALE THE IMAGE (The Visual)
            if (!hasResized) {
                gp.resizeAnimation("DarkSpell_01", GameConfig.s(IMAGE_SIZE));
                hasResized = true;
            }
            gp.setAnimation("DarkSpell_01");
        }
        AudioManager.play("night_spell1");
    }
    
    @Override
    public void update(Player p, MyWorld world) {
        if (!Greenfoot.isKeyDown(getKeybind())) keyWasDown = false;

        if (isActive) {
            // THE HITBOX (The Logic)
            double spellRadius = GameConfig.s(BLAST_RADIUS);

            List<Obstacles> obstacles = world.getObjects(Obstacles.class);
            for (Obstacles obs : obstacles) {
                double distance = Math.hypot(obs.getX() - p.getX(), obs.getY() - p.getY());
                // If car touches the 175px radius...
                if (distance <= (spellRadius + obs.getRadius())) {
                    obs.destroy(); 
                }
            }

            activeTimer--;
            if (activeTimer <= 0) {
                isActive = false;
                onCooldown = true;
                cooldownTimer = COOLDOWN_DURATION;
                if (p instanceof GenericPlayer) ((GenericPlayer) p).setAnimation("Dash");
            }
        }

        if (onCooldown) {
            cooldownTimer--;
            if (cooldownTimer <= 0) onCooldown = false;
        }
    }

    // ... captureState, restoreState, cancel same as before ...
    @Override public String getKeybind() { return "v"; }
    @Override public String getDisplayLabel() { return "V"; }
    @Override public boolean isActive() { return isActive; }
    @Override public boolean isCooldownActive() { return onCooldown; }
    @Override public double getCooldownPercent() { return onCooldown ? 1.0 - ((double)cooldownTimer/COOLDOWN_DURATION) : 0.0; }
    @Override public double getActivePercent() { return isActive ? (double)activeTimer/ACTIVE_DURATION : 0.0; }
    @Override public void cancel() { isActive = false; onCooldown = false; }
    @Override public Object captureState() { return new int[]{activeTimer, cooldownTimer, isActive?1:0, onCooldown?1:0}; }
    @Override public void restoreState(Object state) { int[] s=(int[])state; activeTimer=s[0]; cooldownTimer=s[1]; isActive=s[2]==1; onCooldown=s[3]==1; }
}