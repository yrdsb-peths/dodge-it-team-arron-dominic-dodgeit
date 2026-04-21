/*
 * ─────────────────────────────────────────────────────────────────────────────
 * Roadroller.java  —  THE STANDARD OBSTACLE (MOVING CAR)
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   The most common obstacle.  Spawned by SpawnManager at a random lane.
 *   Moves left at a fixed speed.  If it passes the player (reaches X=0),
 *   it awards score and removes itself.  If it hits the player, the player dies.
 *
 * Multiple constructors allow flexibility:
 *   Roadroller()          — default speed, 1 score point (used internally)
 *   Roadroller(speed)     — custom speed, 1 score point
 *   Roadroller(speed, sc) — custom speed, custom score (for scoreless obstacles)
 *
 * Collision:
 *   Uses GenericPlayer.checkCustomHitbox() with 0.8 padding for a slightly
 *   forgiving circular hitbox.  Skips collision entirely while rewinding.
 *
 * Score:
 *   Added when the roadroller exits the left edge — but only if the player is
 *   alive AND not currently hidden underground (Sticky Fingers).
 *
 * Time Machine:
 *   Saves speed and scoreToAdd.  Position is handled by the base memento.
 *
 * Interacts with:
 *   Obstacles (parent), Player / GenericPlayer (collision + score check),
 *   SpawnManager (creates instances), Time_RewindManager (snapshot)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class Roadroller extends Obstacles implements Time_Snapshottable {

    /** How many score points this roadroller awards when it successfully passes the player. */
    private int scoreToAdd = 1;

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCTORS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Default constructor: loads the sprite, mirrors it, scales it, sets default speed.
     * Called indirectly by the other constructors via this().
     */
    public Roadroller() {
        GreenfootImage img = new GreenfootImage("obstacles/road_roller.png");
        setImage(img);
        getImage().mirrorHorizontally(); // face left (moving left)
        getImage().scale(GameConfig.ROADROLLER_WIDTH, GameConfig.ROADROLLER_WIDTH);
        speed = GameConfig.ROADROLLER_SPEED;
    }

    /**
     * Creates a Roadroller with a custom speed and 1 score point.
     * Used by SpawnManager for difficulty-scaled spawning.
     *
     * @param speed  Pixels per frame (set by SpawnManager based on difficulty).
     */
    public Roadroller(int speed) {
        this(speed, 1);
    }

    /**
     * Creates a Roadroller with a custom speed and custom score award.
     * Allows creating scoreless obstacles (scoreToAdd = 0) for special scenarios.
     *
     * @param speed       Pixels per frame.
     * @param scoreToAdd  Points awarded when successfully dodged.
     */
    public Roadroller(int speed, int scoreToAdd) {
        this(); // call default constructor to set up the sprite
        this.speed      = speed;
        this.scoreToAdd = scoreToAdd;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OBSTACLE LOGIC
    // ─────────────────────────────────────────────────────────────────────────

    /** Moves left by 'speed' pixels per frame. */
    @Override
    protected void movementLogic() {
        move(-speed);
    }

    /**
     * Checks for collision with the player.
     * Two safety guards:
     *   1. null world check (defensive programming against late-frame removal)
     *   2. Skip entirely while rewinding (obstacles are being teleported by the
     *      rewind system and should not kill the player mid-rewind)
     */
    @Override
    protected void collisionLogic() {
        MyWorld world = (MyWorld) getWorld();
        if (world == null || world.isRewinding()) return;

        // getOneIntersectingObject is faster than isTouching — returns the first hit.
        Player player = (Player) getOneIntersectingObject(Player.class);
        if (player != null && !player.isDead()) {
            // 0.8 padding = slightly forgiving hitbox (80% of the player's radius)
            if (player.checkCustomHitbox(this, 0.8)) {
                player.die();
            }
        }
    }

    /**
     * Checks if the roadroller has exited the left edge of the screen.
     * If so: award score (unless player is dead or hidden), then remove self.
     *
     * IMPORTANT: removeObject(this) is called LAST.
     * After removal, getWorld() returns null — calling anything after that crashes.
     */
    @Override
    protected void checkRemove() {
        if (getX() <= 0) {
            // Award score only if the player is alive and above ground
            java.util.List<Player> players = getWorld().getObjects(Player.class);
            if (!players.isEmpty()) {
                Player player = players.get(0);
                boolean hiding = (player instanceof GenericPlayer)
                    && ((GenericPlayer) player).isHidden();
                if (!player.isDead() && !hiding) {
                    ScoreManager.addScore(scoreToAdd);
                }
            }
            getWorld().removeObject(this); // MUST be the absolute last line
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TIME MACHINE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Extra data saved per roadroller for the rewind system.
     * Position is stored by the base Time_ActorMemento; this holds the rest.
     */
    private static class RoadrollerData {
        int speed, scoreToAdd;
        RoadrollerData(int speed, int scoreToAdd) {
            this.speed      = speed;
            this.scoreToAdd = scoreToAdd;
        }
    }

    /** Captures the current speed and scoreToAdd alongside the actor's position. */
    @Override
    public Time_ActorMemento captureState() {
        return new Time_ActorMemento(this, getX(), getY(),
            new RoadrollerData(speed, scoreToAdd));
    }

    /** Restores speed and scoreToAdd from the saved snapshot. */
    @Override
    public void restoreState(Time_ActorMemento m) {
        RoadrollerData d = (RoadrollerData) m.customData;
        this.speed      = d.speed;
        this.scoreToAdd = d.scoreToAdd;
    }
}
