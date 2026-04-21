/*
 * ─────────────────────────────────────────────────────────────────────────────
 * DemoStage.java  —  ONE STAGE OF AN ABILITY DEMONSTRATION
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   Defines the scenario for demonstrating a single ability.
 *   Holds a schedule of timed events: obstacle spawns and hint-text changes.
 *   AbilityDisplayState calls tick() every frame to fire scheduled events.
 *
 * How to write a script (fluent builder pattern):
 *
 *   new DemoStage("StandPunch (E)", "Press E to summon your Stand!", 360)
 *       .spawnRoadrollerAt(60, 1)          // frame 60: roadroller in lane 1
 *       .spawnRoadrollerAt(90, 0)          // frame 90: lane 0
 *       .spawnTrainAt(150, 2)              // frame 150: train in lane 2
 *       .showHintAt(200, "Nice! Your Stand destroys obstacles for you!")
 *
 *   The third constructor argument (360) is the auto-advance frame — the stage
 *   automatically ends after 360 frames whether or not the player did anything.
 *
 * Lane indices:
 *   Use 0, 1, 2 … to refer to lanes. The stage maps them to GameConfig.LANES
 *   Y-coordinates at spawn time (with wrap-around if the index is too large).
 *
 * Speed overrides:
 *   Use the 4-argument spawn variants to set a custom speed.
 *   Otherwise GameConfig.ROADROLLER_SPEED / TRAIN_SPEED is used.
 *
 * Interacts with:
 *   AbilityDisplayState (calls tick() and shouldAdvance() each frame),
 *   DemoScript (collects these into a list),
 *   DemoScripts (where stages are authored)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

public class DemoStage {

    // ─────────────────────────────────────────────────────────────────────────
    // INNER DATA CLASSES  (hold scheduled events compactly)
    // ─────────────────────────────────────────────────────────────────────────

    /** A single spawn event: one obstacle at a specific frame. */
    private static class SpawnEntry {
        int frame;
        boolean isTrain;   // true = Train (ambulance), false = Roadroller
        int laneIndex;     // index into GameConfig.LANES
        int speed;

        SpawnEntry(int frame, boolean isTrain, int laneIndex, int speed) {
            this.frame     = frame;
            this.isTrain   = isTrain;
            this.laneIndex = laneIndex;
            this.speed     = speed;
        }
    }

    /** A hint-text change event: update the on-screen hint at a specific frame. */
    private static class HintEntry {
        int frame;
        String text;

        HintEntry(int frame, String text) {
            this.frame = frame;
            this.text  = text;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIELDS
    // ─────────────────────────────────────────────────────────────────────────

    /** Short name for the ability, shown in the stage indicator label. */
    private String abilityName;

    /** Hint text shown the moment this stage starts. */
    private String initialHint;

    /**
     * The stage automatically advances to the next one once this many frames
     * have elapsed.  Set this generously enough for the player to try the ability
     * at least once.  -1 means never auto-advance (the stage runs forever, which
     * you would only use if there is a different completion check).
     */
    private int autoAdvanceFrames;

    /** All scheduled obstacle spawn events for this stage. */
    private List<SpawnEntry> spawnSchedule = new ArrayList<>();

    /** All scheduled hint-text changes for this stage. */
    private List<HintEntry> hintSchedule   = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new demo stage.
     *
     * @param abilityName        Short label shown in the "Ability N/M: ..." header.
     * @param initialHint        Hint text displayed when the stage starts.
     * @param autoAdvanceFrames  How many frames before moving to the next stage.
     *                           Use a value that gives the player time to explore.
     */
    public DemoStage(String abilityName, String initialHint, int autoAdvanceFrames) {
        this.abilityName       = abilityName;
        this.initialHint       = initialHint;
        this.autoAdvanceFrames = autoAdvanceFrames;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FLUENT BUILDER METHODS  (chain these to populate the stage)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Schedules a Roadroller spawn at the given frame using the default speed.
     *
     * @param frame      The demo frame at which to spawn (60 frames ≈ 1 second).
     * @param laneIndex  Index into GameConfig.LANES (0 = first lane, 1 = second, etc.).
     * @return           This stage, for chaining.
     */
    public DemoStage spawnRoadrollerAt(int frame, int laneIndex) {
        spawnSchedule.add(new SpawnEntry(frame, false, laneIndex, GameConfig.ROADROLLER_SPEED));
        return this;
    }

    /**
     * Schedules a Roadroller spawn with a custom speed.
     *
     * @param frame      The demo frame at which to spawn.
     * @param laneIndex  Index into GameConfig.LANES.
     * @param speed      Custom speed in pixels per frame.
     * @return           This stage, for chaining.
     */
    public DemoStage spawnRoadrollerAt(int frame, int laneIndex, int speed) {
        spawnSchedule.add(new SpawnEntry(frame, false, laneIndex, speed));
        return this;
    }

    /**
     * Schedules a Train spawn (complete with PathWarning and Exclaimation) at
     * the given frame using the default train speed.
     *
     * @param frame      The demo frame at which to spawn.
     * @param laneIndex  Index into GameConfig.LANES.
     * @return           This stage, for chaining.
     */
    public DemoStage spawnTrainAt(int frame, int laneIndex) {
        spawnSchedule.add(new SpawnEntry(frame, true, laneIndex, GameConfig.TRAIN_SPEED));
        return this;
    }

    /**
     * Schedules a Train spawn with a custom speed.
     *
     * @param frame      The demo frame at which to spawn.
     * @param laneIndex  Index into GameConfig.LANES.
     * @param speed      Custom charge speed in pixels per frame.
     * @return           This stage, for chaining.
     */
    public DemoStage spawnTrainAt(int frame, int laneIndex, int speed) {
        spawnSchedule.add(new SpawnEntry(frame, true, laneIndex, speed));
        return this;
    }

    /**
     * Schedules a hint-text update at the given frame.
     * Replaces whatever hint is currently showing.
     *
     * @param frame  The demo frame at which to change the hint.
     * @param text   The new hint text to display.
     * @return       This stage, for chaining.
     */
    public DemoStage showHintAt(int frame, String text) {
        hintSchedule.add(new HintEntry(frame, text));
        return this;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RUNTIME METHODS  (called each frame by AbilityDisplayState)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fires all events whose trigger frame matches the current demo frame.
     * Called once per frame by AbilityDisplayState.update().
     *
     * @param frame      The current frame counter for this stage (starts at 1).
     * @param world      The game world to add obstacles into.
     * @param hintLabel  The UIText actor to update with new hint text.
     */
    public void tick(int frame, MyWorld world, UIText hintLabel) {
        // ── Fire scheduled obstacle spawns ────────────────────────────────────
        for (SpawnEntry s : spawnSchedule) {
            if (s.frame == frame) {
                // Clamp the lane index safely
                int laneCount = GameConfig.LANES.length;
                int laneY     = GameConfig.LANES[s.laneIndex % laneCount];

                if (s.isTrain) {
                    // Train comes with a PathWarning + Exclaimation (same as SpawnManager)
                    int pathH        = GameConfig.LANE_HEIGHT;
                    int exclaimOffset = GameConfig.s(20);
                    int trainOffset   = GameConfig.s(50);

                    world.addObject(
                        new Exclaimation(),
                        world.getWidth() - exclaimOffset, laneY);
                    world.addObject(
                        new PathWarning(world.getWidth(), pathH),
                        world.getWidth() / 2, laneY);
                    world.addObject(
                        new Train(s.speed),
                        world.getWidth() + trainOffset, laneY);
                } else {
                    // Plain Roadroller
                    world.addObject(
                        new Roadroller(s.speed),
                        world.getWidth(), laneY);
                }
            }
        }

        // ── Fire scheduled hint updates ────────────────────────────────────────
        if (hintLabel != null) {
            for (HintEntry h : hintSchedule) {
                if (h.frame == frame) {
                    hintLabel.setText(h.text);
                }
            }
        }
    }

    /**
     * Returns true when this stage should hand off to the next one.
     * Currently based purely on elapsed frames (autoAdvanceFrames).
     * Override this in a subclass or extend with a custom check if you need
     * ability-specific completion conditions.
     *
     * @param frame  Current frame count for this stage.
     * @param world  The game world (available for custom completion checks).
     * @return       True if the stage is complete.
     */
    public boolean shouldAdvance(int frame, MyWorld world) {
        return autoAdvanceFrames > 0 && frame >= autoAdvanceFrames;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GETTERS  (read by AbilityDisplayState for UI)
    // ─────────────────────────────────────────────────────────────────────────

    /** @return The short ability name shown in the stage indicator header. */
    public String getAbilityName() { return abilityName; }

    /** @return The hint text to display when this stage first appears. */
    public String getInitialHint() { return initialHint; }
}
