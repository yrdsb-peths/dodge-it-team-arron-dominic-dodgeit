/*
 * ─────────────────────────────────────────────────────────────────────────────
 * GenericPlayer.java  —  THE COMPLETE PLAYABLE CHARACTER IMPLEMENTATION
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   The concrete implementation of Player that handles everything:
 *   animation, movement, abilities, death, i-frames, and time-machine
 *   snapshotting.  All character data (speed, sprite folder, abilities)
 *   comes from a CharacterConfig, making this class fully data-driven.
 *
 * Design pattern: Data-Driven / Component
 *   GenericPlayer does not hard-code "Dio is this fast with these abilities".
 *   It reads all of that from CharacterConfig and dynamically loads abilities
 *   using Java Reflection (Class.forName()).  Adding a new character requires
 *   only a new CharacterConfig entry — no changes to this class.
 *
 * Ability loading via Reflection:
 *   For each String in config.abilityClassNames, we call:
 *     Class.forName("Ability_StandPunch").getDeclaredConstructor().newInstance()
 *   This creates an object of that class using only its name as a String.
 *   If the class name is wrong, it silently prints an error (no crash).
 *
 * Time Machine:
 *   Implements Time_Snapshottable.  Captures/restores: dead state, current
 *   animation, timer frames, and the state of every ability.
 *
 * Interacts with:
 *   Player (parent), CharacterConfig, Ability (list of), GameTimer,
 *   Animator (HashMap of), Time_RewindManager, GameOverState
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;
import java.util.*;

public class GenericPlayer extends Player implements Time_Snapshottable {

    // ─────────────────────────────────────────────────────────────────────────
    // FIELDS
    // ─────────────────────────────────────────────────────────────────────────

    /** The configuration object that defines this character's data. */
    private CharacterConfig config;

    /**
     * All animations for this character, keyed by animation name (e.g., "Dash").
     * Populated in the constructor from config.animNames.
     */
    private HashMap<String, Animator> animations = new HashMap<>();

    /** The Animator that is currently playing. */
    private Animator currentAnimator;

    /** The name of the currently playing animation (e.g., "Dash"). Used to avoid restarting. */
    private String currentAnimName = "";

    /** All ability instances for this character, loaded via Reflection from config. */
    private List<Ability> abilities = new ArrayList<>();

    /**
     * 4-second countdown after death before transitioning to GameOverState.
     * Gives time for the death animation and death sound to play.
     */
    private GameTimer deathTimer = new GameTimer(4.0, false);

    /**
     * "Invincibility Frame" timer.  While active, the player cannot be hit.
     * Granted after: taking a hit (future feature), emerging from underground,
     * teleporting through a portal, or after a rewind ends.
     */
    private GameTimer iFrameTimer = new GameTimer(0.5, false);

    /** The X position where the player died, used as the centre of the death shake. */
    private int dieX;
    /** The Y position where the player died, used as the centre of the death shake. */
    private int dieY;
    
    //Special filter setting for demo
    private Class<? extends Ability> demoAbilityFilter = null;
    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a GenericPlayer from a CharacterConfig.
     * Step 1: Loads all animations by reading config.animNames and building
     *         Animator objects from the character's sprite folder.
     * Step 2: Switches to the default animation (usually "Dash").
     * Step 3: Uses Java Reflection to instantiate each ability class listed
     *         in config.abilityClassNames.
     *
     * @param config  The character definition to build from.
     */
    public GenericPlayer(CharacterConfig config) {
        this.config = config;

        // ── STEP 1: Load animations ───────────────────────────────────────────
        for (String name : config.animNames) {
            animations.put(name, new Animator(config.folderName, name, config.scale));
        }
        setAnimation(config.defaultAnim);

        // ── STEP 2: Load abilities via Reflection ─────────────────────────────
        // Class.forName(name) finds the class by its String name at runtime.
        // .getDeclaredConstructor().newInstance() calls the no-arg constructor.
        // If the name is wrong or the class has no no-arg constructor, the catch
        // block prints a message — there is NO crash, so check the console output.
        for (String className : config.abilityClassNames) {
            try {
                abilities.add((Ability) Class.forName(className)
                    .getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                System.out.println("Failed to load ability: " + className);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ANIMATION CONTROL
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Switches to the named animation if it exists and is not already playing.
     * Resets the new animation to frame 0 for a clean start.
     * If the player is dead, only "Lose" is allowed (prevents interrupting death anim).
     *
     * @param name  The animation name (must match a key in the animations map).
     */
    public void setAnimation(String name) {
        if (isDead && !name.equals("Lose")) return; // dead players stay in "Lose"
        if (!currentAnimName.equals(name) && animations.containsKey(name)) {
            currentAnimName  = name;
            currentAnimator  = animations.get(name);
            currentAnimator.reset(); // always start from frame 0
        }
    }

    /**
     * Switches to the named animation at a custom frame rate.
     * Changes the Animator's speed BEFORE switching, so the new speed takes effect.
     *
     * @param name   The animation name.
     * @param speed  The frame delay (higher = slower animation).
     */
    public void setAnimation(String name, int speed) {
        if (isDead && !name.equals("Lose")) return;
        if (animations.containsKey(name)) {
            animations.get(name).setSpeed(speed); // apply speed first
            setAnimation(name);                   // then switch
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MOVEMENT LOGIC  (called every frame by Player.act() when in PlayingState)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Runs all per-frame player logic in this order:
     *   1. Tick the i-frame (invincibility) timer.
     *   2. Update every ability (ability timers count down here).
     *   3. Check for ability keybind presses and activate if valid.
     *   4. Handle death: shake, wait, then transition to GameOverState.
     *      OR if alive: handle movement input.
     */
    @Override
    protected void movementLogic() {
        iFrameTimer.update((MyWorld) getWorld());

        boolean playerIsHidden = isHidden(); // true if Sticky Fingers is underground

        // ── Ability update + activation ───────────────────────────────────────
        for (Ability a : abilities) {
            
            //Do not use ability if in demo and not demo-ing current ability
            if (demoAbilityFilter != null && !demoAbilityFilter.isInstance(a)) {
                continue;
            }
            
             // Always update the ability so its timers run.
            // But if the player is hidden, skip updates for abilities that
            // would move the visible player (shouldHidePlayer = true means they
            // already know about the hide state).
            if (!playerIsHidden || a.shouldHidePlayer() || a.isActive()) {
                a.update(this, (MyWorld) getWorld());
            }

            // Check for key press — only activate if: key is held AND
            // (player is not hidden, OR this ability works while underground).
            if (Greenfoot.isKeyDown(a.getKeybind())) {
                if (!playerIsHidden || a.shouldHidePlayer()) {
                    a.activate(this, (MyWorld) getWorld());
                }
            }
        }

        // ── Death vs. normal movement ─────────────────────────────────────────
        if (isDead) {
            deathTimer.update((MyWorld) getWorld());
            if (!deathTimer.isExpired()) {
                shake(); 
            } else if (!((MyWorld) getWorld()).isRewinding()) {
                GameState currentState = ((MyWorld) getWorld()).getGSM().peekState();
                if (currentState instanceof AbilityDisplayState) {
                // Tell the demo state we died so it can restart the demo
                ((AbilityDisplayState) currentState).notifyPlayerDied();
                } else {
                    // Otherwise, proceed to the real Game Over screen
                    ((MyWorld) getWorld()).getGSM().changeState(new GameOverState());
                }
            }
        } else {
            // REPLACE handleStandardMovement(); WITH THIS:
            if (!((MyWorld) getWorld()).isRewinding()) {
                handleStandardMovement();
            }
        }
    }

    /**
     * Moves the player based on UP/DOWN arrow key input.
     * Clamps position to stay within the world bounds with padding.
     * Doubles speed if Ability_MadeInHeaven is currently active.
     */
    private void handleStandardMovement() {
        int speed = config.moveSpeed;

        // Check if any ability is doubling the movement speed (Made in Heaven).
        for (Ability a : abilities) {
            if (a instanceof Ability_MadeInHeaven && a.isActive()) speed *= 2;
        }

        int nextX = getX();
        int nextY = getY();

        if (Greenfoot.isKeyDown("up"))   nextY -= speed;
        if (Greenfoot.isKeyDown("down")) nextY += speed;

        
        // Clamp: keep the player at least 'padding' pixels from any edge.
        int padding = GameConfig.s(30);
        int bottomBound = ((MyWorld) getWorld()).getBottomBound();
        nextX = Math.max(padding, Math.min(getWorld().getWidth()  - padding, nextX));
        nextY = Math.max(padding, Math.min(bottomBound - padding, nextY));
        
        setLocation(nextX, nextY);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DEATH
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Kills the player — but only if no protective condition blocks it:
     *   - i-frames active (just got hit recently, or just used a portal)
     *   - already dead
     *   - Ability_StandPunch is active (invincible while punching)
     *   - a shouldHidePlayer() ability is active (underground = invincible)
     *
     * When death proceeds:
     *   Sets isDead, switches to "Lose" animation, plays death sound,
     *   records death position, starts the 4-second death timer.
     */
    @Override
    public void die() {
        if (iFrameTimer.isActive() || isDead) return;

        // Check ability-based invincibility
        for (Ability a : abilities) {
            if (a.isActive() && a instanceof Ability_StandPunch) return; // invincible while punching
            if (a.shouldHidePlayer()) return;                             // underground = invincible
        }

        isDead = true;
        setAnimation("Lose");
        AudioManager.playPool(config.deathSoundKey); // random death voice
        dieX = getX();
        dieY = getY();
        deathTimer.start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ANIMATION LOGIC  (called every frame by Player.act() when in PlayingState)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Updates the displayed sprite each frame.
     * Also handles two visual effects:
     *   - Hiding: sets transparency to 0 when underground (Sticky Fingers).
     *   - I-frame blinking: alternates transparency every 4 frames while invincible.
     */
    @Override
    protected void animationLogic() {
        if (getWorld() == null) return;
        setImage(currentAnimator.getCurrentFrame()); // advance and display next frame

        // Check if any ability wants us hidden (Sticky Fingers underground)
        boolean hidden = false;
        for (Ability a : abilities) {
            if (a.shouldHidePlayer()) { hidden = true; break; }
        }

        if (hidden) {
            getImage().setTransparency(0); // fully invisible underground
            return; // skip i-frame blinking while hidden
        }

        // I-frame blink: flicker every 4 frames (2 visible, 2 dim) while active
        if (iFrameTimer.isActive() && !iFrameTimer.isExpired()) {
            boolean dim = (iFrameTimer.getRemainingFrames() % 4 < 2);
            getImage().setTransparency(dim ? 60 : 255);
        } else {
            getImage().setTransparency(255); // fully visible otherwise
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Custom circular hitbox: computes the Euclidean distance between the
     * centres of this player and the attacker actor, then compares it to
     * half the player's image width (scaled by the padding factor).
     * A padding < 1 makes the hitbox smaller (more forgiving); > 1 makes it larger.
     *
     * @param attacker  The obstacle to check against.
     * @param padding   Scale factor for the hitbox radius.
     * @return          True if within hitting distance.
     */
    @Override
    public boolean checkCustomHitbox(Actor attacker, double padding) {
        double dist = Math.hypot(getX() - attacker.getX(), getY() - attacker.getY());
        return dist < (getImage().getWidth() * 0.5 * padding);
    }

    /** Shakes the player around the death position — visual feedback on death. */
    private void shake() {
        setLocation(
            dieX + Greenfoot.getRandomNumber(5) - 2,
            dieY + Greenfoot.getRandomNumber(5) - 2
        );
    }

    /**
     * Grants the player invincibility for the given duration.
     * Resets and starts the i-frame timer.
     *
     * @param sec  Duration in seconds.
     */
    public void startIFrame(double sec) {
        iFrameTimer.setDuration(sec);
        iFrameTimer.start();
    }

    /**
     * Returns true if any equipped ability currently wants the player hidden
     * (i.e., the player is underground via Sticky Fingers).
     */
    public boolean isHidden() {
        for (Ability a : abilities) {
            if (a.shouldHidePlayer()) return true;
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ABILITY ACCESS  (used by UI and other systems)
    // ─────────────────────────────────────────────────────────────────────────

    /** @return The first ability in the list (index 0), or null if none. */
    public Ability getPrimaryAbility() {
        return abilities.isEmpty() ? null : abilities.get(0);
    }

    /** @return Total number of abilities loaded for this character. */
    public int getAbilityCount() { return abilities.size(); }

    /** @return The ability at the given index, or null if out of range. */
    public Ability getAbilityAt(int index) {
        return (index >= 0 && index < abilities.size()) ? abilities.get(index) : null;
    }

    /**
     * Returns only the abilities that should display a UI icon (shouldShowIcon() == true).
     * Ability_Mandom returns false here because the RewindBar already shows its state.
     *
     * @return  Filtered list of abilities for icon display.
     */
    public List<Ability> getVisibleAbilities() {
        List<Ability> visible = new ArrayList<>();
        for (Ability a : abilities) {
            if (a.shouldShowIcon()) visible.add(a);
        }
        return visible;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TIME MACHINE IMPLEMENTATION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Internal data class for the rewind snapshot.
     * Stores everything about the player at one moment in time.
     */
    private static class PlayerMemento {
        boolean isDead;
        String  anim;
        int     deathFrames;   // remaining frames on the death timer
        int     iFrameFrames;  // remaining frames on the i-frame timer
        List<Object> abilityStates = new ArrayList<>(); // one snapshot per ability
    }

    /**
     * Captures the player's complete state for the rewind history.
     * Called every frame by Time_RewindManager.record().
     */
    @Override
    public Time_ActorMemento captureState() {
        PlayerMemento m = new PlayerMemento();
        m.isDead       = isDead;
        m.anim         = currentAnimName;
        m.deathFrames  = deathTimer.getRemainingFrames();
        m.iFrameFrames = iFrameTimer.getRemainingFrames();
        for (Ability a : abilities) m.abilityStates.add(a.captureState());
        return new Time_ActorMemento(this, getX(), getY(), m);
    }

    /**
     * Restores the player to a past state from the rewind history.
     * Called by Time_RewindManager.restoreSnapshot() during rewind.
     */
    @Override
    public void restoreState(Time_ActorMemento m) {
        PlayerMemento data = (PlayerMemento) m.customData;
        this.isDead = data.isDead;
        setAnimation(data.anim);

        deathTimer.setRemainingFrames(data.deathFrames);
        iFrameTimer.setRemainingFrames(data.iFrameFrames);

        // Re-activate or deactivate timers to match their past state
        if (data.iFrameFrames > 0) iFrameTimer.start(); else iFrameTimer.stop();

        // Restore each ability from its own saved snapshot
        for (int i = 0; i < abilities.size(); i++) {
            abilities.get(i).restoreState(data.abilityStates.get(i));
        }
    }
    
    /**
     * Returns whether the character has a certain ability
     * Called by PlayingState when deciding what icons to add 
     * (specifically, rewind bar for Mandom)
     */
    public boolean hasAbility(Class<? extends Ability> abilityClass) {
        for (Ability a : abilities) {
            if (abilityClass.isInstance(a)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if a specific ability class is currently active.
     * Used by the Demo Engine to know when the player followed instructions.
     */
    public boolean isAbilityActive(Class<? extends Ability> clazz) {
        for (Ability a : abilities) {
            if (clazz.isInstance(a) && a.isActive()) return true;
        }
        return false;
    }
    
    /**Gets a list of all abilities*/
    public List<Ability> getAllAbilities() {
        return abilities;
    }
    
    /** Gets a specific ability so the Demo Engine can check its status. */
    public Ability getAbility(Class<? extends Ability> clazz) {
        for (Ability a : abilities) {
            if (clazz.isInstance(a)) return a;
        }
        return null;
    }
        
    //Special Setting for Demo
    public void setDemoAbilityFilter(Class<? extends Ability> clazz) {
        this.demoAbilityFilter = clazz;
    }
}
