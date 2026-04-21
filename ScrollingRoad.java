/*
 * ─────────────────────────────────────────────────────────────────────────────
 * ScrollingRoad.java  —  THE INFINITELY SCROLLING BACKGROUND ROAD
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   Two ScrollingRoad instances are placed side-by-side in PlayingState.enter().
 *   They scroll left together.  When one tile reaches the left edge, it
 *   teleports to the far right, creating seamless infinite scrolling.
 *
 * Tiling setup:
 *   Tile 1 starts at world.getWidth()/2         (centre of the screen).
 *   Tile 2 starts at world.getWidth() * 1.5     (one screen-width to the right).
 *   Both are exactly world.getWidth() wide, so they seamlessly cover the screen.
 *
 * Infinite scroll math:
 *   When getX() <= -(width/2) → teleport to getX() + (width * 2).
 *   The tile jumps one full width ahead of the other tile, maintaining coverage.
 *
 * Rewind behaviour:
 *   act() checks BOTH isState(PlayingState) AND !world.isRewinding().
 *   During rewind, the road freezes in place while actor positions are restored
 *   to past states.  This is intentional.
 *
 * Speed:
 *   A static field (shared across all instances) so both tiles always move together.
 *   increaseSpeed() is called by SpawnManager.increaseDifficulty() to match
 *   the growing obstacle speed.
 *
 * Time Machine:
 *   Only position is captured (null customData).  The static speed is not rewound,
 *   but this is acceptable since the road is a pure visual effect.
 *
 * Interacts with:
 *   PlayingState (adds two instances), SpawnManager (calls increaseSpeed()),
 *   Time_RewindManager (snapshot for position)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class ScrollingRoad extends Actor implements Time_Snapshottable {

    /** Current scroll speed (pixels/frame). Static so both tiles stay in sync. */
    private static int speed    = GameConfig.ROAD_SCROLL_SPEED;
    /** Speed cap — road can never scroll faster than this. */
    private static int maxSpeed = GameConfig.ROAD_MAX_SPEED;
    /** Width of one road tile (equals the world width for seamless tiling). */
    private int width = GameConfig.WORLD_WIDTH;

    public ScrollingRoad() {
        speed = GameConfig.ROAD_SCROLL_SPEED; // reset static field on construction
        drawPlaceholderRoad();
    }

    /**
     * Draws the road graphic procedurally — no external image file needed.
     * A dark grey rectangle with yellow dashed lane dividers.
     * Lane positions are taken from GameConfig.LANES.
     */
    private void drawPlaceholderRoad() {
        GreenfootImage img = new GreenfootImage(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);

        // Asphalt base
        img.setColor(new Color(50, 50, 50));
        img.fill();

        // Yellow dashed lane dividers
        img.setColor(Color.YELLOW);
        for (int i = 1; i < GameConfig.LANES.length; i++) {
            // Draw dividers midway between adjacent lane centres
            int lineY = (GameConfig.LANES[i] + GameConfig.LANES[i - 1]) / 2;
            for (int x = 0; x < width; x += GameConfig.s(40)) {
                img.fillRect(x, lineY - 2, GameConfig.s(20), GameConfig.s(4));
            }
        }
        setImage(img);
    }

    @Override
    public void act() {
        MyWorld world = (MyWorld) getWorld();
        // Only scroll during active gameplay — freeze during menus, pause, and rewind.
        IActiveGameState activeState = (IActiveGameState) world.getGSM().peekState();
        if (world.getGSM().isState(IActiveGameState.class) && !world.isRewinding() && !activeState.isGameFrozen()) {
            scroll();
        }
    }

    /**
     * Moves the tile left and wraps it to the right when it goes off-screen.
     * The wrap threshold of -(width/2) is when the tile's CENTRE reaches the left edge.
     */
    private void scroll() {
        setLocation(getX() - speed, getY());
        if (getX() <= -width / 2) {
            setLocation(getX() + width * 2, getY()); // teleport to far right
        }
    }

    /**
     * Increases the scroll speed by the given amount, up to maxSpeed.
     * Called by SpawnManager each difficulty increase so the road keeps pace.
     */
    public static void increaseSpeed(int amount) {
        if (speed < maxSpeed) speed += amount;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TIME MACHINE
    // ─────────────────────────────────────────────────────────────────────────

    /** Only position matters; customData is null. */
    @Override
    public Time_ActorMemento captureState() {
        return new Time_ActorMemento(this, getX(), getY(), null);
    }

    /** Position is restored by the rewind manager automatically. Nothing else to do. */
    @Override
    public void restoreState(Time_ActorMemento m) { }
}
