import greenfoot.*;

public class FX_DarkLane extends Actor {
    private int life = 30; // Lasts longer than a pulse to match the freeze vibe

    public FX_DarkLane() {
        // Full screen width, lane height
        GreenfootImage img = new GreenfootImage(GameConfig.WORLD_WIDTH, GameConfig.LANE_HEIGHT);
        img.setColor(new Color(0, 50, 100, 120)); // Deep dark blue for freeze
        img.fill();
        setImage(img);
    }

    public void act() {
        life--;
        if (life <= 0) {
            if (getWorld() != null) getWorld().removeObject(this);
        } else {
            getImage().setTransparency(life * 4); // Fades out slowly
        }
    }
}