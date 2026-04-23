import greenfoot.*;

public class FX_MenuAmbulance extends Actor {
    
    private int speed;

    public FX_MenuAmbulance() {
        // Note: Spelling matches your Train.java file ("ambulence.png")
        GreenfootImage img = new GreenfootImage("obstacles/ambulence.png");
        
        // Make it 2x larger than the normal Train size!
        img.scale(GameConfig.TRAIN_WIDTH * 1, GameConfig.TRAIN_WIDTH * 1);
        setImage(img);
        
        // Give it a random speed 
        speed = GameConfig.s(5 + Greenfoot.getRandomNumber(5));
    }

    @Override
    public void act() {
        // Fly left across the screen
        setLocation(getX() - speed, getY());

        // Remove itself once it is safely off-screen to the left
        if (getX() < -GameConfig.s(300)) {
            if (getWorld() != null) {
                getWorld().removeObject(this);
            }
        }
    }
}