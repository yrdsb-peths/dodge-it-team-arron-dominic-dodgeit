import greenfoot.*;

public class FX_RewindOverlay extends Actor {
    private int frame = 0;
    
    // 1. MAKE THESE STATIC! (This is the magic fix)
    // Static means they are kept in memory forever, not recreated every time you press R.
    private static GreenfootImage screen1;
    private static GreenfootImage screen2;

    public FX_RewindOverlay() {
        // 2. Only draw them if we haven't drawn them yet!
        if (screen1 == null || screen2 == null) {
            screen1 = createOverlay(false);
            screen2 = createOverlay(true);
        }
        setImage(screen1);
    }

    public void act() {
        frame++;
        // Just swap the image, don't DRAW anything new
        if (frame % 4 < 2) {
            setImage(screen1);
        } else {
            setImage(screen2);
        }
    }

    // Notice we made this method static too!
    private static GreenfootImage createOverlay(boolean scanlineOffset) {
        int w = GameConfig.WORLD_WIDTH;
        int h = GameConfig.WORLD_HEIGHT;
        GreenfootImage img = new GreenfootImage(w, h);
        
        // Blue tint
        img.setColor(new Color(60, 100, 200, 60));
        img.fill();
        
        // Scanlines
        img.setColor(new Color(0, 0, 0, 50));
        int startY = scanlineOffset ? GameConfig.s(2) : 0;
        for (int y = startY; y < h; y += GameConfig.s(4)) {
            img.fillRect(0, y, w, GameConfig.s(2));
        }
        return img;
    }
    
    // 3. Pre-load helper so we don't even lag the VERY FIRST time you press R
    public static void preLoad() {
        if (screen1 == null) {
            screen1 = createOverlay(false);
            screen2 = createOverlay(true);
        }
    }
}