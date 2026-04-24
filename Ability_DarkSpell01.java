import greenfoot.*;
import java.util.List;

public class Ability_DarkSpell01 implements Ability {
        
    private static final int POOL = GameConfig.MAX_REWIND_TIME + 10; // 370 slots
    private int[][] statePool = new int[POOL][5]; // 5 = however many values you store
    private int poolIdx = 0;
    private GameTimer durationTimer = new GameTimer(GameConfig.DS01_DURATION, false);
    private GameTimer cooldownTimer = new GameTimer(GameConfig.DS01_COOLDOWN, false);
    
    private boolean keyWasDown = false;
    private boolean hasResized = false;
    
    // --- NEW: Flag to remember which cooldown to use at the end ---
    private boolean usedResonance = false;

    @Override
    public void activate(Player p, MyWorld world) {
        if (durationTimer.isActive() || cooldownTimer.isActive() || keyWasDown) return;
        
        keyWasDown = true;
        durationTimer.reset();
        durationTimer.start();
        
        // 1. Determine if we are resonating right now
        usedResonance = (Ability_DarkSpell02.CURRENT_FROZEN_LANE_Y != -1);

        if (p instanceof GenericPlayer) {
            GenericPlayer gp = (GenericPlayer) p;
            
            if (!usedResonance) {
                // Standard Sphere Visual
                if (!hasResized) {
                    gp.resizeAnimation("DarkSpell_01", GameConfig.s(GameConfig.DS01_IMAGE_SIZE));
                    hasResized = true;
                }
                gp.setAnimation("DarkSpell_01");
            } else {
                // Combo Beam Visual (Stay in Idle)
                gp.setAnimation("Idle"); 
            }
        }
        
        AudioManager.play("night_spell1");
    }
    
    @Override
    public void update(Player p, MyWorld world) {
        cooldownTimer.update(world);
        if (!Greenfoot.isKeyDown(getKeybind())) keyWasDown = false;

        if (durationTimer.isActive()) {
            durationTimer.update(world);

            List<Obstacles> obstacles = world.getObjects(Obstacles.class);
            
            if (usedResonance) {
                // BEAM LOGIC
                int frozenY = Ability_DarkSpell02.CURRENT_FROZEN_LANE_Y;
                for (Obstacles obs : obstacles) {
                    if (Math.abs(obs.getY() - frozenY) < GameConfig.s(25)) obs.destroy(); 
                }
                if (durationTimer.getRemainingFrames() % 5 == 0) {
                    world.addObject(new FX_DarkAnnihilationBeam(), world.getWidth()/2, frozenY);
                }
            } else {
                // SPHERE LOGIC
                double spellRadius = GameConfig.s(GameConfig.DS01_RADIUS);
                for (Obstacles obs : obstacles) {
                    double dist = Math.hypot(obs.getX() - p.getX(), obs.getY() - p.getY());
                    if (dist <= (spellRadius + obs.getRadius())) obs.destroy(); 
                }
            }

            if (durationTimer.isExpired()) {
                durationTimer.stop();
                
                // --- THE REWARD LOGIC ---
                // Set cooldown based on how we just used the ability
                double cdTime = usedResonance ? GameConfig.DS01_COMBO_COOLDOWN : GameConfig.DS01_COOLDOWN;
                cooldownTimer.setDuration(cdTime);
                
                cooldownTimer.reset();
                cooldownTimer.start();
                
                if (p instanceof GenericPlayer) ((GenericPlayer) p).setAnimation("Dash");
            }
        }
    }

    // Capture/Restore need to save the usedResonance flag for Rewind safety!
    @Override
    public Object captureState() {
        return new Object[]{
            durationTimer.getRemainingFrames(), cooldownTimer.getRemainingFrames(),
            durationTimer.isActive() ? 1 : 0, cooldownTimer.isActive() ? 1 : 0,
            usedResonance ? 1 : 0, cooldownTimer.getTotalFrames()
        };
    }

    @Override
    public void restoreState(Object state) {
        Object[] s = (Object[]) state;
        durationTimer.setRemainingFrames((int)s[0]);
        cooldownTimer.setRemainingFrames((int)s[1]);
        if ((int)s[2] == 1) durationTimer.start(); else durationTimer.stop();
        
        // Restore the specific CD timer duration that was active
        cooldownTimer.setDuration((int)s[5] / 60.0);
        if ((int)s[3] == 1) cooldownTimer.start(); else cooldownTimer.stop();
        
        usedResonance = ((int)s[4] == 1);
    }
    
    @Override public void cancel() { durationTimer.stop(); cooldownTimer.stop(); }
    @Override public String getKeybind() { return GameConfig.DS01_BUTTON; }
    @Override public String getDisplayLabel() { return "V"; }
    @Override public boolean isActive() { return durationTimer.isActive(); }
    @Override public boolean isCooldownActive() { return cooldownTimer.isActive(); }
    @Override public double getActivePercent() { return durationTimer.isActive() ? (1.0 - durationTimer.getPercentComplete()) : 0.0; }
    @Override public double getCooldownPercent() { return cooldownTimer.isActive() ? cooldownTimer.getPercentComplete() : 0.0; }
}