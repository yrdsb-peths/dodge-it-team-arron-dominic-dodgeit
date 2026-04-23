import greenfoot.*;

public class FX_ShatteredGlass extends Actor {
    private int life = 255;
    private int vx, vy;

    public FX_ShatteredGlass() {
        int size = GameConfig.s(15 + Greenfoot.getRandomNumber(20));
        GreenfootImage img = new GreenfootImage(size, size);
        
        // Random crimson/white shard colors
        Color shardColor = (Greenfoot.getRandomNumber(2) == 0) 
            ? new Color(255, 30, 60, 200) 
            : new Color(255, 255, 255, 180);
            
        img.setColor(shardColor);
        
        // Draw a random triangle shard
        int[] x = { 0, size/2, size };
        int[] y = { size, 0, size/2 + Greenfoot.getRandomNumber(size/2) };
        img.fillPolygon(x, y, 3);
        setImage(img);
        
        // Explosion velocity
        vx = Greenfoot.getRandomNumber(10) - 5;
        vy = Greenfoot.getRandomNumber(10) - 5;
    }

    @Override
    public void act() {
        setLocation(getX() + vx, getY() + vy);
        life -= 15; // Rapid fade out
        if (life <= 0) {
            if (getWorld() != null) getWorld().removeObject(this);
        } else {
            getImage().setTransparency(life);
        }
    }
}