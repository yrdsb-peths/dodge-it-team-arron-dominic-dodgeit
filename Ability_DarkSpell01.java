import greenfoot.*;
import java.util.List;

public class Ability_DarkSpell01 implements Ability {

    private GameTimer durationTimer = new GameTimer(GameConfig.DS01_DURATION, false);
    private GameTimer cooldownTimer = new GameTimer(GameConfig.DS01_COOLDOWN, false);
    
    private boolean keyWasDown = false;
    private boolean hasResized = false;

    @Override
    public void activate(Player p, MyWorld world) {
        if (durationTimer.isActive() || cooldownTimer.isActive() || keyWasDown) return;
        
        keyWasDown = true;
        durationTimer.reset();
        durationTimer.start();
        
        if (p instanceof GenericPlayer) {
            GenericPlayer gp = (GenericPlayer) p;
            if (!hasResized) {
                gp.resizeAnimation("DarkSpell_01", GameConfig.s(GameConfig.DS01_IMAGE_SIZE));
                hasResized = true;
            }
            gp.setAnimation("DarkSpell_01");
        }
        AudioManager.play("night_spell1");
    }
    
    @Override
    public void update(Player p, MyWorld world) {
        cooldownTimer.update(world);
        
        if (!Greenfoot.isKeyDown(getKeybind())) keyWasDown = false;

        if (durationTimer.isActive()) {
            durationTimer.update(world);
            
            // PERSISTENT KILLING LOGIC
            double spellRadius = GameConfig.s(GameConfig.DS01_RADIUS);
            List<Obstacles> obstacles = world.getObjects(Obstacles.class);
            for (Obstacles obs : obstacles) {
                double distance = Math.hypot(obs.getX() - p.getX(), obs.getY() - p.getY());
                if (distance <= (spellRadius + obs.getRadius())) {
                    obs.destroy(); 
                }
            }

            if (durationTimer.isExpired()) {
                durationTimer.stop();
                cooldownTimer.reset();
                cooldownTimer.start();
                if (p instanceof GenericPlayer) ((GenericPlayer) p).setAnimation("Dash");
            }
        }
    }

    @Override public void cancel() { durationTimer.stop(); cooldownTimer.stop(); }
    @Override public String getKeybind() { return GameConfig.DS01_BUTTON; }
    @Override public String getDisplayLabel() { return "V"; }
    @Override public boolean isActive() { return durationTimer.isActive(); }
    @Override public boolean isCooldownActive() { return cooldownTimer.isActive(); }
    
    @Override public double getActivePercent() { 
        return durationTimer.isActive() ? (1.0 - durationTimer.getPercentComplete()) : 0.0; 
    }
    @Override public double getCooldownPercent() { 
        return cooldownTimer.isActive() ? cooldownTimer.getPercentComplete() : 0.0; 
    }

    @Override
    public Object captureState() {
        return new int[]{
            durationTimer.getRemainingFrames(), cooldownTimer.getRemainingFrames(),
            durationTimer.isActive() ? 1 : 0, cooldownTimer.isActive() ? 1 : 0
        };
    }

    @Override
    public void restoreState(Object state) {
        int[] s = (int[]) state;
        durationTimer.setRemainingFrames(s[0]);
        cooldownTimer.setRemainingFrames(s[1]);
        if (s[2] == 1) durationTimer.start(); else durationTimer.stop();
        if (s[3] == 1) cooldownTimer.start(); else cooldownTimer.stop();
    }
}