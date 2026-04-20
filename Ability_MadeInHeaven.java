import greenfoot.*;

public class Ability_MadeInHeaven {
    private GameTimer durationTimer = new GameTimer(4.8, false); // Lasts 6.5 seconds to match the sound effect
    private int afterimageCounter = 0;
    private final double speedMultiplier = 2; // How much faster?
    
    
    private final int ACCELERATED_TICK_SPEED = GameConfig.MIH_TICK_SPEED;//Everything moves slow as you speed up
    private final int NORMAL_TICK_SPEED = GameConfig.NORMAL_TICK_SPEED;

    public void activate() {
        if (durationTimer.isExpired() || !durationTimer.isActive()) {
            
            durationTimer.reset();
            durationTimer.start();
            Greenfoot.setSpeed(ACCELERATED_TICK_SPEED);
            AudioManager.play("speed_up_time");
        }
    }

    public void update(Player p, MyWorld world) {
        if (!durationTimer.isActive()) return;
        
        durationTimer.update(world);

        // Spawn afterimage every frame
        afterimageCounter++;
        if (afterimageCounter % 1 == 0) {
            world.addObject(new FX_Afterimage(p.getImage()), p.getX(), p.getY());
        }

        if (durationTimer.isExpired()) {
            durationTimer.stop();
            Greenfoot.setSpeed(NORMAL_TICK_SPEED);
        }
    }

    public double getSpeedMultiplier() {
        return durationTimer.isActive() && !durationTimer.isExpired() ? speedMultiplier : 1.0;
    }
    
    // For the Time Machine snapshots
    // These allow the Time Machine to save and load the ability's progress
    public int getRemainingFrames() { return durationTimer.getRemainingFrames(); }
    public void setRemainingFrames(int f) { durationTimer.setRemainingFrames(f); }
    
    public boolean isActive() { return durationTimer.isActive(); }
    public void startTimer() { durationTimer.start(); }
    public void stopTimer() { durationTimer.stop(); }
}