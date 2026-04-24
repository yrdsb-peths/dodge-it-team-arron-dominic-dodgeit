import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * GameOverState.java — THE FINAL JUDGMENT
 * ─────────────────────────────────────────────────────────────────────────────
 * Displays the results of the run, the obituary, and persistent records.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class GameOverState implements GameState {

    private List<Actor> uiElements = new ArrayList<>();

    @Override
    public void enter(MyWorld world) {
        // 1. AUDIO CLEANUP
        AudioManager.stopAllAbilities();
        AudioManager.stop(GameConfig.ACTIVE_CHARACTER.bgmKey);
        AudioManager.playLoop("lost_bgm");

        // 2. DATA GATHERING
        ScoreManager.updateHighScore();
        int finalScore    = ScoreManager.getScore();
        int allTimeBest   = DataManager.getInt("all_time_high");
        String obituary   = ObituaryManager.getRandomObituary(GameConfig.ACTIVE_CHARACTER);
        String fav        = SaveManager.getFavoriteCharacter();
        
        int totalSeconds = SaveManager.getInt("total_playtime");
        String timeStr = String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60); 
        
        // 3. BACKGROUND SETUP
        world.setBackground(new GreenfootImage("game_over.png")); 
        world.getBackground().scale(GameConfig.WORLD_WIDTH + 200, GameConfig.WORLD_HEIGHT);

        // 4. UI CONSTRUCTION
        int midX = world.getWidth() / 2;
        int panelY = GameConfig.s(240);
        int panelW = world.getWidth() - 100;
        int panelH = GameConfig.s(170);

        // ── DARK BACKDROP PANEL ──
        addUI(world, new UI_Panel(panelW, panelH, new Color(0, 0, 0, 160)), midX, panelY);

        // ── SCORE & STATS BLOCK (Grouped higher up in the panel) ──
        int statsStartY = panelY - GameConfig.s(55);
        
        Color scoreColor = (finalScore >= allTimeBest) ? Color.YELLOW : Color.WHITE;
        addUI(world, new UIText("FINAL SCORE: " + finalScore, GameConfig.s(22), scoreColor), midX, statsStartY);
        
        String bestText = (finalScore >= allTimeBest) ? "NEW ALL-TIME RECORD!" : "ALL-TIME BEST: " + allTimeBest;
        addUI(world, new UIText(bestText, GameConfig.s(18), Color.CYAN), midX, statsStartY + GameConfig.s(25));
        
        addUI(world, new UIText("Favorite: " + fav + "  |  Total Time: " + timeStr, GameConfig.s(15), Color.LIGHT_GRAY), midX, statsStartY + GameConfig.s(45));
        
        // ── THE OBITUARY (The Flavor) ──
        // THE FIX: Use the built-in wrapping of UIText. 
        // We set the maxWidth to be slightly smaller than the panel.
        int wrapWidth = panelW - GameConfig.s(40); 
        UIText obiText = new UIText("\"" + obituary + "\"", GameConfig.s(17), Color.WHITE, wrapWidth);
        
        // Place it in the lower half of the panel
        addUI(world, obiText, midX, panelY + GameConfig.s(25));
    }

    // REMOVE the addWrappedText method entirely!

    @Override
    public void update(MyWorld world) {
        // Simple transition back to main menu
        if ("enter".equals(Greenfoot.getKey())) {
            world.getGSM().changeState(new MenuState());
        }
    }

    @Override
    public void exit(MyWorld world) {
        // Cleanup visuals and character-specific death sounds
        world.getBackground().setColor(Color.WHITE); 
        world.getBackground().fill();
        AudioManager.stop("lost_bgm");
        AudioManager.stopPool(GameConfig.ACTIVE_CHARACTER.deathSoundKey);
        
        world.removeObjects(uiElements);
        uiElements.clear();
    }

    private void addUI(MyWorld world, Actor a, int x, int y) {
        world.addObject(a, x, y);
        uiElements.add(a);
    }
    
    private void addWrappedText(MyWorld world, String text, int fontSize, Color color, int midX, int startY, int maxCharsPerLine) {
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int lineCount = 0;
        int lineSpacing = fontSize + 5; // Adjust vertical gap between lines
    
        for (String word : words) {
            // Check if adding the next word exceeds the limit
            if (currentLine.length() + word.length() > maxCharsPerLine) {
                // Add the completed line as a UI element
                addUI(world, new UIText(currentLine.toString().trim(), fontSize, color), midX, startY + (lineCount * lineSpacing));
                currentLine = new StringBuilder();
                lineCount++;
            }
            currentLine.append(word).append(" ");
        }
        
        // Add the final remaining bit of text
        if (currentLine.length() > 0) {
            addUI(world, new UIText(currentLine.toString().trim(), fontSize, color), midX, startY + (lineCount * lineSpacing));
        }
    }
}