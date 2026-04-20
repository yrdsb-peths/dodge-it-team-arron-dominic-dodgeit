import greenfoot.*;

public class UI_AbilityIcon extends Actor {    
    private Player player;
    private int size;
    
    public UI_AbilityIcon(Player player) {
        this.player = player;
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
        if (player.isDead()) {
            img.setColor(new Color(255, 0, 0, 150));
            img.fillOval(0, 0, size, size);
            setImage(img);
            return;
        }

        // Draw Base Background
        img.setColor(new Color(30, 30, 30, 220));
        img.fillOval(0, 0, size, size);
        
        // Draw the Letter "E"
        img.setColor(Color.WHITE);
        img.setFont(new Font("Arial", true, false, GameConfig.s(22)));
        img.drawString("E", cx - GameConfig.s(7), cy + GameConfig.s(8));

        double percent = 0;
        Color sweepColor = null;
        
        Ability ability = null;
        if (player instanceof GenericPlayer) {
            ability = ((GenericPlayer)player).getPrimaryAbility();
        }

        if (ability == null) return;


        // Check Timers
        if (ability.isActive()) {
            percent = ability.getActivePercent();
            sweepColor = new Color(255, 140, 0, 180);
        } 
        else if (ability.isCooldownActive()) {
            percent = ability.getCooldownPercent();
            sweepColor = new Color(0, 150, 255, 180);
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