import greenfoot.*;

public class FX_DimOverlay extends Actor {
    public FX_DimOverlay(int width, int height) {
        GreenfootImage img = new GreenfootImage(width, height);
        img.setColor(new Color(0, 0, 0, 140)); // Semi-transparent black
        img.fill();
        setImage(img);
    }
}