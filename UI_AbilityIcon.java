/*
 * ─────────────────────────────────────────────────────────────────────────────
 * UI_AbilityIcon.java  —  CIRCULAR COOLDOWN WHEEL FOR ONE ABILITY
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   Draws a circular, per-pixel cooldown indicator for one of the player's
 *   abilities.  Placed in the bottom-right corner by PlayingState.enter().
 *   One icon is created per "visible" ability (shouldShowIcon()==true).
 *
 * Per-pixel rendering:
 *   The icon uses a nested x/y loop over every pixel.  For each pixel, it
 *   computes the angle from the centre using Math.atan2(dx, -dy) — this gives
 *   0° at the top, increasing clockwise, which is the standard "clock sweep"
 *   direction.  Pixels whose angle is ≤ percent × 360° are painted.
 *
 * Two rings:
 *   OUTER ring (between 32% radius and 100% radius):
 *     - Orange while active (getActivePercent() drains from 1.0→0.0)
 *     - Blue while on cooldown (getCooldownPercent() fills from 0.0→1.0)
 *
 *   INNER ring (0% to 32% radius):
 *     - Light blue for the secondary cooldown (portal warp in Sticky Fingers)
 *     - Only visible if getSecondaryCooldownPercent() > 0
 *
 * Green border:
 *   When both cooldown and active are idle AND the player is alive, a green
 *   circle outline is drawn to signal "ability is ready".
 *
 * Self-removal:
 *   If the player's visible ability list no longer has an entry at slotIndex
 *   (e.g., the ability was removed or slotIndex is out of bounds), the icon
 *   removes itself from the world.
 *
 * Interacts with:
 *   GenericPlayer.getVisibleAbilities() (ability data source),
 *   Ability interface (all percentage and state methods),
 *   PlayingState (creates instances), GameConfig.s() (sizing)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;
import java.util.List;

public class UI_AbilityIcon extends Actor {

    private Player player;
    /** Which position in the player's visible-ability list this icon represents. */
    private Ability ability;
    /** Pixel size of the circular icon (width and height). */
    private int    size;

    /**
     * @param player     The player whose ability this icon tracks.
     * @param slotIndex  Index into player.getVisibleAbilities().
     */
    public UI_AbilityIcon(Player player, Ability ability) {
        this.player    = player;
        this.ability = ability;
        this.size      = GameConfig.s(50);
        updateImage();
    }

    @Override
    public void act() {
        updateImage();
    }

    /**
     * Redraws the icon based on the current ability state.
     * Called every frame.
     */
    private void updateImage() {
        if (ability == null) return;

        // ── Build the icon image ──────────────────────────────────────────────
        GreenfootImage img = new GreenfootImage(size, size);
        int cx = size / 2;
        int cy = size / 2;

        // Precompute radius boundaries (squared, to avoid sqrt in the loop)
        int outerRadiusSq = (size / 2)               * (size / 2);
        int innerRadiusSq = (int)(size * 0.32) * (int)(size * 0.32); // 32% = boundary between rings

        // ── 1. Dark background circle ─────────────────────────────────────────
        img.setColor(player.isDead()
            ? new Color(100, 0, 0, 150)   // red tint when dead
            : new Color(30, 30, 30, 200)); // dark grey normally
        img.fillOval(0, 0, size, size);

        // ── 2. Keybind letter in the centre ───────────────────────────────────
        img.setColor(Color.WHITE);
        img.setFont(new Font("Arial", true, false, GameConfig.s(18)));
        img.drawString(ability.getDisplayLabel(), cx - GameConfig.s(6), cy + GameConfig.s(7));

        // ── 3. Determine percentages and colours for the two rings ────────────
        double primaryPercent;
        Color  primaryColor;

        if (ability.isActive()) {
            primaryPercent = ability.getActivePercent();
            primaryColor   = new Color(255, 140, 0, 180); // orange = active/draining
        } else {
            primaryPercent = ability.getCooldownPercent();
            primaryColor   = new Color(0, 150, 255, 180); // blue = cooldown/filling
        }

        double secondaryPercent = ability.getSecondaryCooldownPercent();
        Color  secondaryColor   = new Color(150, 220, 255, 200); // lighter blue = portal

        // ── 4. Per-pixel ring rendering ───────────────────────────────────────
        // For each pixel inside the outer circle, compute its clockwise angle
        // from the top (12 o'clock = 0°) and paint it if within the fill arc.
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int dx     = x - cx;
                int dy     = y - cy;
                int distSq = dx * dx + dy * dy;

                if (distSq <= outerRadiusSq) {
                    // Angle in degrees, 0° at top, clockwise
                    double angle = Math.toDegrees(Math.atan2(dx, -dy));
                    if (angle < 0) angle += 360;

                    if (distSq > innerRadiusSq) {
                        // OUTER RING — primary ability / cooldown
                        if (angle <= primaryPercent * 360 && primaryPercent > 0) {
                            img.setColorAt(x, y, primaryColor);
                        }
                    } else {
                        // INNER RING — secondary cooldown (portal)
                        if (angle <= secondaryPercent * 360 && secondaryPercent > 0) {
                            img.setColorAt(x, y, secondaryColor);
                        }
                    }
                }
            }
        }

        // ── 5. Green "ready" border ───────────────────────────────────────────
        // Only drawn when both primary and secondary are fully reset and player is alive.
        if (!ability.isCooldownActive() && !ability.isActive() && !player.isDead()) {
            img.setColor(Color.GREEN);
            img.drawOval(1, 1, size - 3, size - 3);
        }

        setImage(img);
    }
}
