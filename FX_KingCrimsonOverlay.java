import greenfoot.*;

public class FX_KingCrimsonOverlay extends Actor {

    private static GreenfootImage frame1;
    private static GreenfootImage frame2;
    private GreenfootImage displayImage;
    private int frameTick = 0;

    public FX_KingCrimsonOverlay() {
        if (frame1 == null || frame2 == null) {
            frame1 = buildOverlay(false);
            frame2 = buildOverlay(true);
        }
        displayImage = new GreenfootImage(frame1);
        setImage(displayImage);
    }

    @Override
    public void act() {
        if (!Ability_KingCrimson.ERASING) {
            if (getWorld() != null) getWorld().removeObject(this);
            return;
        }

        frameTick++;
        // Fast flicker: swap base image every 2 frames
        GreenfootImage base = (frameTick % 4 < 2) ? frame1 : frame2;
        displayImage = new GreenfootImage(base);

        // --- INTENSE RED RAMP ---
        double pct = Ability_KingCrimson.erasurePercent;
        int alpha;
        
        if (pct < 0.5) {
            alpha = 110; // High base visibility for the red tint
        } else {
            // Aggressive quadratic ramp: 110 -> 255
            // Becomes a solid red wall in the final 0.5 seconds
            double t = (pct - 0.5) / 0.5;
            alpha = 110 + (int)(t * t * 145); 
        }
        
        displayImage.setTransparency(Math.min(255, alpha));
        setImage(displayImage);
    }

    private static GreenfootImage buildOverlay(boolean offset) {
        int w = GameConfig.WORLD_WIDTH;
        int h = GameConfig.WORLD_HEIGHT;
        GreenfootImage img = new GreenfootImage(w, h);

        // 1. FILL: Violent Crimson
        img.setColor(new Color(220, 0, 0)); 
        img.fill();

        // 2. GRAIN: High-contrast static lines
        // Using semi-transparent white makes the "digital error" look pop
        img.setColor(new Color(255, 255, 255, 50)); 
        int grain = GameConfig.s(7); 
        int lineW = GameConfig.s(2);
        int startX = offset ? (grain / 2) : 0;

        for (int x = startX; x < w; x += grain) {
            img.fillRect(x, 0, lineW, h);
        }

        return img;
    }

    public static void preLoad() {
        if (frame1 == null) {
            frame1 = buildOverlay(false);
            frame2 = buildOverlay(true);
        }
    }
}