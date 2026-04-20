import greenfoot.*;

public class FX_ZipperGround extends Actor {
    private int frame = 0;
    private static final int W = 70; 
    private static final int H = 12;

    public FX_ZipperGround() {
        redraw(0);
    }

    public void act() {
        MyWorld world = (MyWorld) getWorld();
        if (world == null || !world.getGSM().isState(PlayingState.class)) return;
        frame++;
        redraw(frame);
    }

    private void redraw(int f) {
        int w = GameConfig.s(W);
        int h = GameConfig.s(H);
        GreenfootImage img = new GreenfootImage(w, h);

        // Pulse: brightness oscillates between 180 and 255
        int brightness = 180 + (int)(75 * (0.5 + 0.5 * Math.sin(f * 0.1)));

        // ── Zipper spine & Teeth (Purple) ── 
        // We use (brightness, 0, brightness) for a vibrant purple pulse
        img.setColor(new Color(brightness, 0, brightness)); 
        
        // Spine
        img.fillRect(0, h/2 - GameConfig.s(1), w, GameConfig.s(2));

        // Teeth
        int toothW = GameConfig.s(6);
        int toothH = GameConfig.s(4);
        for (int x = 0; x + toothW <= w; x += toothW * 2) {
            int[] xpts = {x, x + toothW/2, x + toothW};
            int[] ypts = {h/2, h/2 - toothH, h/2};
            img.fillPolygon(xpts, ypts, 3);
            int[] ypts2 = {h/2, h/2 + toothH, h/2};
            img.fillPolygon(xpts, ypts2, 3);
        }

        // ── Zipper pull (Darker/Deep Purple) ──
        img.setColor(new Color(120, 0, 150)); 
        int pullW = GameConfig.s(6);
        int pullH = GameConfig.s(8);
        img.fillRect(w - pullW, h/2 - pullH/2, pullW, pullH);

        setImage(img);
    }
}
