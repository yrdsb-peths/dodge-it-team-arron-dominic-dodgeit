/*
 * ─────────────────────────────────────────────────────────────────────────────
 * GameStateManager.java  —  THE STATE MACHINE THAT DRIVES THE GAME
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   Manages a stack of GameState objects and determines which state is
 *   currently running.  All game logic flows through here.
 *
 * Why a Stack?
 *   A Stack follows Last-In-First-Out (LIFO) order — like a stack of plates.
 *   This is perfect for nested states:
 *     - PlayingState is running (bottom of stack).
 *     - Player presses W → PausedState is PUSHED on top.
 *     - Only PausedState updates; PlayingState freezes underneath.
 *     - Player presses W again → PausedState is POPPED; PlayingState resumes.
 *   Without a stack, implementing "pause over play" would require messy booleans.
 *
 * pushState  vs  changeState:
 *   pushState  — keeps the state below it alive (use for overlays like Pause).
 *   changeState— replaces the current state entirely (use for Menu→Game→GameOver).
 *
 * Interacts with:
 *   MyWorld (owns this), every GameState implementation
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;
import java.util.Stack;

public class GameStateManager {

    /** The stack of active states.  Only the top state is updated each frame. */
    private Stack<GameState> stateStack;

    /** Reference to the world, passed to every state's enter/update/exit calls. */
    private MyWorld world;

    /**
     * Creates a new GameStateManager for the given world.
     * @param world  The MyWorld instance this manager belongs to.
     */
    public GameStateManager(MyWorld world) {
        this.stateStack = new Stack<>();
        this.world = world;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STACK OPERATIONS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Pushes a new state onto the top of the stack and enters it.
     * The previous state stays in the stack — it just stops updating.
     * Use this for overlaying states (e.g., Pause over Playing).
     *
     * @param state  The new state to activate.
     */
    public void pushState(GameState state) {
        stateStack.push(state);
        state.enter(world);
    }

    /**
     * Removes the top state from the stack and exits it.
     * The state below it becomes active again automatically.
     * Use this to "dismiss" an overlay (e.g., un-pause).
     */
    public void popState() {
        if (!stateStack.isEmpty()) {
            GameState popped = stateStack.pop();
            popped.exit(world);
        }
    }

    /**
     * Replaces the current top state with a new one.
     * Exits the old state before entering the new one.
     * Use this for clean transitions (Menu → CharacterSelect → Playing → GameOver).
     *
     * @param state  The new state to switch to.
     */
    public void changeState(GameState state) {
        if (!stateStack.isEmpty()) {
            stateStack.pop().exit(world);
        }
        stateStack.push(state);
        state.enter(world);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PER-FRAME UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called once per frame by MyWorld.act().
     * Only the state on TOP of the stack gets updated — all others are frozen.
     */
    public void update() {
        if (!stateStack.isEmpty()) {
            stateStack.peek().update(world);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // QUERY HELPERS  (used by actors to check "what state are we in?")
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns true if the top state is an instance of the given class.
     * This is the standard guard used at the top of every actor's act():
     *   if (!world.getGSM().isState(PlayingState.class)) return;
     *
     * @param stateClass  The class to check against (e.g., PlayingState.class).
     * @return            True if the top state is of that type.
     */
    public boolean isState(Class<?> stateClass) {
        if (stateStack.isEmpty()) return false;
        return stateClass.isInstance(stateStack.peek());
    }

    /**
     * Returns the current top state without removing it.
     * Returns null if the stack is empty.
     * Useful when you need to call a method specific to a known state type.
     */
    public GameState peekState() {
        return stateStack.isEmpty() ? null : stateStack.peek();
    }
}
