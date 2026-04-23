import greenfoot.*;

public class FX_ErasureSnap extends Actor {
    private int alpha = 255;

    public FX_ErasureSnap() {
        GreenfootImage img = new GreenfootImage(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        img.setColor(Color.WHITE);
        img.fill();
        setImage(img);
    }

    @Override
    public void act() {
        alpha -= 25; // Fades out very quickly (~10 frames)
        if (alpha <= 0) {
            if (getWorld() != null) getWorld().removeObject(this);
        } else {
            getImage().setTransparency(alpha);
        }
    }
}