/*
 * ─────────────────────────────────────────────────────────────────────────────
 * GameOverState.java  —  THE DEATH / GAME OVER SCREEN
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   Entered via changeState() when the player's death timer expires
 *   (in GenericPlayer.movementLogic()).
 *   Shows: "RETIRED", final score, best score, restart prompt.
 *   Pressing ENTER returns to the MenuState (NOT PlayingState — fully restart).
 *
 * Score display:
 *   updateHighScore() is called first so the high score reflects this run.
 *   Both finalScore and bestScore are captured before any UI is built.
 *
 * Interacts with:
 *   ScoreManager (reads scores), AudioManager (plays/stops music),
 *   GameStateManager (changeState to MenuState), UIText (labels)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

public class GameOverState implements GameState {

    /** All actors created by this state — removed in exit(). */
    private List<Actor> uiElements = new ArrayList<>();

    @Override
    public void enter(MyWorld world) {
        AudioManager.stopAllAbilities();//Stop ability sounds
        AudioManager.playLoop("lost_bgm"); // play the loss music

        // Freeze the scores before building the UI
        ScoreManager.updateHighScore();
        int finalScore = ScoreManager.getScore();
        int bestScore  = ScoreManager.getHighScore();

        int midX = world.getWidth() / 2;
        // FETCH THE OBITUARY
        String text = ObituaryManager.getRandomObituary(GameConfig.ACTIVE_CHARACTER);
        // Display it under the scores (Adjust Y position as needed)
        addUI(world, new UIText(text, GameConfig.s(20), Color.BLACK), midX, GameConfig.s(290));
        
        world.setBackground(new GreenfootImage("game_over.png")); 
        world.getBackground().scale(GameConfig.WORLD_WIDTH+200, GameConfig.WORLD_HEIGHT);
        //addUI(world, new UIText("RETIRED",                          GameConfig.s(80), Color.RED),   midX, GameConfig.s(150));
        addUI(world, new UIText("Final Score: "   + finalScore,     GameConfig.s(30), Color.RED),   midX, GameConfig.s(200));
        addUI(world, new UIText("Best Survival: " + bestScore,      GameConfig.s(30), Color.RED),   midX, GameConfig.s(250));
        //addUI(world, new UIText("Press ENTER to Restart",           GameConfig.s(25), Color.BLACK), midX, GameConfig.s(300));
    

    }

    /** Waits for ENTER, then goes back to the main menu (full restart). */
    @Override
    public void update(MyWorld world) {
        if ("enter".equals(Greenfoot.getKey())) {
            world.getGSM().changeState(new MenuState());
        }
    }

    @Override
    public void exit(MyWorld world) {
        world.getBackground().setColor(Color.WHITE); world.getBackground().fill();
        AudioManager.stop("lost_bgm");
        world.removeObjects(uiElements);
        AudioManager.stopPool(GameConfig.ACTIVE_CHARACTER.deathSoundKey);
    }

    private void addUI(MyWorld world, Actor a, int x, int y) {
        world.addObject(a, x, y);
        uiElements.add(a);
    }
}
