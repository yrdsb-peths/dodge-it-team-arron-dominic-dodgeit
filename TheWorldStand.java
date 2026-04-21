/*
 * ─────────────────────────────────────────────────────────────────────────────
 * TheWorldStand.java  —  DIO'S STAND ACTOR (SPAWNED BY Ability_StandPunch)
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A visual and gameplay actor that appears beside Dio when E is pressed.
 *   Plays a punch animation, destroys obstacles it touches, and follows Dio.
 *   Self-destructs when Ability_StandPunch.isActive() returns false.
 *
 * Self-destruction logic (IMPORTANT ORDER):
 *   1. FIRST check if the ability is no longer active → remove self IMMEDIATELY.
 *      This must happen even while paused or rewinding so the Stand never
 *      lingers as a ghost actor.
 *   2. THEN do the game-state safety check for normal movement/animation.
 *
 * Why the ability check is first:
 *   If the player dies, Ability_StandPunch.cancel() is called → isActive()=false.
 *   If we checked isState(PlayingState) first and the game had just paused,
 *   the Stand would never see isActive()==false and would remain forever.
 *
 * Time Machine:
 *   Implements Time_Snapshottable but only saves position (null customData).
 *   The ability's own timer state is saved through GenericPlayer's captureState().
 *
 * Interacts with:
 *   Ability_StandPunch (reads isActive()), GenericPlayer (follows it),
 *   Obstacles (intersecting ones are removed), ScoreManager (adds 2 per kill),
 *   Animator (punch animation)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;
import java.util.List;

public class TheWorldStand extends Actor implements Time_Snapshottable {

    /** Plays the WorldPunch animation frames beside Dio. */
    private Animator punchAnim;

    /** Reference to the ability that spawned us — checked every frame. */
    private Ability_StandPunch ability;

    /**
     * Creates the Stand, loads its animation, and plays the summon sound.
     *
     * @param ability  The Ability_StandPunch instance that created this Stand.
     *                 We hold a reference to check isActive() each frame.
     */
    public TheWorldStand(Ability_StandPunch ability) {
        punchAnim    = new Animator("Dio", "WorldPunch", ability.standAnimSpeed, GameConfig.DIO_BASE_SCALE);
        this.ability = ability;
        AudioManager.play("summon_stand");
    }

    @Override
    public void act() {
        MyWorld world = (MyWorld) getWorld();
        if (world == null) return;

        // ── STEP 1: Self-destruct check — MUST be first ───────────────────────
        // If the ability ended (time expired, player died, rewind reset it),
        // remove this actor immediately regardless of game state.
        if (!ability.isActive()) {
            world.removeObject(this);
            return;
        }

        // ── STEP 2: Standard game-state guard ────────────────────────────────
        // Only animate and fight obstacles while in PlayingState and not rewinding.
       if (!world.getGSM().isState(PlayingState.class) || world.isRewinding()) return;

        // Advance the punch animation
        setImage(punchAnim.getCurrentFrame());

        // ── Follow Dio ────────────────────────────────────────────────────────
        List<GenericPlayer> players = world.getObjects(GenericPlayer.class);
        if (!players.isEmpty()) {
            GenericPlayer player = players.get(0);
            if (player.isDead()) {
                world.removeObject(this); // don't follow a dead Dio
                return;
            }
            // Offset 80 design-pixels to the right of Dio
            setLocation(player.getX() + GameConfig.s(80), player.getY());
        }

        // ── Destroy obstacles on contact ─────────────────────────────────────
        List<Obstacles> hitObstacles = getIntersectingObjects(Obstacles.class);
        for (Obstacles obs : hitObstacles) {
            world.removeObject(obs);
            ScoreManager.addScore(2); // bonus points for Stand kills
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TIME MACHINE — only position needed; ability state is in GenericPlayer
    // ─────────────────────────────────────────────────────────────────────────

    /** The Stand only needs its position saved — the ability timer is stored elsewhere. */
    @Override
    public Time_ActorMemento captureState() {
        return new Time_ActorMemento(this, getX(), getY(), null);
    }

    /** Nothing to restore beyond position (handled by the rewind manager). */
    @Override
    public void restoreState(Time_ActorMemento m) { }
}
