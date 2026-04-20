import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)


public class HeroMaybe extends Actor
{
    private int health = 3;
    private int speed = 2;
    
    private GreenfootImage[] walkFrames;
    
    public HeroMaybe()
    {
        walkFrames = new GreenfootImage[8];
        for (int i = 0; i < 8; i++)
        {
            walkFrames[i] = new GreenfootImage("Sneaky" + i + ".png");
        }
    }
    
    public void act()
    {
        // Add your action code here.
    }
}
