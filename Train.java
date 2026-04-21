/*
 * ─────────────────────────────────────────────────────────────────────────────
 * Train.java  —  THE HIGH-SPEED LANE-CHARGING OBSTACLE  (called "Ambulance")
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A more dangerous obstacle that telegraphs its arrival before charging.
 *   Uses an ambulance sprite and awards 5 score points when successfully dodged.
 *   Named "Train" because it charges down a fixed lane like a train on tracks.
 *
 * Two-state internal machine:
 *   STATE 0 — Waiting (65 frames ≈ 1 second):
 *     Sits at the right edge while PathWarning and Exclaimation are on-screen.
 *     The 65-frame wait is slightly longer than PathWarning's 60-frame life,
 *     so the warning disappears just as the Train starts moving.
 *     Collision is DISABLED in this state (train hasn't moved yet).
 *
 *   STATE 1 — Rushing:
 *     Moves left at full speed (default s(25) pixels/frame).
 *     Collision becomes active.
 *
 * Telegraph system (coordinated by SpawnManager):
 *   SpawnManager.spawnTrain() also adds:
 *     - Exclaimation (! mark) — appears above the lane
 *     - PathWarning  (red zone) — highlights the danger lane
 *   These give the player ~1 second of visual warning before the Train charges.
 *
 * Time Machine:
 *   Saves speed, state, and waitTimer so rewinding correctly returns the Train
 *   to its waiting/charging phase.
 *
 * Interacts with:
 *   Obstacles (parent), SpawnManager (creates it alongside PathWarning/Exclaimation),
 *   Player (collision), Time_RewindManager (snapshot)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class Train extends Obstacles implements Time_Snapshottable {

    /**
     * STATE 0: Waiting — the Train is visible but hasn't moved yet.
     * STATE 1: Rushing — the Train charges left at full speed.
     */
    private int state = 0;

    /**
     * Counts down from 65 while in STATE 0.
     * 65 frames ≈ 1.08 seconds, slightly longer than PathWarning's 60-frame life.
     */
    private int waitTimer = 65;

    /** Speed in pixels per frame once charging (STATE 1). Set by SpawnManager. */
    private int speed;

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a Train with the given charge speed.
     * Loads and scales the ambulance sprite.
     *
     * @param speed  Pixels per frame when in STATE 1 (rushing).
     */
    public Train(int speed) {
        GreenfootImage img = new GreenfootImage("obstacles/ambulence.png");
        setImage(img);
        getImage().scale(GameConfig.TRAIN_WIDTH, GameConfig.TRAIN_WIDTH);
        this.speed = speed;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OBSTACLE LOGIC
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * STATE 0: Count down the wait timer, then transition to STATE 1.
     * STATE 1: Move left at full speed.
     */
    @Override
    protected void movementLogic() {
        if (state == 0) {
            waitTimer--;
            // Stationary while waiting — setLocation keeps position explicit
            setLocation(getX(), getY());
            if (waitTimer <= 0) {
                state = 1; // transition to rushing
            }
        } else {
            // STATE 1: Charge across the screen
            move(-speed);
        }
    }

    /**
     * Collision is only checked in STATE 1 (rushing).
     * Uses a tight hitbox (padding=1.0) since the Train moves very fast.
     * Plays the crash sound on impact.
     */
    @Override
    protected void collisionLogic() {
        if (state == 0) return; // can't hit player while waiting

        Player p = (Player) getOneIntersectingObject(Player.class);
        if (p != null && !p.isDead()) {
            if (p.checkCustomHitbox(this, 1)) { // full-size hitbox (no forgiveness)
                AudioManager.play("car_crash");
                p.die();
            }
        }
    }

    /**
     * Removes the Train if it has fully crossed the left edge.
     * Awards 5 score points if the player is alive and not hiding.
     * Uses -100 (not 0) as the threshold because the Train sprite is wide
     * and we want it fully off-screen before removing.
     */
    @Override
    protected void checkRemove() {
        if (getX() < -100) {
            java.util.List<Player> players = getWorld().getObjects(Player.class);
            if (!players.isEmpty()) {
                Player player = players.get(0);
                boolean hiding = (player instanceof GenericPlayer)
                    && ((GenericPlayer) player).isHidden();
                if (!player.isDead() && !hiding) {
                    ScoreManager.addScore(5); // trains are worth more than cars
                }
            }
            getWorld().removeObject(this); // MUST be last
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TIME MACHINE
    // ─────────────────────────────────────────────────────────────────────────

    /** Extra data saved per Train: we need all three to correctly replay the waiting phase. */
    private static class TrainData {
        int speed, state, waitTimer;
        TrainData(int speed, int state, int waitTimer) {
            this.speed     = speed;
            this.state     = state;
            this.waitTimer = waitTimer;
        }
    }

    /** Captures speed, state (waiting/charging), and waitTimer frames. */
    @Override
    public Time_ActorMemento captureState() {
        return new Time_ActorMemento(this, getX(), getY(),
            new TrainData(speed, state, waitTimer));
    }

    /** Restores all three fields from the past snapshot. */
    @Override
    public void restoreState(Time_ActorMemento m) {
        TrainData d     = (TrainData) m.customData;
        this.speed      = d.speed;
        this.state      = d.state;
        this.waitTimer  = d.waitTimer;
    }
}
