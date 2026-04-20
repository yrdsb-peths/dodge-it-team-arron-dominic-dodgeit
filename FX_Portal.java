import greenfoot.*;

/*
 * A brief zipper-portal flash drawn at the top or bottom screen edge.
 * Full screen width, just a thin strip that fades out.
 * Drawn programmatically.
 * 
 * teethFaceUp: true means teeth point upward (bottom portal), false = teeth point down (top portal)
 */
public class FX_Portal extends Actor {
    private int alpha = 255;
    private int width;
    private boolean teethFaceUp;

    public FX_Portal(int worldWidth, boolean teethFaceUp) {
        this.width = worldWidth;
        this.teethFaceUp = teethFaceUp;
        redraw();
    }

    public void act() {
        alpha -= 14; // fades in ~18 frames
        if (alpha <= 0) {
            if (getWorld() != null) getWorld().removeObject(this);
            return;
        }
        redraw();
    }

    private void redraw() {
        int h = GameConfig.s(18);
        GreenfootImage img = new GreenfootImage(width, h);

        // Purple/gold portal background
        img.setColor(new Color(160, 60, 220, Math.min(alpha, 200)));
        img.fill();

        // Zipper teeth along the inner edge
        int toothW = GameConfig.s(8);
        int toothH = GameConfig.s(7);
        img.setColor(new Color(255, 220, 0, alpha));

        int teethY = teethFaceUp ? 0 : h - toothH; // base of teeth
        int tipY   = teethFaceUp ? toothH : h - toothH; // direction

        for (int x = 0; x + toothW <= width; x += toothW) {
            int mid = x + toothW / 2;
            int[] xpts = {x, mid, x + toothW};
            int[] ypts = {teethFaceUp ? toothH : h - toothH,
                          teethFaceUp ? 0 : h,
                          teethFaceUp ? toothH : h - toothH};
            img.fillPolygon(xpts, ypts, 3);
        }

        img.setTransparency(alpha);
        setImage(img);
    }
}