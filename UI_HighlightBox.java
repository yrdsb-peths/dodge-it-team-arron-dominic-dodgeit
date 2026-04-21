import greenfoot.*;

public class UI_HighlightBox extends Actor {
    public UI_HighlightBox(int size) {
        GreenfootImage img = new GreenfootImage(size, size);
        img.setColor(Color.YELLOW);
        
        // Draw a thick 3-pixel border
        for(int i = 0; i < 3; i++) {
            img.drawRect(i, i, size - 1 - (i * 2), size - 1 - (i * 2));
        }
        setImage(img);
    }
}