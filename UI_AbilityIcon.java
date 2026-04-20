import greenfoot.*;

public class UI_AbilityIcon extends Actor {
    private Dio dio;
    private int size;
    
    public UI_AbilityIcon(Dio dio) {
        this.dio = dio;
        this.size = GameConfig.s(55); // Scales with GameConfig
        updateImage();
    }
    
    public void act() {
        updateImage();
    }
    
    private void updateImage() {
        GreenfootImage img = new GreenfootImage(size, size);
        int cx = size / 2;
        int cy = size / 2;
        int radiusSq = cx * cy;
        
        // If Dio is dead, the UI grays out and turns red.
        if (dio.isDead()) {
            img.setColor(new Color(255, 0, 0, 150));
            img.fillOval(0, 0, size, size);
            setImage(img);
            return;
        }

        Ability_StandPunch ability = dio.getStandPunchAbility();

        // Draw Base Background
        img.setColor(new Color(30, 30, 30, 220));
        img.fillOval(0, 0, size, size);
        
        // Draw the Letter "E"
        img.setColor(Color.WHITE);
        img.setFont(new Font("Arial", true, false, GameConfig.s(22)));
        img.drawString("E", cx - GameConfig.s(7), cy + GameConfig.s(8));

        double percent = 0;
        Color sweepColor = null;

        // Check Timers
        if (ability.isActive()) {
            // Punching right now! (Orange sweep draining)
            percent = ability.getDurFrames() / (GameConfig.WORLD_PUNCH_DURATION * 60.0);
            sweepColor = new Color(255, 140, 0, 180); // Orange
        } 
        else if (ability.isCoolActive()) {
            // On Cooldown! (Blue sweep filling up)
            percent = ability.getCoolFrames() / (GameConfig.WORLD_PUNCH_COOLDOWN * 60.0);
            sweepColor = new Color(0, 150, 255, 180); // Blue
        }

        // Fills the circle slice (LoL Cooldown Style)
        if (sweepColor != null) {
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    int dx = x - cx;
                    int dy = y - cy;
                    if (dx * dx + dy * dy <= radiusSq) {
                        double angle = Math.toDegrees(Math.atan2(dx, -dy)); // 0 degrees is Top
                        if (angle < 0) angle += 360;
                        if (angle <= percent * 360) {
                            img.setColorAt(x, y, sweepColor);
                        }
                    }
                }
            }
        } else {
            // Ready to be used! Draw a green border.
            img.setColor(new Color(0, 255, 0));
            img.drawOval(0, 0, size - 1, size - 1);
            img.drawOval(1, 1, size - 3, size - 3);
        }
        
        setImage(img);
    }
}