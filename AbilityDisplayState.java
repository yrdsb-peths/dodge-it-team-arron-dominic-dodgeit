/*
 * ─────────────────────────────────────────────────────────────────────────────
 * AbilityDisplayState.java  —  GUIDED ABILITY DEMONSTRATION SCREEN
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A GameState that sits between CharacterSelectState and PlayingState.
 *   It sets up an isolated live environment for each of the active character's
 *   abilities so the player can see and try them before the real game starts.
 *
 * Flow:
 *   CharacterSelectState (ENTER) → AbilityDisplayState → PlayingState
 *   Each DemoStage runs in turn; when all stages are done, PlayingState starts.
 *   Pressing ENTER at any time skips immediately to PlayingState.
 *
 * Per-stage lifecycle (startStage):
 *   1. Clear actors from the previous stage.
 *   2. Reset rewind manager + demo spawn manager.
 *   3. Respawn the player, ability icons, and rewind bar (if applicable).
 *   4. Display the stage label and initial hint.
 *
 * How Mandom works here:
 *   A full Time_RewindManager is created for each stage — the rewind system
 *   runs exactly as in PlayingState.  `demoSpawnManager` is a real SpawnManager
 *   but its update() is never called, so it never randomly spawns obstacles.
 *   It exists only to satisfy Time_FrameSnapshot's constructor.
 *
 * How player death is handled:
 *   GenericPlayer detects the AbilityDisplayState and calls notifyPlayerDied()
 *   instead of pushing GameOverState.  On the next update() frame the stage
 *   resets cleanly.
 *
 * Implements IActiveGameState so all actors' state guards (Obstacles, Road, etc.)
 * accept this state the same way they accept PlayingState.
 *
 * Interacts with:
 *   DemoScript / DemoStage / DemoScripts (scenario data),
 *   GenericPlayer / Dio (player actor),
 *   Time_RewindManager (full rewind support for Mandom),
 *   SpawnManager (dummy instance held for the rewind snapshot),
 *   IActiveGameState (interface implemented here),
 *   CharacterSelectState (transitions here),
 *   PlayingState (this transitions there when done)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

public class AbilityDisplayState implements GameState, IActiveGameState {

    // ─────────────────────────────────────────────────────────────────────────
    // STATE FIELDS
    // ─────────────────────────────────────────────────────────────────────────

    /** The script for the active character — fetched once in enter(). */
    private DemoScript script;

    /** Which stage we are currently showing (index into script). */
    private int currentStageIndex = 0;

    /** Frame counter that resets to 0 at the start of each stage. */
    private int demoFrame = 0;

    /**
     * Set to true by notifyPlayerDied().
     * Checked in update() — if true, the stage is restarted on the next frame.
     */
    private boolean playerDiedThisFrame = false;

    // ─────────────────────────────────────────────────────────────────────────
    // SYSTEMS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Handles the Mandom rewind mechanic, exactly as in PlayingState.
     * Recreated at the start of each stage so history is fresh.
     */
    private Time_RewindManager rewindManager;

    /**
     * Held purely to satisfy Time_FrameSnapshot's constructor when recording.
     * Its update() is NEVER called — all obstacle spawning is done by DemoStage.tick().
     * Recreated at the start of each stage so its state can be rewound correctly.
     */
    private SpawnManager demoSpawnManager;

    /** The blue scanline overlay shown during a rewind.  Null when not rewinding. */
    private FX_RewindOverlay rewindOverlay;

    // ─────────────────────────────────────────────────────────────────────────
    // UI  (persistent across stages)
    // ─────────────────────────────────────────────────────────────────────────

    /** Scrolling hint text displayed at the top of the screen. */
    private UIText hintLabel;

    /** "Ability N / M: <name>" — shows which stage we are on. */
    private UIText stageLabel;

    /** Always visible at the bottom: how to skip to the game. */
    private UIText skipLabel;

    // ─────────────────────────────────────────────────────────────────────────
    // ACTOR TRACKING  (for per-stage cleanup)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Tracks actors that belong to the CURRENT stage only (player, icons, bars).
     * They are removed and re-created when the stage changes.
     * The road tiles, hint label, stage label, and skip label are NOT in this list —
     * they persist for the entire AbilityDisplayState session.
     */
    private List<Actor> stageActors = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────────────────
    // ENTER / EXIT
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void enter(MyWorld world) {
        // Clear everything (remnants of CharacterSelectState)
        world.removeObjects(world.getObjects(null));

        CharacterConfig cfg = GameConfig.ACTIVE_CHARACTER;
        script = DemoScripts.getScript(cfg);

        // Start the character's BGM (same key as PlayingState uses)
        AudioManager.playLoop(cfg.bgmKey);

        // Pre-load rewind overlay images to avoid lag on first rewind
        FX_RewindOverlay.preLoad();

        // Ensure normal tick speed (MiH might have left it changed)
        Greenfoot.setSpeed(50);

        // ── Add the infinite-scroll road (two tiles, same as PlayingState) ────
        world.addObject(new ScrollingRoad(), world.getWidth() / 2,                    world.getHeight() / 2);
        world.addObject(new ScrollingRoad(), world.getWidth() + world.getWidth() / 2, world.getHeight() / 2);

        // ── Persistent UI (never removed until exit()) ─────────────────────────
        hintLabel  = new UIText("", GameConfig.s(22), Color.WHITE);
        stageLabel = new UIText("", GameConfig.s(18), new Color(255, 200, 50));
        skipLabel  = new UIText("[ ENTER : Skip to Game ]", GameConfig.s(18), Color.CYAN);

        world.addObject(hintLabel,  world.getWidth() / 2, GameConfig.s(28));
        world.addObject(stageLabel, world.getWidth() / 2, GameConfig.s(52));
        world.addObject(skipLabel,  world.getWidth() / 2, world.getHeight() - GameConfig.s(20));

        // ── Begin the first stage ──────────────────────────────────────────────
        currentStageIndex = 0;
        startStage(world);
    }

    @Override
    public void exit(MyWorld world) {
        AudioManager.stop(GameConfig.ACTIVE_CHARACTER.bgmKey);
        world.removeObjects(world.getObjects(null));
        Greenfoot.setSpeed(50);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PER-STAGE SETUP
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * (Re)initialises everything that is stage-specific:
     *   - Removes actors from the previous stage (player, icons, bars, obstacles).
     *   - Resets the rewind manager and the demo spawn manager.
     *   - Spawns a fresh player with their full ability set.
     *   - Adds ability icons and the rewind bar if the character has Mandom.
     *   - Updates the stage indicator and hint text.
     *
     * Called from enter() and from update() whenever a stage ends.
     */
    private void startStage(MyWorld world) {
        // ── 1. Remove actors from the previous stage ──────────────────────────
        clearStageActors(world);

        // ── 2. Reset systems ──────────────────────────────────────────────────
        playerDiedThisFrame = false;
        demoFrame           = 0;
        demoSpawnManager    = new SpawnManager();   // never updated; just holds state for rewind
        rewindManager       = new Time_RewindManager();

        // ── 3. Update the stage indicator label ───────────────────────────────
        DemoStage stage    = script.getStage(currentStageIndex);
        int total          = script.getStageCount();
        if (total > 1) {
            stageLabel.setText("Ability " + (currentStageIndex + 1) + " / " + total
                + " :  " + stage.getAbilityName());
        } else {
            stageLabel.setText("Ability Demo :  " + stage.getAbilityName());
        }

        // ── 4. Update the hint text ────────────────────────────────────────────
        hintLabel.setText(stage.getInitialHint());

        // ── 5. Spawn the player ────────────────────────────────────────────────
        GenericPlayer player;
        if (GameConfig.ACTIVE_CHARACTER == CharacterConfig.DIO) {
            player = new Dio();
        } else {
            player = new GenericPlayer(GameConfig.ACTIVE_CHARACTER);
        }
        // Place the player on the left side, vertically centred
        world.addObject(player, GameConfig.s(80), world.getHeight() / 2);
        stageActors.add(player);

        // ── 6. Ability icons (bottom-right corner) ────────────────────────────
        List<Ability> visibleAbilities = player.getVisibleAbilities();
        int iconCount   = visibleAbilities.size();
        int iconSpacing = GameConfig.s(55);
        int startX      = world.getWidth()  - GameConfig.s(45);
        int startY      = world.getHeight() - GameConfig.s(45);
        for (int i = 0; i < iconCount; i++) {
            UI_AbilityIcon icon = new UI_AbilityIcon(player, i);
            world.addObject(icon, startX - (i * iconSpacing), startY);
            stageActors.add(icon);
        }

        // ── 7. Rewind bar (top-right corner) — only if character has Mandom ───
        if (player.hasAbility(Ability_Mandom.class)) {
            UI_RewindBar bar = new UI_RewindBar(rewindManager);
            world.addObject(bar, world.getWidth() - GameConfig.s(100), GameConfig.s(20));
            stageActors.add(bar);
        }
    }

    /**
     * Removes all actors that belong to the current stage, plus any obstacle /
     * effect actors that DemoStage.tick() may have spawned.
     * Leaves the road tiles and persistent UI (hint, stage, skip labels) intact.
     */
    private void clearStageActors(MyWorld world) {
        // Remove tracked stage actors (player, icons, bars)
        for (Actor a : stageActors) {
            if (a.getWorld() != null) world.removeObject(a);
        }
        stageActors.clear();

        // Remove any obstacles / effects left over from the scenario
        world.removeObjects(world.getObjects(Obstacles.class));
        world.removeObjects(world.getObjects(PathWarning.class));
        world.removeObjects(world.getObjects(Exclaimation.class));
        world.removeObjects(world.getObjects(FX_Portal.class));
        world.removeObjects(world.getObjects(FX_ZipperGround.class));
        world.removeObjects(world.getObjects(FX_Afterimage.class));
        world.removeObjects(world.getObjects(TheWorldStand.class));

        // Clean up any rewind overlay
        if (rewindOverlay != null && rewindOverlay.getWorld() != null) {
            world.removeObject(rewindOverlay);
        }
        rewindOverlay = null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MAIN UPDATE LOOP
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void update(MyWorld world) {
        String key = Greenfoot.getKey();

        // ── Skip to PlayingState immediately ──────────────────────────────────
        if ("enter".equals(key)) {
            world.getGSM().changeState(new PlayingState());
            return;
        }

        // ── Rewind handling (DVD metaphor, identical to PlayingState) ──────────
        if (rewindManager.isRewinding()) {
            boolean stillGoing = rewindManager.rewindStep(world, demoSpawnManager);

            if (!stillGoing) {
                // Rewind just finished
                if (rewindOverlay != null && rewindOverlay.getWorld() != null) {
                    world.removeObject(rewindOverlay);
                }
                rewindOverlay = null;
                Greenfoot.setSpeed(50);
                AudioManager.setAllSoundsPaused(false);

                // Grant brief i-frames (same as PlayingState)
                List<GenericPlayer> players = world.getObjects(GenericPlayer.class);
                if (!players.isEmpty()) {
                    players.get(0).startIFrame(1.0);
                }
            }
            // Do NOT record during rewind — same as PlayingState
        } else {
            // Record the current frame into the rewind history
            rewindManager.record(world, demoSpawnManager);
        }

        // ── Handle player death (notified by GenericPlayer) ───────────────────
        if (playerDiedThisFrame) {
            playerDiedThisFrame = false;
            // Remove obstacles so the restart is clean
            world.removeObjects(world.getObjects(Obstacles.class));
            world.removeObjects(world.getObjects(PathWarning.class));
            world.removeObjects(world.getObjects(Exclaimation.class));
            startStage(world);
            return;
        }

        // ── Advance the demo frame and fire this frame's events ───────────────
        demoFrame++;
        DemoStage stage = script.getStage(currentStageIndex);
        stage.tick(demoFrame, world, hintLabel);

        // ── Check if the stage should advance ────────────────────────────────
        if (stage.shouldAdvance(demoFrame, world)) {
            currentStageIndex++;
            if (currentStageIndex >= script.getStageCount()) {
                // All stages are done — start the actual game
                world.getGSM().changeState(new PlayingState());
            } else {
                // Move on to the next ability demonstration
                startStage(world);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IACTIVEGAMESTATE  (rewind interface, also used by actors' state guards)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called by MyWorld.isRewinding() so actors know not to move during a rewind.
     * Mirrors the identical method in PlayingState.
     */
    @Override
    public boolean isRewinding() {
        return rewindManager != null && rewindManager.isRewinding();
    }

    /**
     * Called by Ability_Mandom.activate() when the player presses R.
     * Starts a rewind if enough history has been recorded.
     * Mirrors the identical method in PlayingState.
     */
    @Override
    public void triggerRewind(MyWorld world) {
        if (rewindManager.canRewind()) {
            AudioManager.setAllSoundsPaused(true);
            AudioManager.playPool("rewind");
            rewindManager.startRewind();

            rewindOverlay = new FX_RewindOverlay();
            world.addObject(rewindOverlay, world.getWidth() / 2, world.getHeight() / 2);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CALLED BY GenericPlayer
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called by GenericPlayer.movementLogic() when the death timer expires,
     * instead of pushing GameOverState as it normally would.
     * Sets a flag so the stage resets on the next update() frame.
     * Also updates the hint text so the player sees feedback.
     */
    public void notifyPlayerDied() {
        playerDiedThisFrame = true;
        if (hintLabel != null) {
            hintLabel.setText("You died!  Stage restarting...");
        }
    }
}
