
import greenfoot.*;

public class Ability_StandPunch {
    private GameTimer durationTimer = new GameTimer(GameConfig.WORLD_PUNCH_DURATION, false);
    private GameTimer cooldownTimer = new GameTimer(GameConfig.WORLD_PUNCH_COOLDOWN, false);
    public int standAnimSpeed = 2;
    
    public void activate(Dio dio, MyWorld world) {
        if (!durationTimer.isActive() && !cooldownTimer.isActive()) {
            durationTimer.reset();
            durationTimer.start();
            
            dio.setAnimation("Idle");
            world.addObject(new TheWorldStand(this), dio.getX(), dio.getY());
            
            AudioManager.play("muda_barrage");
        }
    }

    public void update(Dio dio, MyWorld world) {
        if (cooldownTimer.isActive()) cooldownTimer.update(world);
        if (!durationTimer.isActive()) return;
        
        durationTimer.update(world);

        // If the ability expires OR the player died, stop the ability
        if (durationTimer.isExpired() || dio.isDead()) {
            stopAbility(dio);
        }
    }
    
    // New helper to handle clean shut-down
    private void stopAbility(Dio dio) {
        durationTimer.stop();
        if (!dio.isDead()) {
            cooldownTimer.reset();
            cooldownTimer.start();
            dio.setAnimation("Dash");
        }
    }

    // This is called by Dio.die() to make sure the Stand disappears immediately
    public void cancel() {
        durationTimer.stop();
        // TheWorldStand looks at isActive(), so it will remove itself next frame
    }

    public boolean isActive() { 
        return durationTimer.isActive() && !durationTimer.isExpired(); 
    }
    
    // ... (Keep your existing Time Machine getters/setters below) ...
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