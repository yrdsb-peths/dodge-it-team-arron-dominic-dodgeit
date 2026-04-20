import greenfoot.*;

public class UI_RewindBar extends Actor {
    private Time_RewindManager rewindManager;
    private static final int BAR_W = 150;
    private static final int BAR_H = 12;

    public UI_RewindBar(Time_RewindManager rewindManager) {
        this.rewindManager = rewindManager;
        redraw();
    }

    public void act() {
        redraw();
    }

    private void redraw() {
        int w = GameConfig.s(BAR_W);
        int h = GameConfig.s(BAR_H);
        int padding = GameConfig.s(2);
        
        GreenfootImage img = new GreenfootImage(w + padding*2, h + GameConfig.s(18));
        
        // Label
        GreenfootImage label = new GreenfootImage("TIME", GameConfig.s(10), Color.WHITE, new Color(0,0,0,0));
        img.drawImage(label, 0, 0);
        
        int barTop = GameConfig.s(14);
        
        // Background track
        img.setColor(new Color(0, 0, 0, 160));
        img.fillRect(0, barTop, w + padding*2, h + padding*2);
        
        // Fill amount
        double fill = (double) rewindManager.getHistorySize() / Time_RewindManager.MAX_HISTORY;
        boolean canRewind = rewindManager.canRewind();
        boolean isRewinding = rewindManager.isRewinding();
        
        Color fillColor;
        if (isRewinding) {
            fillColor = new Color(100, 200, 255); // bright blue while rewinding
        } else if (canRewind) {
            fillColor = new Color(160, 100, 255); // purple when ready
        } else {
            fillColor = new Color(80, 80, 80);    // gray when not enough
        }
        
        img.setColor(fillColor);
        img.fillRect(padding, barTop + padding, (int)(fill * w), h);
        
        // Cost marker — shows where "5 seconds" is
        double costFrac = (double) Time_RewindManager.REWIND_COST_FRAMES / Time_RewindManager.MAX_HISTORY;
        int markerX = (int)(costFrac * w) + padding;
        img.setColor(new Color(255, 255, 255, 180));
        img.fillRect(markerX, barTop, GameConfig.s(2), h + padding*2);
        
        setImage(img);
    }
}