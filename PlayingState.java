/*
 * ─────────────────────────────────────────────────────────────────────────────
 * PlayingState.java  —  THE MAIN GAME LOOP STATE
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   The state that runs during actual gameplay.  Owns the SpawnManager and
 *   the Time_RewindManager.  Its update() is the heartbeat of the game:
 *   it either records the world state (normal play) or replays past states
 *   (during rewind), then updates spawning and the score display.
 *
 * enter() sets up a FRESH world every time it is entered:
 *   - Removes ALL existing actors (so nothing bleeds over from previous runs).
 *   - Seeds the RNG, resets the score, starts music.
 *   - Adds two ScrollingRoad tiles side-by-side for infinite scroll.
 *   - Creates the SpawnManager and rewind manager.
 *   - Spawns the player (Dio or GenericPlayer based on ACTIVE_CHARACTER).
 *   - Adds ability icons and UI elements.
 *
 * update() runs every frame:
 *   1. Reads the key pressed THIS frame (stored once because Greenfoot.getKey()
 *      clears the key buffer on first read — reading it twice gives null).
 *   2. If W pressed → push PausedState (time-stop).
 *   3. DVD-player metaphor: if rewinding → step backward; else → record forward.
 *   4. Update the SpawnManager (may spawn new obstacles).
 *   5. Update the score display label.
 *
 * triggerRewind():
 *   Called by Ability_Mandom.activate().
 *   Mutes audio, plays the rewind sound, starts the rewind, and adds the
 *   FX_RewindOverlay for the blue scanline visual effect.
 *
 * Interacts with:
 *   MyWorld, GameStateManager, SpawnManager, Time_RewindManager,
 *   GenericPlayer, Dio, Ability (icons), AudioManager, ScoreManager,
 *   ScrollingRoad, UIText, UI_RewindBar, FX_RewindOverlay, GameRNG
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;
import java.util.List;
import java.util.ArrayList;

public class PlayingState implements GameState,IActiveGameState{

    private SpawnManager spawnManager;
    private UIText scoreDisplay;
    private Time_RewindManager rewindManager;
    private UI_RewindBar rewindBar;

    /** The blue scanline overlay shown while rewinding. Null when not rewinding. */
    private FX_RewindOverlay rewindOverlay;

    // ─────────────────────────────────────────────────────────────────────────
    // ENTER
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sets up the entire game world for a new play session.
     * Called once when this state becomes active (either first run or restart).
     */
    @Override
    public void enter(MyWorld world) {
        world.removeObjects(world.getObjects(null)); // clear ALL existing actors

        GameRNG.randomize();   // generate a new random seed for this session
        ScoreManager.reset();  // reset score to 0 (copies to highScore if higher)

        // Start the character's background music
        AudioManager.playLoop(GameConfig.ACTIVE_CHARACTER.bgmKey);

        // Pre-draw the rewind overlay images into static memory now, before
        // the player first presses R, to avoid a lag spike on first use.
        FX_RewindOverlay.preLoad();

        Greenfoot.setSpeed(50); // ensure normal tick speed (MiH may have changed it)

        rewindManager = new Time_RewindManager();

        // Two road tiles placed side-by-side — they scroll left and wrap around
        // to create the illusion of an infinite road.
        world.addObject(new ScrollingRoad(), world.getWidth() / 2,       world.getHeight() / 2);
        world.addObject(new ScrollingRoad(), world.getWidth() + world.getWidth() / 2, world.getHeight() / 2);

        spawnManager = new SpawnManager();

        // Score label in the top-left corner
        scoreDisplay = new UIText("SCORE: 0", GameConfig.s(25), Color.WHITE);
        world.addObject(scoreDisplay, GameConfig.s(80), GameConfig.s(20));

        // ── Spawn the player ─────────────────────────────────────────────────
        // Dio gets his own subclass so onPauseUpdate() can spawn the boss banner.
        // All other characters use GenericPlayer directly.
        GenericPlayer player;
        if (GameConfig.ACTIVE_CHARACTER == CharacterConfig.DIO) {
            player = new Dio();
        } else {
            player = new GenericPlayer(GameConfig.ACTIVE_CHARACTER);
        }
        // Rewind bar in the top-right corner
        if (player.hasAbility(Ability_Mandom.class)) {
            rewindBar = new UI_RewindBar(rewindManager);
            world.addObject(rewindBar, world.getWidth() - GameConfig.s(100), GameConfig.s(20));
        }
        // ── Ability icons (bottom-right corner, one per visible ability) ──────
        List<Ability> visibleAbilities = player.getVisibleAbilities();
        int iconCount   = visibleAbilities.size();
        int iconSpacing = GameConfig.s(55);
        int startX      = world.getWidth()  - GameConfig.s(45);
        int startY      = world.getHeight() - GameConfig.s(45);

        for (int i = 0; i < iconCount; i++) {
            world.addObject(new UI_AbilityIcon(player, visibleAbilities.get(i)), startX - (i * iconSpacing), startY);
        }

        world.addObject(player, GameConfig.s(80), GameConfig.s(80));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * The main game loop — called every frame while this state is active.
     *
     * IMPORTANT: Greenfoot.getKey() clears the key buffer on first call.
     * Store it in a local variable before comparing it multiple times.
     */
    @Override
    public void update(MyWorld world) {
        // Store the pressed key once — a second call would return null.
        String key = Greenfoot.getKey();

        // ── Time-stop: W pushes PausedState on top of this state ─────────────
        if (GameConfig.TIME_STOP_BUTTON.equals(key)) {
            world.getGSM().pushState(new PausedState());
        }

        /*
         * DVD-PLAYER METAPHOR:
         *   If Rewind Mode is on  → play the game BACKWARD (restore past states).
         *   If Rewind Mode is off → act like Record mode (save current state).
         *
         * Every frame is either a rewind step OR a record — never both.
         */
        if (rewindManager.isRewinding()) {
            // Step backward through history; returns false when rewind finishes.
            boolean stillGoing = rewindManager.rewindStep(world, spawnManager);

            if (!stillGoing && rewindOverlay != null) {
                // Rewind just finished — clean up the visual overlay
                if (rewindOverlay.getWorld() != null) world.removeObject(rewindOverlay);
                rewindOverlay = null;
                Greenfoot.setSpeed(50);               // restore normal tick speed
                AudioManager.setAllSoundsPaused(false); // resume background music

                // Grant brief invincibility so the player doesn't die immediately
                List<GenericPlayer> players = world.getObjects(GenericPlayer.class);
                if (!players.isEmpty()) {
                    players.get(0).startIFrame(1.0);
                }
            }
        } else {
            // Normal gameplay: record the current world state into the history deque.
            rewindManager.record(world, spawnManager);
        }

        // ── Normal speed restore key (debug / accessibility) ─────────────────
        if (Greenfoot.isKeyDown("n")) {
            Greenfoot.setSpeed(50);
        }

        // ── Update spawning and score display ─────────────────────────────────
        spawnManager.update(world);
        scoreDisplay.setText("SCORE: " + ScoreManager.getScore());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EXIT
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Cleans up when PlayingState ends.
     * Note: PlayingState is normally never "exited" — other states are pushed
     * ON TOP of it (Pause, then popped).  exit() is only called when the player
     * truly loses (GameOverState.update → changeState).
     */
    @Override
    public void exit(MyWorld world) {
        AudioManager.stop(GameConfig.ACTIVE_CHARACTER.bgmKey);
        world.removeObjects(world.getObjects(null));
        Greenfoot.setSpeed(50);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REWIND TRIGGER  (called by Ability_Mandom.activate())
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Starts a rewind if the manager has enough history.
     * Also mutes audio, plays the rewind SFX, and adds the blue overlay.
     * Called by Ability_Mandom, not triggered directly from update().
     *
     * @param world  The game world.
     */
    public void triggerRewind(MyWorld world) {
        if (rewindManager.canRewind()) {
            AudioManager.setAllSoundsPaused(true); // mute everything
            AudioManager.playPool("rewind");        // play rewind whoosh
            rewindManager.startRewind();

            rewindOverlay = new FX_RewindOverlay();
            world.addObject(rewindOverlay, world.getWidth() / 2, world.getHeight() / 2);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────────────────────────────────────

    public SpawnManager getSpawnManager() { return spawnManager; }

    /** @return True while the rewind manager is actively rewinding. */
    public boolean isRewinding() {
        return rewindManager != null && rewindManager.isRewinding();
    }
    
    @Override
    public boolean isGameFrozen() {
        return false; 
    }
}
