
import greenfoot.*;

public class Ability_StandPunch implements Ability {
    private GameTimer durationTimer = new GameTimer(GameConfig.WORLD_PUNCH_DURATION, false);
    private GameTimer cooldownTimer = new GameTimer(GameConfig.WORLD_PUNCH_COOLDOWN, false);
    public int standAnimSpeed = 2;
    
    public void activate(Player p, MyWorld world) {
        if (!durationTimer.isActive() && !cooldownTimer.isActive()) {
            durationTimer.reset();
            durationTimer.start();
            
            p.setAnimation("Idle");
            world.addObject(new TheWorldStand(this), p.getX(), p.getY());
            
            AudioManager.play("muda_barrage");
        }
    }

    public void update(Player p, MyWorld world) {
        if (cooldownTimer.isActive()) cooldownTimer.update(world);
        if (!durationTimer.isActive()) return;
        
        durationTimer.update(world);

        // If the ability expires OR the player died, stop the ability
        if (durationTimer.isExpired() || p.isDead()) {
            stopAbility(p);
        }
    }
    
    // New helper to handle clean shut-down
    private void stopAbility(Player p) {
        durationTimer.stop();
        if (!p.isDead()) {
            cooldownTimer.reset();
            cooldownTimer.start();
            p.setAnimation("Dash");//
        }
    }
    
    // This is called by Dio.die() to make sure the Stand disappears immediately
    public void cancel() {
        durationTimer.stop();
        // TheWorldStand looks at isActive(), so it will remove itself next frame
    }
    //Overrding ability
    public boolean isActive() { return durationTimer.isActive() && !durationTimer.isExpired(); }
    public boolean isCooldownActive() { return cooldownTimer.isActive(); }
    public double getActivePercent() { 
        return durationTimer.isActive() ? (1.0 - durationTimer.getPercentComplete()) : 0.0; 
    }
    public double getCooldownPercent() { 
        return cooldownTimer.isActive() ? cooldownTimer.getPercentComplete() : 0.0; 
    }
    
    public String getKeybind() { return GameConfig.STAND_PUNCH_BUTTON; }
    // --- Time Machine ---
    public Object captureState() {
        return new int[]{durationTimer.getRemainingFrames(), cooldownTimer.getRemainingFrames(), 
                         durationTimer.isActive() ? 1 : 0, cooldownTimer.isActive() ? 1 : 0};
    }

    public void restoreState(Object state) {
        int[] data = (int[]) state;
        durationTimer.setRemainingFrames(data[0]);
        cooldownTimer.setRemainingFrames(data[1]);
        if (data[2] == 1) durationTimer.start(); else durationTimer.stop();
        if (data[3] == 1) cooldownTimer.start(); else cooldownTimer.stop();
    }
    
    public String getDisplayLabel() { return "E"; }//Ability Cooldown display symbol
    //Time machine getters setters
    public int getDurFrames() { return durationTimer.getRemainingFrames(); }
    public void setDurFrames(int f) { durationTimer.setRemainingFrames(f); }
    public boolean isDurActive() { return durationTimer.isActive(); }
    public void startDur() { durationTimer.start(); }
    public void stopDur() { durationTimer.stop(); }
    public int getCoolFrames() { return cooldownTimer.getRemainingFrames(); }
    public void setCoolFrames(int f) { cooldownTimer.setRemainingFrames(f); }
    public boolean isCoolActive() { return cooldownTimer.isActive(); }
    public void startCool() { cooldownTimer.start(); }
    public void stopCool() { cooldownTimer.stop(); }
    
}