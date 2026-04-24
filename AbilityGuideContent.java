import java.util.Map;
import java.util.HashMap;

public class AbilityGuideContent {
    private static final Map<Class<? extends Ability>, String> guides = new HashMap<>();

    static {
        guides.put(Ability_KingCrimson.class, 
            "--- EPITAPH & KING CRIMSON ---\n\n" +
            "Diavolo's power allows him to see and skip\n" +
            "the immediate future.\n\n" +
            "1. THE VISION (Epitaph):\n" +
            "HOLD [RIGHT ARROW] to see the future.\n" +
            "You become a GHOST. Move to a safe spot.\n\n" +
            "2. THE REVERT:\n" +
            "RELEASE [RIGHT] to return to the present.\n" +
            "The world goes back, but YOU stay put!\n\n" +
            "3. THE ERASURE (King Crimson):\n" +
            "While in vision, PRESS [Q] to commit.\n" +
            "The timeline skips forward permanently.\n\n" +
            "YOU KEEP THE SCORES DURING THE SKIPPED PERIOD\n" 

        );
        
        guides.put(Ability_DarkSpell01.class,
            "--- DARK VOID (Spell 01) ---\n\n" +
            "Unleashes a massive purple explosion.\n\n" +
            "Anything inside the radius is erased instantly.\n" +
            "You are INVINCIBLE while casting.\n\n" +
            "Use this to clear massive waves of cars.\n"+
            "RESONANCE: If a lane is FROZEN (Spell 02),\n" +
            "this clears the ENTIRE LANE instead!\n"+
            "The cooldown is then massively reduced"
        );

        guides.put(Ability_DarkSpell02.class,
            "--- LANE FREEZE (Spell 02) ---\n\n" +
            "Curses your current lane with dark ice.\n\n" +
            "Cars currently in the lane until ability endsFREEZE.\n" +
            "New cars entering the lane hit an invisible\n" +
            "wall until the spell ends.\n"+
            "Cooldown is short so abuse it!"
        );
        
         // --- MADE IN HEAVEN ---
        guides.put(Ability_MadeInHeaven.class,
            "--- MADE IN HEAVEN ---\n\n" +
            "PRESS [S] to accelerate time.\n\n" +
            "The world's clock slows down, allowing you\n" +
            "to move with extreme speed and precision.\n\n" +
            "Use the afterimage trail to judge your\n" +
            "movement through dense traffic."
        );

        // --- BUCCIARATI / STICKY FINGERS ---
        guides.put(Ability_StickyFingers.class,
            "--- STICKY FINGERS ---\n\n" +
            "A master of spatial navigation.\n\n" +
            "1. ZIPPER HIDE (Press [F]):\n" +
            "Zip into the road to become INVINCIBLE.\n" +
            "You cannot earn score while hidden.\n\n" +
            "2. PORTAL WARP (Passive):\n" +
            "Walk into the top or bottom screen edge\n" +
            "to warp to the opposite side instantly."
        );

        // --- RINGO / MANDOM ---
        guides.put(Ability_Mandom.class,
            "--- MANDOM ---\n\n" +
            "The power to rewind time by 2 seconds.\n\n" +
            "PRESS [R] to undo a mistake.\n\n" +
            "Everything—the cars, your position, and\n" +
            "the score—returns to where it was.\n\n" +
            "Watch the 'TIME' bar in the corner to see\n" +
            "how much history you have stored."+
            "Can rewind even after you are dead"
        );
        
        guides.put(Ability_TheWorld.class,
            "--- THE WORLD (Time Stop) ---\n\n" +
            "PRESS[W] to stop time completely.\n\n" +
            "The world freezes, but you can move freely.\n" +
            "Use this window to reposition yourself safely\n" +
            "or escape impossible situations.\n\n" +
            "Time resumes automatically after 4 seconds."
        );
    }

    public static String get(Class<? extends Ability> clazz) {
        return guides.getOrDefault(clazz, "No detailed guide available.");
    }
}