import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

public class MenuState implements GameState {

    /** All actors created by this state — removed in exit(). */
    private List<Actor> uiElements = new ArrayList<>();

    public void enter(MyWorld world) {
        // ── Background Setup ──────────────────────────────────────────────────
        world.setBackground(new GreenfootImage("dodge_it.png")); 
        world.getBackground().scale(GameConfig.WORLD_WIDTH + 200, GameConfig.WORLD_HEIGHT);

        // --- THE FIX: RELIABLE LABEL PLACEMENT ---
        // Synchronize with GameConfig!
        int currentHiScore = SaveManager.getInt("all_time_high");
        
        if (currentHiScore >= GameConfig.LEGACY_UNLOCK_SCORE) {
            // Move it further left (s(120) from edge) and lower (s(50) from top)
            int x = world.getWidth() - GameConfig.s(120); 
            int y = GameConfig.s(50);
            
            // Use a bigger font (s(18)) so it's actually readable
            UIText legacyBtn = new UIText("[ L : VIEW LEGACY ]", GameConfig.s(18), Color.YELLOW);
            addUI(world, legacyBtn, x, y);
            
        }
    }

    @Override
    public void update(MyWorld world) {
        // ── Ambience ──────────────────────────────────────────────────────────
        if (Greenfoot.getRandomNumber(120) == 0) {
            int startX = world.getWidth() + GameConfig.s(300);
            world.addObject(new FX_MenuAmbulance(), startX, GameConfig.s(290));
        }

        AudioManager.playLoop("menu_bgm"); 

        // ── Inputs ────────────────────────────────────────────────────────────
        String key = Greenfoot.getKey();
        
        if ("enter".equals(key)) {
            world.getGSM().changeState(new CharacterSelectState());
        } 
        
        // Handle the secret Legacy key if they have the score
        else if ("l".equals(key) && SaveManager.getInt("all_time_high") >= 1) {
            world.getGSM().pushState(new LegacyState());
        }
    }

    @Override
    public void exit(MyWorld world) {
        // Cleanup
        world.getBackground().setColor(Color.WHITE); 
        world.getBackground().fill();
        
        world.removeObjects(uiElements);
        uiElements.clear();
        
        // Clear the ambulances
        world.removeObjects(world.getObjects(FX_MenuAmbulance.class));
    }

    private void addUI(MyWorld world, Actor a, int x, int y) {
        world.addObject(a, x, y);
        uiElements.add(a);
    }
}