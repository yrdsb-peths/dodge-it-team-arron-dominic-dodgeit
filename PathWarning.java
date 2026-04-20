import greenfoot.*;

public class PathWarning extends Actor implements Time_Snapshottable {
    private int timer = 60; // How long the warning stays (1 second)

    public PathWarning(int width, int height) {
        GreenfootImage img = new GreenfootImage(width, height);
        img.setColor(new Color(255, 0, 0, 100)); // Semi-transparent red
        img.fill();
        setImage(img);
    }

    public void act() {
        // Standard GSM check so it freezes during Time Stop
        MyWorld world = (MyWorld) getWorld();
        if (world == null || !world.getGSM().isState(PlayingState.class)) return;

        timer--;
        
        // Flashing effect: gets more intense as time runs out
        if (timer % 10 < 5) {
            getImage().setTransparency(40);
        } else {
            getImage().setTransparency(140);
        }

        if (timer <= 0) {
            world.removeObject(this);
        }
    }
    
    // --- TIME MACHINE ADDITIONS ---
    public Time_ActorMemento captureState() {
        return new Time_ActorMemento(this, getX(), getY(), timer);
    }

    public void restoreState(Time_ActorMemento m) {
        this.timer = (int)m.customData;
    }
}