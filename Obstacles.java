import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class Obstacles here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public abstract class Obstacles extends Actor
{
    protected int speed;
    
    /**
     * Act - do whatever the Obstacles wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    public void act()
    {
        MyWorld world = (MyWorld) getWorld();
        if(world == null || !world.getGSM().isState(PlayingState.class))return;
        
        movementLogic();
        collisionLogic();
        checkRemove();
        
    }
    
    protected abstract void movementLogic();
    protected abstract void collisionLogic();
    protected abstract void checkRemove();
}
