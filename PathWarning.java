/*
 * ─────────────────────────────────────────────────────────────────────────────
 * PathWarning.java  —  RED LANE HIGHLIGHT BEFORE A TRAIN CHARGES
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A semi-transparent red rectangle that spans the full screen width and
 *   highlights the lane a Train is about to charge through.
 *   Flickers for 60 frames (1 second) then removes itself.
 *   Spawned by SpawnManager.spawnTrain() alongside the Exclaimation mark.
 *
 * Telegraph timing:
 *   PathWarning lasts 60 frames.  The Train waits 65 frames before charging.
 *   So the warning disappears 5 frames before the Train moves, creating a
 *   brief "oh no" moment right before the charge.
 *
 * Time Machine:
 *   Saves the remaining timer so rewinding correctly returns the warning to
 *   its past flicker state.
 *
 * Interacts with:
 *   SpawnManager (creates this alongside Train), Train (arrives 5 frames later),
 *   Time_RewindManager (snapshot)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class PathWarning extends Actor implements Time_Snapshottable {

    /** Remaining frames before this warning auto-removes itself.  60 frames = 1 second. */
    private int timer = 60;
    private Color warningColor; 
    /**
     * Creates a semi-transparent red rectangle of the given size.
     * Width should match the world width; height should match the lane height.
     *
     * @param width   Width of the warning zone in pixels.
     * @param height  Height of the warning zone in pixels.
     */
    public PathWarning(int width, int height) {
        this.warningColor = GameConfig.getPathWarningColor(GameConfig.ACTIVE_CHARACTER.roadImage);
        GreenfootImage img = new GreenfootImage(width, height);
        img.setColor(warningColor);
        img.fill();
        setImage(img);
    }

    @Override
    public void act() {
        MyWorld world = (MyWorld) getWorld();
        if (world == null) return;
        
        GameState state = world.getGSM().peekState();
        if (!(state instanceof IActiveGameState)) return; // Stop if paused
        
        IActiveGameState activeState = (IActiveGameState) state;
        if (activeState.isGameFrozen()) return; // Stop if demo is waiting

        timer--;
        // Flicker: alternate between dim (40) and bright (140) every 5 frames
        getImage().setTransparency(timer % 10 < 5 ? 40 : 140);

        if (timer <= 0) {
            world.removeObject(this);
        }
    }

    @Override
    public Time_ActorMemento captureState() {
        return new Time_ActorMemento(this, getX(), getY(), timer);
    }

    @Override
    public void restoreState(Time_ActorMemento m) {
        this.timer = (int) m.customData;
    }
}
