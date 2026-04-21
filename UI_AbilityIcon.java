import greenfoot.*;
import java.util.List;
import java.util.ArrayList;

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
            List<Ability> visible = gp.getVisibleAbilities();
            if (slotIndex >= visible.size()) {
                if (getWorld() != null) getWorld().removeObject(this);
                return;
            }
            ability = visible.get(slotIndex);
        }

        if (ability == null) return;

        GreenfootImage img = new GreenfootImage(size, size);
        int cx = size / 2;
        int cy = size / 2;
        
        // Define our ring boundaries
        int outerRadiusSq = (size / 2) * (size / 2);
        int innerRadiusSq = (int)((size * 0.32) * (size * 0.32)); // Boundary between rings

        // 1. Background Circle
        img.setColor(new Color(30, 30, 30, 200));
        if (player.isDead()) img.setColor(new Color(100, 0, 0, 150));
        img.fillOval(0, 0, size, size);
        
        // 2. Ability Letter Label
        img.setColor(Color.WHITE);
        img.setFont(new Font("Arial", true, false, GameConfig.s(18)));
        img.drawString(ability.getDisplayLabel(), cx - GameConfig.s(6), cy + GameConfig.s(7));

        // 3. Prepare Percentages
        double primaryPercent = 0;
        Color primaryColor = null;

        if (ability.isActive()) {
            primaryPercent = ability.getActivePercent();
            primaryColor = new Color(255, 140, 0, 180); // Orange for active
        } else {
            primaryPercent = ability.getCooldownPercent();
            primaryColor = new Color(0, 150, 255, 180); // Blue for cooldown
        }

        double secondaryPercent = ability.getSecondaryCooldownPercent();
        Color secondaryColor = new Color(150, 220, 255, 200); // Lighter blue for portal

        // 4. Per-Pixel Rendering for the Wheels
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int dx = x - cx;
                int dy = y - cy;
                int distSq = dx * dx + dy * dy;

                // Only draw if within the outer circle
                if (distSq <= outerRadiusSq) {
                    double angle = Math.toDegrees(Math.atan2(dx, -dy));
                    if (angle < 0) angle += 360;

                    // OUTER RING (Primary Ability / Hide Cooldown)
                    if (distSq > innerRadiusSq) {
                        if (angle <= primaryPercent * 360 && primaryPercent > 0) {
                            img.setColorAt(x, y, primaryColor);
                        }
                    }
                    // INNER RING (Secondary Ability / Portal Cooldown)
                    else {
                        if (angle <= secondaryPercent * 360 && secondaryPercent > 0) {
                            img.setColorAt(x, y, secondaryColor);
                        }
                    }
                }
            }
        }

        // 5. Ready Border (Only if both are ready)
        if (!ability.isCooldownActive() && !ability.isActive() && !player.isDead()) {
            img.setColor(Color.GREEN);
            img.drawOval(1, 1, size - 3, size - 3);
        }
        
        setImage(img);
    }
}