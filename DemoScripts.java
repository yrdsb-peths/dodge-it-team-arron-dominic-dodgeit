/*
 * ─────────────────────────────────────────────────────────────────────────────
 * DemoScripts.java  —  THE AUTHORED SCENARIO SCRIPTS FOR EVERY CHARACTER
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A static factory class.  Its only job is to return a DemoScript for a
 *   given CharacterConfig.  AbilityDisplayState calls DemoScripts.getScript()
 *   once in enter() to know what scenarios to run.
 *
 * HOW TO WRITE A STAGE:
 *   new DemoStage("AbilityName (KEY)", "Initial hint text.", autoAdvanceFrames)
 *       .spawnRoadrollerAt(frame, laneIndex)   // frame: when; laneIndex: 0/1/2
 *       .spawnTrainAt(frame, laneIndex)
 *       .showHintAt(frame, "New hint text")
 *
 *   - 60 frames ≈ 1 second at normal speed.
 *   - laneIndex 0/1/2 maps to GameConfig.LANES[0/1/2].
 *   - autoAdvanceFrames: total frames before the stage ends.
 *     Give enough time for the player to try the ability AND see the result.
 *
 * HOW TO ADD A NEW CHARACTER:
 *   1. Add a new case to the switch block below.
 *   2. Write a DemoScript with one DemoStage per ability.
 *   3. If that character never needs a demo, return getDefaultScript(cfg).
 *
 * MANDOM NOTE:
 *   The Mandom stage deliberately sends obstacles early so the rewind bar fills
 *   up.  The player presses R once the bar turns purple.  The rewind system
 *   works normally here — AbilityDisplayState has its own Time_RewindManager.
 *
 * STICKY FINGERS NOTE:
 *   Because StickyFingers has two distinct mechanics (portal wrap + underground
 *   hide), its stage runs long and uses two showHintAt() calls to guide the
 *   player through both in sequence.
 *
 * Interacts with:
 *   AbilityDisplayState (calls getScript()), DemoScript, DemoStage
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class DemoScripts {

    // ─────────────────────────────────────────────────────────────────────────
    // ENTRY POINT
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the authored DemoScript for the given character.
     * Falls back to a generic one-stage script for unrecognised characters.
     *
     * @param cfg  The active character configuration.
     * @return     A fully-populated DemoScript ready to be run.
     */
    public static DemoScript getScript(CharacterConfig cfg) {
        switch (cfg) {
            case DIO:       return getDioScript();
            default:        return getDefaultScript(cfg);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DIO  (4 abilities)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Dio's full demo.  One stage per ability, in the order they appear in his
     * CharacterConfig abilityClassNames list.
     */
    private static DemoScript getDioScript() {
        return new DemoScript()

            // ── STAGE 1: The World Stand (E key) ─────────────────────────────
            // Roadrollers come in quickly.  The hint tells the player to press E.
            // The Stand appears, destroys incoming obstacles, and times out.
            // Stage is long enough for the Stand to fully expire so players see
            // both the active phase and the cooldown.
            .add(new DemoStage(
                    "The World Stand (E)",
                    "Roadrollers incoming!  Press E to summon your Stand!",
                    360)
                .spawnRoadrollerAt(50,  1)
                .spawnRoadrollerAt(70,  2)
                .spawnRoadrollerAt(90,  0)
                .spawnRoadrollerAt(120, 1)
                .spawnRoadrollerAt(140, 2)
                .spawnRoadrollerAt(160, 0)
                .spawnRoadrollerAt(220, 1)
                .spawnRoadrollerAt(250, 2)
                .showHintAt(180, "Your Stand destroys anything it touches! Watch the cooldown ring.")
            )

            // ── STAGE 2: Made in Heaven (S key) ──────────────────────────────
            // A wave of roadrollers comes in dense pairs — hard to dodge normally.
            // The hint tells the player to hold S.
            // During MiH the world slows while Dio moves normally → easy dodging.
            .add(new DemoStage(
                    "Made in Heaven (S)",
                    "Dense traffic ahead!  Hold S to enter Made in Heaven!",
                    420)
                .spawnRoadrollerAt(40,  0)
                .spawnRoadrollerAt(50,  2)
                .spawnRoadrollerAt(80,  1)
                .spawnRoadrollerAt(90,  0)
                .spawnRoadrollerAt(100, 2)
                .spawnRoadrollerAt(160, 1)
                .spawnRoadrollerAt(170, 0)
                .spawnRoadrollerAt(180, 2)
                .spawnRoadrollerAt(240, 0)
                .spawnRoadrollerAt(255, 1)
                .spawnRoadrollerAt(270, 2)
                .showHintAt(60,  "Hold S — notice how obstacles slow around you!")
                .showHintAt(300, "After S ends, wait for the cooldown (blue ring) to refill.")
            )

            // ── STAGE 3: Mandom — Rewind Time (R key) ────────────────────────
            // Obstacles arrive early so the TIME bar has time to fill (it needs
            // at least REWIND_COST_FRAMES of recorded history).
            // Multiple showHintAt() calls walk the player through the sequence.
            .add(new DemoStage(
                    "Mandom — Rewind Time (R)",
                    "Watch the TIME bar in the top-right fill up...",
                    480)
                .spawnRoadrollerAt(20,  2)
                .spawnRoadrollerAt(50,  1)
                .spawnRoadrollerAt(80,  0)
                .spawnTrainAt(100, 1)
                .spawnRoadrollerAt(160, 2)
                .spawnRoadrollerAt(190, 0)
                .spawnRoadrollerAt(220, 1)
                .spawnTrainAt(260, 0)
                .spawnRoadrollerAt(330, 2)
                .spawnRoadrollerAt(360, 1)
                .showHintAt(90,  "Once the TIME bar turns PURPLE, press R to rewind!")
                .showHintAt(240, "Press R now — everything rewinds 2 seconds into the past!")
                .showHintAt(360, "You gain brief invincibility when the rewind ends.")
            )

            // ── STAGE 4: Sticky Fingers (F key) ──────────────────────────────
            // Two mechanics in one stage:
            //   Part 1 (frames 1-200):  guide the player to the screen edges.
            //   Part 2 (frames 200+):   guide the player to press F to hide.
            // Obstacles come in throughout so there is genuine danger to respond to.
            .add(new DemoStage(
                    "Sticky Fingers (F)",
                    "Move to the top or bottom screen edge — you'll teleport!",
                    540)
                .spawnRoadrollerAt(60,  0)
                .spawnRoadrollerAt(90,  2)
                .spawnRoadrollerAt(130, 1)
                .spawnRoadrollerAt(170, 0)
                .spawnTrainAt(200, 2)
                .spawnRoadrollerAt(260, 1)
                .spawnRoadrollerAt(290, 0)
                .spawnRoadrollerAt(320, 2)
                .spawnTrainAt(360, 1)
                .spawnRoadrollerAt(420, 0)
                .spawnRoadrollerAt(450, 2)
                .showHintAt(130, "Teleport lets you dodge whole lanes!  Now try pressing F...")
                .showHintAt(200, "Press F to zip underground — you become INVISIBLE and INVINCIBLE!")
                .showHintAt(350, "Press F again to emerge (with brief i-frames), then dodge!")
            );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HEROMAYBE  (2 abilities: StandPunch + StickyFingers)
    // ─────────────────────────────────────────────────────────────────────────

    private static DemoScript getHeroMaybeScript() {
        return new DemoScript()

            // Stage 1: Stand Punch (E)
            .add(new DemoStage(
                    "The World Stand (E)",
                    "Press E to summon your Stand and destroy obstacles!",
                    300)
                .spawnRoadrollerAt(50,  1)
                .spawnRoadrollerAt(75,  0)
                .spawnRoadrollerAt(100, 2)
                .spawnRoadrollerAt(150, 1)
                .spawnRoadrollerAt(180, 0)
                .showHintAt(200, "The Stand punches for you — watch the orange ring drain.")
            )

            // Stage 2: Sticky Fingers (F)
            .add(new DemoStage(
                    "Sticky Fingers (F)",
                    "Move to screen edges to teleport!  Press F to hide underground!",
                    420)
                .spawnRoadrollerAt(50,  0)
                .spawnRoadrollerAt(80,  2)
                .spawnTrainAt(130, 1)
                .spawnRoadrollerAt(200, 0)
                .spawnRoadrollerAt(230, 2)
                .spawnRoadrollerAt(280, 1)
                .spawnTrainAt(330, 0)
                .showHintAt(180, "Press F now — you become invisible and invincible underground!")
                .showHintAt(320, "Press F again to surface with brief invincibility frames.")
            );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FALLBACK  (any character without a custom script)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generic one-stage demo for characters without a custom script.
     * Just sends a few roadrollers and hints the player to use their abilities.
     */
    private static DemoScript getDefaultScript(CharacterConfig cfg) {
        return new DemoScript()
            .add(new DemoStage(
                    "Abilities",
                    "Try your abilities!  Use them to survive the incoming obstacles.",
                    360)
                .spawnRoadrollerAt(60,  1)
                .spawnRoadrollerAt(90,  0)
                .spawnRoadrollerAt(120, 2)
                .spawnRoadrollerAt(180, 1)
                .spawnTrainAt(220, 0)
                .spawnRoadrollerAt(280, 2)
                .showHintAt(150, "Check the ability icons (bottom-right) to see your cooldowns.")
            );
    }
}
