import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)


public class HeroMaybe extends Actor
{
    private int health = 3;
    private int speed = 2;
    
    private GreenfootImage[] walkFrames;
    private GreenfootImage[] walkFramesLeft;
    
    private int currentFrame = 0;
    private int animationTimer = 0;
    private boolean isMoving = false;
    private boolean facingRight = true;
    
    public HeroMaybe()
    {
        walkFrames = new GreenfootImage[8];
        for (int i = 0; i < 8; i++)
        {
            walkFrames[i] = new GreenfootImage("sneaky" + i + ".png");
            walkFrames[i].scale(60, 80);
        }
        
        //Mirror the same for left-facing
        
        walkFramesLeft = new GreenfootImage[8];
        for (int i = 0; i < 8; i++)
        {
            walkFramesLeft[i] = new GreenfootImage(walkFrames[i]);
            walkFramesLeft[i].mirrorHorizontally();
        }
        
        setImage(walkFrames[0]);
    }
    
    public void act()
    {
        checkKeys();
        animate();
    }
    
    private void checkKeys()
    {
        isMoving = false;
        
        if (Greenfoot.isKeyDown("a") || Greenfoot.isKeyDown("left"))
        {
            setLocation(getX() - speed, getY());
            isMoving = true;
            facingRight = false;
        }
        if (Greenfoot.isKeyDown("d") || Greenfoot.isKeyDown("right"))
        {
            setLocation(getX() + speed, getY());
            isMoving = true;
            facingRight = true;
        }
        if (Greenfoot.isKeyDown("w") || Greenfoot.isKeyDown("up"))
        {
            setLocation(getX(), getY() - speed);
            isMoving = true;
        }
        if (Greenfoot.isKeyDown("s") || Greenfoot.isKeyDown("Down"))
        {
            setLocation(getX(), getY() + speed);
            isMoving = true;
        }
    }
    
    private void animate()
    {
        if (!isMoving)
        {
            //stand still on first frame when not moving
            setImage(facingRight ? walkFrames[0] : walkFramesLeft[0]);
            currentFrame = 0;
            animationTimer = 0;
            return;
        }
        
        animationTimer++;
        if (animationTimer % 5 == 0)
        {
            setImage(facingRight ? walkFrames[currentFrame] : walkFramesLeft[currentFrame]);
            currentFrame++;
            if (currentFrame >= walkFrames.length) currentFrame = 0;
        }
    }
}
