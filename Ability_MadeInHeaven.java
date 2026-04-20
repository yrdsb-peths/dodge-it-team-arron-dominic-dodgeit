import greenfoot.*;

public class Ability_MadeInHeaven implements Ability{
    private GameTimer durationTimer = new GameTimer(4.8, false); // Lasts 6.5 seconds to match the sound effect
    private int afterimageCounter = 0;
    private final double speedMultiplier = 2; // How much faster?
    
    
    private final int ACCELERATED_TICK_SPEED = GameConfig.MIH_TICK_SPEED;//Everything moves slow as you speed up
    private final int NORMAL_TICK_SPEED = GameConfig.NORMAL_TICK_SPEED;
    
    private GameTimer cooldownTimer = new GameTimer(GameConfig.MIH_COOLDOWN, false);

    public void activate(Player p, MyWorld world) {
        if (durationTimer.isExpired() || !durationTimer.isActive()&& !cooldownTimer.isActive()) {
            durationTimer.reset();
            durationTimer.start();
            Greenfoot.setSpeed(ACCELERATED_TICK_SPEED);
            AudioManager.play("speed_up_time");
        }
    }

    public void update(Player p, MyWorld world) {
        //Countdown cooldown
        if (cooldownTimer.isActive()) {
            cooldownTimer.update(world);
        }
        
        if (!durationTimer.isActive()) return;
        
        //Do not spawn afterimage when underground
        if (p instanceof GenericPlayer && ((GenericPlayer)p).isHidden()) return;
        
        durationTimer.update(world);
    
        // Spawn afterimage every frame
        afterimageCounter++;
        if (afterimageCounter % 1 == 0) {
            world.addObject(new FX_Afterimage(p.getImage()), p.getX(), p.getY());
        }

        if (durationTimer.isExpired()) {
            durationTimer.stop();
            Greenfoot.setSpeed(NORMAL_TICK_SPEED);
            
            cooldownTimer.reset();
            cooldownTimer.start();
        }
    }

    public double getSpeedMultiplier() {
        return durationTimer.isActive() && !durationTimer.isExpired() ? speedMultiplier : 1.0;
    }
    
    
    //Override ability methods
    
    public void cancel() { 
        durationTimer.stop(); 
        Greenfoot.setSpeed(GameConfig.NORMAL_TICK_SPEED);
    }
    
    public boolean isCooldownActive() { return cooldownTimer.isActive(); } 
    
    public double getActivePercent() { 
        return durationTimer.isActive() ? (1.0 - durationTimer.getPercentComplete()) : 0.0; 
    }
    
    
    public double getCooldownPercent() { 
        return cooldownTimer.isActive() ? cooldownTimer.getPercentComplete() : 0.0; 
    }
    
    public String getKeybind() { return GameConfig.MIH_BUTTON; }
    // For the Time Machine snapshots
    // These allow the Time Machine to save and load the ability's progress
    public int getRemainingFrames() { return durationTimer.getRemainingFrames(); }
    public void setRemainingFrames(int f) { durationTimer.setRemainingFrames(f); }
    
    public boolean isActive() { return durationTimer.isActive(); }
    public void startTimer() { durationTimer.start(); }
    public void stopTimer() { durationTimer.stop(); }
    
    
    public Object captureState() {
        return new int[]{
            durationTimer.getRemainingFrames(), cooldownTimer.getRemainingFrames(),
            durationTimer.isActive() ? 1 : 0, cooldownTimer.isActive() ? 1 : 0
        };
    }

    public void restoreState(Object state) {
        int[] data = (int[]) state;
        durationTimer.setRemainingFrames(data[0]);
        cooldownTimer.setRemainingFrames(data[1]);
        if (data[2] == 1) durationTimer.start(); else durationTimer.stop();
        if (data[3] == 1) cooldownTimer.start(); else cooldownTimer.stop();
    }
    
    public String getDisplayLabel() { return "S"; }//Ability Cooldown display symbol
}