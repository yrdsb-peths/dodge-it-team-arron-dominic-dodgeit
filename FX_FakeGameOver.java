import greenfoot.*;

public class FX_FakeGameOver extends Actor {
    public FX_FakeGameOver() {
        // Create a screen-sized image
        GreenfootImage img = new GreenfootImage(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        
        // 1. Dark background
        img.setColor(new Color(0, 0, 0, 200));
        img.fill();
        
        // 2. Write "GAME OVER" in the middle
        img.setColor(Color.RED);
        img.setFont(new Font("Serif", true, false, GameConfig.s(60)));
        img.drawString("GAME OVER", GameConfig.WORLD_WIDTH/2 - GameConfig.s(150), GameConfig.WORLD_HEIGHT/2);
        
        // 3. Add a "Glitchy" King Crimson red tint
        img.setColor(new Color(255, 0, 0, 50));
        img.fill();
        
        setImage(img);
    }
}