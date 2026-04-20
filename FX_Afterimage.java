import greenfoot.*;

public class FX_Afterimage extends Actor {
    private int alpha = 150; // Start semi-transparent

    public FX_Afterimage(GreenfootImage playerImg) {
        // Create a copy so we don't ruin the original player image
        GreenfootImage img = new GreenfootImage(playerImg);
        
        // --- THE NEGATIVE (负片) EFFECT ---
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                Color c = img.getColorAt(x, y);
                // If it's not transparent, invert it
                if (c.getAlpha() > 0) {
                    img.setColorAt(x, y, new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue(), c.getAlpha()));
                }
            }
        }
        setImage(img);
    }

    public void act() {
        // Fade out
        alpha -= 7;
        if (alpha <= 0) {
            getWorld().removeObject(this);
        } else {
            getImage().setTransparency(alpha);
        }
    }
}