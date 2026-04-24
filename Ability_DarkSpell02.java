import greenfoot.*;
import java.util.List;

public class Ability_DarkSpell02 implements Ability {  

    private GameTimer durationTimer = new GameTimer(GameConfig.DS02_DURATION, false);
    private GameTimer cooldownTimer = new GameTimer(GameConfig.DS02_COOLDOWN, false);
    private boolean keyWasDown = false;
    
    public static int CURRENT_FROZEN_LANE_Y = -1;
        
    private static final int POOL = GameConfig.MAX_REWIND_TIME + 10; // 370 slots
    private int[][] statePool = new int[POOL][5]; // 5 = however many values you store
    private int poolIdx = 0;
    @Override
    public void activate(Player p, MyWorld world) {
        if (durationTimer.isActive() || cooldownTimer.isActive() || keyWasDown) return;

        keyWasDown = true;
        durationTimer.reset();
        durationTimer.start();
        
        if (p instanceof GenericPlayer) {
            ((GenericPlayer) p).setAnimation("DarkSpell_02");
        }
        
        AudioManager.play("night_spell2");

        


        // Calculate Lane
        int targetLaneY = GameConfig.LANES[0];
        int minDiff = Integer.MAX_VALUE;
        for (int laneY : GameConfig.LANES) {
            int diff = Math.abs(p.getY() - laneY);
            if (diff < minDiff) {
                minDiff = diff;
                targetLaneY = laneY;
            }
        }
        CURRENT_FROZEN_LANE_Y = targetLaneY;
        world.addObject(new FX_DarkLane(), world.getWidth() / 2, targetLaneY);
    }
    
    @Override
    public void update(Player p, MyWorld world) {
        cooldownTimer.update(world);
        if (!Greenfoot.isKeyDown(getKeybind())) keyWasDown = false;

        if (durationTimer.isActive()) {
            durationTimer.update(world);

            List<Obstacles> obstacles = world.getObjects(Obstacles.class);
            for (Obstacles obs : obstacles) {
                if (Math.abs(obs.getY() - CURRENT_FROZEN_LANE_Y) < 10) {
                    // --- THE FIX: "HOLD" LOGIC ---
                    // Instead of a huge number, we set the freeze to 2.
                    // This "refreshes" the freeze every frame the spell is active.
                    // The moment the spell ends, the car only has 2 frames left!
                    obs.freeze(2); 
                }
            }

            if (durationTimer.isExpired()) {
                stopAbility(p);
            }
        }
    }
    
    private void stopAbility(Player p) {
        durationTimer.stop();
        CURRENT_FROZEN_LANE_Y = -1; // CRITICAL: Clear the static curse
        cooldownTimer.reset();
        cooldownTimer.start();
        if (p instanceof GenericPlayer) ((GenericPlayer) p).setAnimation("Dash");
    }
    
    @Override
    public void cancel() { 
        durationTimer.stop(); 
        cooldownTimer.stop(); 
        CURRENT_FROZEN_LANE_Y = -1; 
    }
    
    @Override public String getKeybind()      { return GameConfig.DS02_BUTTON; }
    @Override public String getDisplayLabel() { return "C"; }
    @Override public boolean isActive()       { return durationTimer.isActive(); }
    @Override public boolean isCooldownActive() { return cooldownTimer.isActive(); }
    
    @Override public double getActivePercent() { 
        return durationTimer.isActive() ? (1.0 - durationTimer.getPercentComplete()) : 0.0; 
    }
    @Override public double getCooldownPercent() { 
        return cooldownTimer.isActive() ? cooldownTimer.getPercentComplete() : 0.0; 
    }

    @Override
    public Object captureState() {
        int[] s = statePool[poolIdx++ % POOL];
        s[0] = durationTimer.getRemainingFrames();
        s[1] = cooldownTimer.getRemainingFrames();
        s[2] = durationTimer.isActive() ? 1 : 0;
        s[3] = cooldownTimer.isActive() ? 1 : 0;
        s[4] = CURRENT_FROZEN_LANE_Y;
        return s;
    }
    
        
    @Override
        public void restoreState(Object state) {
            int[] s = (int[]) state;
            durationTimer.setRemainingFrames(s[0]);
            cooldownTimer.setRemainingFrames(s[1]);
            if (s[2] == 1) durationTimer.start(); else durationTimer.stop();
            if (s[3] == 1) cooldownTimer.start(); else cooldownTimer.stop();
            
            // This line is the most important for the freeze bug!
            CURRENT_FROZEN_LANE_Y = s[4]; 
        }
    }