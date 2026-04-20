import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class Player here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public abstract class Player extends Actor
{
    protected boolean isDead = false;
    //Every player class must have die, movement and animation  logic.
    //Currently we only have DIO, but new playable characters can be added!
    
    //Die method is public because anyone can tell player to die
    public abstract void die();
    //Other methods are protected, meaning subclasses can access it but not the rest of the world
    protected abstract void movementLogic();
    protected abstract void animationLogic();
    //This is for hitbox controlling
    public abstract boolean checkCustomHitbox(Actor attacker, double padding);
    //Keep track of banner (for dio only)
    protected boolean bannerSpawned = false; 
    /**
     * Act - do whatever the Player wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    public void act()
    {
        MyWorld world = (MyWorld) getWorld();
        if(world == null) return;
        
        //Only update normally if game is in playing state
        if(!world.getGSM().isState(PlayingState.class)){
            //If it is in paused state, we can carry out some special things
            //But normally nothing happens: nobody can move.
            if(world.getGSM().isState(PausedState.class)){
            onPauseUpdate(world);
            }
            return;
        }
        //This banner is for DIO only. I need to tell him he can play banenr again.
        bannerSpawned = false; 
        movementLogic();    
        animationLogic();
    }
    
    public boolean isDead() {
        return isDead;
    }
    
    //Normally, players do nothing while being paused
    //This method can be overwritten to do something werid during pause state.
    protected void onPauseUpdate(MyWorld world) {}
    
}
