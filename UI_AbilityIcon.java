import greenfoot.*;

public class UI_AbilityIcon extends Actor {    
    private Player player;
    private int slotIndex;
    private int size;
    
    public UI_AbilityIcon(Player player, int slotIndex) {
        this.player = player;
        this.slotIndex = slotIndex;
        this.size = GameConfig.s(50); // Slightly smaller to fit rows
        updateImage();
    }
    
    public void act() {
        updateImage();
    }
    
    private void updateImage() {
        Ability ability = null;
        if (player instanceof GenericPlayer) {
            GenericPlayer gp = (GenericPlayer)player;
            // If the player doesn't have an ability for this slot, remove the icon
            if (slotIndex >= gp.getAbilityCount()) {
                if (getWorld() != null) getWorld().removeObject(this);
                return;
            }
            ability = gp.getAbilityAt(slotIndex);
        }

        if (ability == null) return;

        GreenfootImage img = new GreenfootImage(size, size);
        int cx = size / 2;
        int cy = size / 2;
        int radiusSq = (size/2) * (size/2);
        
        // 1. Background Circle
        img.setColor(new Color(30, 30, 30, 200));
        if (player.isDead()) img.setColor(new Color(100, 0, 0, 150));
        img.fillOval(0, 0, size, size);
        
        // 2. Ability Letter Label
        img.setColor(Color.WHITE);
        img.setFont(new Font("Arial", true, false, GameConfig.s(18)));
        // Center the text roughly
        img.drawString(ability.getDisplayLabel(), cx - GameConfig.s(6), cy + GameConfig.s(7));

        // 3. Cooldown/Active Sweeps
        double percent = 0;
        Color sweepColor = null;

        if (ability.isActive()) {
            percent = ability.getActivePercent();
            sweepColor = new Color(255, 140, 0, 160); // Orange
        } 
        else if (ability.isCooldownActive()) {
            percent = ability.getCooldownPercent();
            sweepColor = new Color(0, 150, 255, 160); // Blue
        }

        if (sweepColor != null) {
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    int dx = x - cx;
                    int dy = y - cy;
                    if (dx * dx + dy * dy <= radiusSq) {
                        double angle = Math.toDegrees(Math.atan2(dx, -dy));
                        if (angle < 0) angle += 360;
                        if (angle <= percent * 360) {
                            img.setColorAt(x, y, sweepColor);
                        }
                    }
                }
            }
        } else if (!player.isDead()) {
            // Ready Border
            img.setColor(Color.GREEN);
            img.drawOval(1, 1, size - 3, size - 3);
        }
        
        setImage(img);
    }
}