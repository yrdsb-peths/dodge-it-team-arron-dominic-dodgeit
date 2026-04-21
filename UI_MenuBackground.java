import greenfoot.*;

public class UI_MenuBackground extends Actor {
    public UI_MenuBackground(String imageName) {
        GreenfootImage img = new GreenfootImage(imageName);
        // Scale to the full world size
        img.scale(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        setImage(img);
    }
}