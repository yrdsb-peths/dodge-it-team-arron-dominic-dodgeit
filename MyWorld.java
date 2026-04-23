/*
 * ─────────────────────────────────────────────────────────────────────────────
 * MyWorld.java  —  THE ENTRY POINT OF THE ENTIRE GAME
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   This is the one-and-only World class that Greenfoot instantiates when
 *   the project starts.  Everything in the game lives inside this world.
 *
 * How it works:
 *   Greenfoot calls world.act() 60 times per second.  Instead of putting
 *   all game logic here, we delegate immediately to the GameStateManager (GSM).
 *   The GSM then delegates to whichever GameState is currently on top of
 *   its stack (Menu, Playing, Paused, etc.).
 *
 * Key responsibilities:
 *   1. Create the GameStateManager.
 *   2. Set the paint (rendering) order so actors are drawn in the right layers.
 *   3. Push the first state (MenuState) to kick off the game.
 *   4. Pre-load all audio into RAM so there is no lag on first play.
 *   5. Expose isRewinding() so any actor can ask "are we currently rewinding?"
 *
 * Interacts with:
 *   GameStateManager, MenuState, PlayingState, AudioManager, GameConfig
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class MyWorld extends World {

    /** The state machine that drives the entire game. All logic flows through it. */
    private GameStateManager gsm;

    /**
     * A stored reference to a PlayingState object.
     * NOTE: This field is currently unused in practice — the real PlayingState
     * is created inside gsm.pushState() when the game begins.  It is safe to
     * ignore this field for now.
     */
    public PlayingState playingState;

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────────────
    public MyWorld() {
        /*
         * super(width, height, cellSize, bounded)
         *   width / height : size of the world in pixels (scaled by GameConfig.s())
         *   cellSize = 1   : each grid cell is 1×1 pixel (so coordinates = pixels)
         *   bounded = false: actors are NOT clipped to the world boundary —
         *                    they can move off-screen, which we need for
         *                    obstacles entering from the right edge.
         */
        super(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, 1, false);

        // Create the game state manager, passing ourselves so states can
        // add/remove actors from this world.
        gsm = new GameStateManager(this);

        /*
         * PAINT ORDER — controls which actors are drawn on top of which.
         * Earlier in the list = drawn LAST = appears on top.
         * Later in the list  = drawn FIRST = appears at the bottom (background).
         *
         * Layer order (top → bottom):
         *   Banner          — boss intro banner (always on top of everything)
         *   UIText          — score label and text overlays
         *   UI_AbilityIcon  — ability cooldown wheels
         *   UI_RewindBar    — the TIME rewind meter
         *   FX_DimOverlay   - dim overlay in abiity display state
         *   UI_Panel        - UI during abiity display section
         *   FX_RewindOverlay— blue scanline effect during rewind
         *   Exclaimation    — the ! warning mark above a Train's lane
         *   PathWarning     — red lane highlight before a Train charges
         *   TheWorldStand   — Dio's punch stand (beside the player)
         *   FX_Portal       — zipper flash at screen edges
         *   FX_ZipperGround — zipper drawn on the road when hiding
         *   GenericPlayer   — the player character sprite
         *   FX_Afterimage   — motion-blur ghost trail (behind player)
         *   Obstacles       — road rollers and trains
         *   ScrollingRoad   — the moving road background (bottommost)
         */
        setPaintOrder(
            Banner.class, UIText.class, UI_HighlightBox.class, UI_AbilityIcon.class, UI_RewindBar.class,
            FX_ErasureSnap.class, FX_ShatteredGlass.class,    
            TheWorldStand.class,GenericPlayer.class,FX_KingCrimsonOverlay.class,          
            UI_Panel.class,FX_RewindOverlay.class,
            Exclaimation.class, PathWarning.class,
            FX_Portal.class, FX_ZipperGround.class,
            FX_Afterimage.class, Obstacles.class, 
            ScrollingRoad.class,
            FX_DimOverlay.class
        );

        // Pre-create a PlayingState (stored but not used yet — see field note above).
        playingState = new PlayingState();

        // Start the game in the main menu.
        // pushState adds the state on top of the GSM stack and calls its enter() method.
        gsm.pushState(new MenuState());

        // Pre-load every sound file into RAM now, so the first play of any
        // sound does not cause a noticeable lag spike.
        AudioManager.init();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MAIN GAME LOOP
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called by Greenfoot ~60 times per second.
     * We do NOT put any game logic here directly.
     * Instead, we delegate entirely to the GameStateManager, which forwards
     * the call to whichever GameState is currently active (Menu, Playing, etc.).
     */
    @Override
    public void act() {
        gsm.update();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GETTERS / HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the GameStateManager so that any actor or state can interact
     * with the state machine (e.g., to check isState() or changeState()).
     */
    public GameStateManager getGSM() {
        return gsm;
    }

    /**
     * Convenience method: returns true if the game is currently performing
     * a time-rewind.  Actors use this to freeze their own movement while
     * the rewind system restores past positions.
     *
     * How it works: peeks the current state; if it is a PlayingState,
     * asks it whether its rewind manager is active.
     */
    public boolean isRewinding() {
        GameState s = gsm.peekState();
        if (s instanceof IActiveGameState) {
            return ((IActiveGameState) s).isRewinding();
        }
        return false;
    }
    
    /**
     * Returns the playable height of the world.
     * During normal play, this is the full world height.
     * During the ability demo, it restricts actors to the top sandbox area.
     */
    public int getBottomBound() {
        if (gsm.isState(AbilityDisplayState.class)) {
            return GameConfig.DEMO_BOTTOM_BOUND;
        }
        return getHeight();
    }
}
