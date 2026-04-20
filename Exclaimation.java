import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class Exclaimation here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Exclaimation extends Actor implements Time_Snapshottable {
    private GameTimer lifeTimer = new GameTimer(1.0, false);
    private Animator exclaimAnim;
    
    public Exclaimation(){
        exclaimAnim = new Animator("symbols", "exclaimation", 0.1 * GameConfig.SCALE);
        lifeTimer.start();
    }
    public void act()
    {
        // Add your action code here.
        setImage(exclaimAnim.getCurrentFrame());
        lifeTimer.update((MyWorld) getWorld());
        if(lifeTimer.isExpired()){
            getWorld().removeObject(this);
        }
    }
    
    // --- TIME MACHINE ADDITIONS ---
    public Time_ActorMemento captureState() {
        // Save the remaining frames of the timer
        return new Time_ActorMemento(this, getX(), getY(), lifeTimer.getRemainingFrames());
    }

    public void restoreState(Time_ActorMemento m) {
        // Restore the timer so it doesn't vanish or reset
        int remaining = (int)m.customData;
        lifeTimer.setRemainingFrames(remaining);
    }
}
