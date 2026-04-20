import greenfoot.*;

public class GameTimer {
    private int totalFrames;
    private int remainingFrames;
    private boolean active;
    private boolean loop;

    /**
     * @param seconds How many real-world seconds the timer should run for.
     * @param loop Whether the timer should be looped. True for looped , false for one time uses.
     */
    public GameTimer(double seconds, boolean loop) {
        // Greenfoot's default speed is approx 60 acts per second
        this.totalFrames = (int)(seconds * 60);
        this.remainingFrames = totalFrames;
        this.loop = loop;
        this.active = false;
    }
    /*
     * Sample on how to use timer for an ability with a cooldown of 5 seconds:
     * 
     * --------
     * private GameTimer abilityTimer = new GameTimer(5.0, false);
     * 
     * if (Greenfoot.isKeyDown("space") && ability.isExpired()) {
            some_ability_with_cooldown();
            abilityTimer.reset(); // Refill the cooldown
            abilityTimer.start(); // Start the countdown
        }   
        -------
        
        Sample on how to use timer for spawning regularly:
        
        --------
        private GameTimer spawnTimer = new GameTimer(1.5, true); 

        public void update(MyWorld world) {
        // Start it once
        spawnTimer.start(); 
        
        // This only ticks if the game is NOT paused
        spawnTimer.update(world); 

        if (spawnTimer.isExpired()) {
            world.addObject(new Roadroller(), 800, 300);
            // NOTE: No need to call reset() because loop is true! 
            // It refills itself automatically.
        }
        }
        --------
     */

    public void start() {
        this.active = true;
    }

    public void stop() {
        this.active = false;
    }

    public void reset() {
        this.remainingFrames = totalFrames;
    }

    /**
     * This is the engine of the timer. 
     * It should be called in the act() method of the Actor or the update() of a State.
     */
    public void update(MyWorld world) {
        // THE ROBUST PART: Only tick down if the game isn't paused
        // and (optionally) if time isn't stopped by DIO.
        if (active && world.getGSM().isState(PlayingState.class)) {
            if (remainingFrames > 0) {
                remainingFrames--;
            } else if (loop) {
                reset();
            } else {
                active = false;
            }
        }
    }

    /*
     * Returns if time is up
     */
    public boolean isExpired() {
        return remainingFrames <= 0;
    }
    
    /*
     * Returns time remaing in seconds
     */
    public double getSecondsRemaining() {
        return remainingFrames / 60.0;
    }
    
    /*
     * Returns percentage of time passed.
     * 
     * Sample of using percent complete:
     *  Inside HUD class
        double progress = abilityTimer.getPercentComplete(); 
        // Width of bar gets smaller as timer counts down
        int barWidth = (int)(100 * (1.0 - progress)); 
        img.fillRect(10, 10, barWidth, 10);
     */
    public double getPercentComplete() {
        return (double)(totalFrames - remainingFrames) / totalFrames;
    }
    
    /*
     * Resets the duration of the timer
     */
    public void setDuration(double seconds) {
        this.totalFrames = (int)(seconds * 60);
        reset();
    }
    
    //Count time even if game is paused.
    public void forceTick() {
    if (remainingFrames > 0) remainingFrames--;
    }
    
    public int getRemainingFrames() { return remainingFrames; }
    public void setRemainingFrames(int frames) { this.remainingFrames = frames; }
    public boolean isActive() { return active; }
    public int getTotalFrames() { return totalFrames; }
}