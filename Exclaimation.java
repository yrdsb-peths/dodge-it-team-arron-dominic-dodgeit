/*
 * ─────────────────────────────────────────────────────────────────────────────
 * Exclaimation.java  —  THE "!" WARNING MARK ABOVE A TRAIN'S LANE
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A short-lived animated "!" actor that appears at the right edge of the
 *   screen above the lane a Train is about to enter.
 *   Plays a sprite animation for 1 second, then removes itself.
 *   Spawned by SpawnManager.spawnTrain() alongside PathWarning and Train.
 *
 * Animation:
 *   Uses the Animator system with frames from images/symbols/exclaimation/.
 *   Scale is 0.1 × GameConfig.SCALE — small enough to be a subtle indicator.
 *
 * Time Machine:
 *   Saves the remaining lifeTimer frames so rewinding correctly restores
 *   the ! mark's lifetime (prevents it from instantly disappearing or resetting).
 *
 * Interacts with:
 *   SpawnManager (creates this), Animator (animation frames),
 *   GameTimer (1-second life), Time_RewindManager (snapshot)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class Exclaimation extends Actor implements Time_Snapshottable {

    /** Auto-removes after 1 second. */
    private GameTimer lifeTimer = new GameTimer(1.0, false);
    /** Plays the animated ! sprite. */
    private Animator exclaimAnim;

    public Exclaimation() {
        exclaimAnim = new Animator("symbols", "exclaimation", 0.1 * GameConfig.SCALE);
        lifeTimer.start();
    }

    @Override
    public void act() {
        MyWorld world = (MyWorld) getWorld();
        if (world == null || !world.getGSM().isState(IActiveGameState.class)) return;
        IActiveGameState activeState = (IActiveGameState) world.getGSM().peekState();
        if (activeState.isGameFrozen()) return;
        
        setImage(exclaimAnim.getCurrentFrame());
        lifeTimer.update((MyWorld) getWorld());
        if (lifeTimer.isExpired()) {
            getWorld().removeObject(this);
        }
    }

    @Override
    public Time_ActorMemento captureState() {
        // Saves remaining timer frames — restoring this prevents reset-on-rewind
        return new Time_ActorMemento(this, getX(), getY(), lifeTimer.getRemainingFrames());
    }

    @Override
    public void restoreState(Time_ActorMemento m) {
        lifeTimer.setRemainingFrames((int) m.customData);
    }
}
