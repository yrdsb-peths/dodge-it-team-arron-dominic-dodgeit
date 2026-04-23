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
        int sessionBest   = ScoreManager.getHighScore();
        int allTimeBest   = DataManager.getInt("all_time_high");
        String obituary   = ObituaryManager.getRandomObituary(GameConfig.ACTIVE_CHARACTER);
        String fav = SaveManager.getFavoriteCharacter();
        int totalTime = SaveManager.getInt("total_playtime") / 60; 
        
        // 3. BACKGROUND SETUP
        world.setBackground(new GreenfootImage("game_over.png")); 
        world.getBackground().scale(GameConfig.WORLD_WIDTH + 200, GameConfig.WORLD_HEIGHT);

        // 4. UI CONSTRUCTION
        int midX = world.getWidth() / 2;
        int topY = GameConfig.s(140);

        // ── DARK BACKDROP PANEL (Makes text readable) ──
        addUI(world, new UI_Panel(world.getWidth() - 100, GameConfig.s(160), new Color(0, 0, 0, 160)), midX, GameConfig.s(240));

        // ── RESULTS TITLE ──

        // ── SCORE BLOCK ──
        Color scoreColor = (finalScore >= allTimeBest) ? Color.YELLOW : Color.WHITE;
        addUI(world, new UIText("FINAL SCORE: " + finalScore, GameConfig.s(20), scoreColor), midX, topY + GameConfig.s(45));
        
        String bestText = (finalScore >= allTimeBest) ? "NEW ALL-TIME RECORD!" : "ALL-TIME BEST: " + allTimeBest;
        addUI(world, new UIText(bestText, GameConfig.s(20), Color.CYAN), midX, topY + GameConfig.s(65));
        
        // Lowered these Y values so they don't bunch up
        addUI(world, new UIText("Favorite Character: " + fav, 20, Color.WHITE), midX, topY + GameConfig.s(85));
        addUI(world, new UIText("Total Playtime: " + totalTime + " mins", 20, Color.CYAN), midX, topY + GameConfig.s(105));
        
        // ── THE OBITUARY (The Flavor) ──
        // Pushed the obituary lower (to s(170)) so it doesn't hit the text above it
        addWrappedText(world, "\"" + obituary + "\"", GameConfig.s(16), Color.LIGHT_GRAY, midX, topY + GameConfig.s(130), 100);
    }

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