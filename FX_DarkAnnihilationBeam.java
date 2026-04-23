import greenfoot.*;

public class FX_DarkAnnihilationBeam extends Actor {
    private int life = 15;

    public FX_DarkAnnihilationBeam() {
        // WORLD_WIDTH wide, LANE_HEIGHT tall
        GreenfootImage img = new GreenfootImage(GameConfig.WORLD_WIDTH, GameConfig.LANE_HEIGHT);
        
        // Dark core
        img.setColor(new Color(20, 0, 40));
        img.fill();
        
        // Jagged energy edges
        img.setColor(new Color(130, 0, 255));
        for (int x = 0; x < GameConfig.WORLD_WIDTH; x += 10) {
            int offset = Greenfoot.getRandomNumber(20) - 10;
            img.fillRect(x, 0, 5, GameConfig.s(5) + offset); // Top jagged
            img.fillRect(x, img.getHeight() - GameConfig.s(5) + offset, 5, 10); // Bottom
        }
        
        setImage(img);
    }

    public void act() {
        life--;
        // Randomly flicker width to look like unstable energy
        if (life > 0) {
            getImage().setTransparency(life * 15);
            // Slight vertical jitter
            setLocation(getX(), getY() + Greenfoot.getRandomNumber(3) - 1);
        } else {
            if (getWorld() != null) getWorld().removeObject(this);
        }
    }
}