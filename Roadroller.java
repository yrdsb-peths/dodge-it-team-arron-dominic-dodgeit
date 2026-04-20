import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class Roadroller here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */

public class Roadroller extends Obstacles implements Time_Snapshottable
{
    private int scoreToAdd = 1;
    /**
     * Act - do whatever the Roadroller wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    private boolean faceLeft = false;
    private boolean resized = false;
    
    
    private static int globalSpeed = GameConfig.ROADROLLER_SPEED;
    private static final int MAX_SPEED = GameConfig.ROADROLLER_MAX_SPEED; // Safety cap
    
    //Can customise roadroller so that it doesnt give any score
    public Roadroller(int speed) {
        this(speed,1); // Calls the original Roadroller() constructor first
    }
    
     public Roadroller(int speed, int scoreToAdd) {
        this(); // Calls the default constructor to setup the image
        this.speed = speed; // Assign the speed from SpawnManager!
        this.scoreToAdd = scoreToAdd;
    }
    
    public Roadroller(){
        GreenfootImage img = new GreenfootImage("obstacles/road_roller.png");
        setImage(img);
        //Resizing and orienting the image
        getImage().mirrorHorizontally();
        getImage().scale(GameConfig.ROADROLLER_WIDTH, GameConfig.ROADROLLER_WIDTH);
        //Moves at 6 pixels per frame
        speed = GameConfig.ROADROLLER_SPEED;
    }
    
    public void movementLogic(){
        //Negative speed means moving from right to left
        move(-speed);
    }
    
    public void collisionLogic(){
    
        //I'm really afraid of that null pointer error so this is a safety check.
        //Also dont scream when im rewinding
        MyWorld world = (MyWorld) getWorld();
        if (getWorld() == null|| world.isRewinding()) return; 

        // Get the player directly (no need to use isTouching first, this is faster)
        Player player = (Player) getOneIntersectingObject(Player.class);
        if (player != null && !player.isDead()) {
            if (player.checkCustomHitbox(this, 0.8)) {
                player.die();
            }
        }
    }   
    
    public void checkRemove(){
        //Check remove is checked separately and lastly
        //because this avoids calling world after removing itself from world
        //which results in null pointer error (yikes!)
        if (getX() <= 0) {
            //When it reaches the end, AND if player is alive, add score. 
            java.util.List<Player> players = getWorld().getObjects(Player.class);
            if (!players.isEmpty()) {
                Player player = players.get(0);
                if (!player.isDead()) {
                    ScoreManager.addScore(scoreToAdd);
                }
            }
            
            getWorld().removeObject(this);
            
        }
        return;
    }
    
    // Time machine stuff for time rewinding.

    //These are the custom data stored for roadrollers
    private static class RoadrollerData {
        int speed, scoreToAdd;
        RoadrollerData(int speed, int scoreToAdd) {
            this.speed = speed;
            this.scoreToAdd = scoreToAdd;
        }
    }
    
    //Captures the current state ands sends them back as a momento
    public Time_ActorMemento captureState() {
        return new Time_ActorMemento(this, getX(), getY(), 
            new RoadrollerData(speed, scoreToAdd));
    }
    
    //Restores state according to the momento
    public void restoreState(Time_ActorMemento m) {
        RoadrollerData d = (RoadrollerData) m.customData;
        this.speed = d.speed;
        this.scoreToAdd = d.scoreToAdd;
    }
}
