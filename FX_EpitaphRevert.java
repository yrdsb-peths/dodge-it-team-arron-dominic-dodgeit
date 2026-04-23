import greenfoot.*;

public class FX_EpitaphRevert extends Actor {
    private int alpha = 200;

    public FX_EpitaphRevert() {
        GreenfootImage img = new GreenfootImage(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        // Cyan color to represent returning to the stable past
        img.setColor(new Color(0, 255, 255)); 
        img.fill();
        setImage(img);
    }

    @Override
    public void act() {
        alpha -= 20; // Slightly slower fade than the commit snap
        if (alpha <= 0) {
            if (getWorld() != null) getWorld().removeObject(this);
        } else {
            getImage().setTransparency(alpha);
        }
    }
}