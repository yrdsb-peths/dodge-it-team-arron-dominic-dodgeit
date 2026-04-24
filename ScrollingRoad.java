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
 */import greenfoot.*;

public class ScrollingRoad extends Actor implements Time_Snapshottable {

    private static int speed = GameConfig.ROAD_SCROLL_SPEED;
    private static int maxSpeed = GameConfig.ROAD_MAX_SPEED;
    private int width = GameConfig.WORLD_WIDTH;

    /**
     * @param imagePath The filename of the image in the images/ folder.
     */
    public ScrollingRoad() {
        speed = GameConfig.ROAD_SCROLL_SPEED; 
        
        // Load the image and force it to the world size for seamless tiling
        GreenfootImage img = new GreenfootImage(GameConfig.getActiveRoad());
        img.scale(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        setImage(img);
    }

    @Override
    public void act() {
        MyWorld world = (MyWorld) getWorld();
        if (world == null) return;

        GameState state = world.getGSM().peekState();
        
        if (state instanceof IActiveGameState) {
            IActiveGameState activeState = (IActiveGameState) state;
            
            // Check if player is dead
            boolean playerDead = false;
            java.util.List<GenericPlayer> players = world.getObjects(GenericPlayer.class);
            if (!players.isEmpty() && players.get(0).isDead()) {
                playerDead = true;
            }

            // ONLY scroll if: 
            // 1. Not rewinding
            // 2. Not frozen
            // 3. Player is ALIVE <--- This is the fix
            if (!world.isRewinding() && !activeState.isGameFrozen() && !playerDead) {
                scroll();
            }
        }
    }

    private void scroll() {
        setLocation(getX() - speed, getY());
        // Standard infinite loop math
        if (getX() <= -width / 2) {
            setLocation(getX() + width * 2, getY());
        }
    }

    public static void increaseSpeed(int amount) {
        if (speed < maxSpeed) speed += amount;
    }

    @Override
    public Time_ActorMemento captureState() {
        return new Time_ActorMemento(this, getX(), getY(), null);
    }

    @Override
    public void restoreState(Time_ActorMemento m) { }
    
    /**
     * Forcefully scrolls the road during King Crimson's future vision,
     * bypassing the standard 'isGameFrozen' checks.
     */
    public void fastForward() {
        scroll();
    }

}