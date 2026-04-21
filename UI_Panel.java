import greenfoot.*;

public class UI_Panel extends Actor {
    public UI_Panel(int width, int height, Color color) {
        GreenfootImage img = new GreenfootImage(width, height);
        img.setColor(color);
        img.fill();
        
        // Add a thin white border at the top if it's the bottom UI deck
        if (color.getBlue() == 30) { 
            img.setColor(Color.WHITE);
            img.fillRect(0, 0, width, GameConfig.s(2));
        }
        setImage(img);
    }
}