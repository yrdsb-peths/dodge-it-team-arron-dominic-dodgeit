/*
 * ─────────────────────────────────────────────────────────────────────────────
 * DemoScript.java  —  A COMPLETE ABILITY DEMONSTRATION FOR ONE CHARACTER
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A thin container that holds an ordered list of DemoStage objects, one
 *   per ability to demonstrate.  AbilityDisplayState runs through them in order.
 *
 * Usage:
 *   DemoScripts.java is the only place where DemoScript objects are constructed.
 *   You build a script by chaining .add() calls:
 *
 *       new DemoScript()
 *           .add(new DemoStage("StandPunch (E)", "Press E!", 300)
 *               .spawnRoadrollerAt(60, 1)
 *               .spawnRoadrollerAt(90, 2))
 *           .add(new DemoStage("Mandom (R)", "Press R to rewind!", 480)
 *               .spawnRoadrollerAt(30, 0)
 *               ...)
 *
 * Interacts with:
 *   AbilityDisplayState (iterates stages), DemoStage (the stages themselves),
 *   DemoScripts (where scripts are defined)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import java.util.ArrayList;
import java.util.List;

public class DemoScript {

    /** The ordered list of stages for this character's demonstration. */
    private List<DemoStage> stages = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────────────────
    // BUILDER  (fluent API — chain .add() calls)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Appends a stage to this script and returns `this` for chaining.
     *
     * @param stage  The demo stage to add.
     * @return       This script, so calls can be chained.
     */
    public DemoScript add(DemoStage stage) {
        stages.add(stage);
        return this;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ACCESSORS  (used by AbilityDisplayState)
    // ─────────────────────────────────────────────────────────────────────────

    /** @return The stage at the given index. */
    public DemoStage getStage(int index) {
        return stages.get(index);
    }

    /** @return Total number of stages in this script. */
    public int getStageCount() {
        return stages.size();
    }
}
