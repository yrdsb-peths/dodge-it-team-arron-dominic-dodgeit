import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

public class MenuState implements GameState {

    /** All actors created by this state — removed in exit(). */
    private List<Actor> uiElements = new ArrayList<>();

    @Override
    public void enter(MyWorld world) {
        world.setBackground(new GreenfootImage("dodge_it.png")); 
        world.getBackground().scale(GameConfig.WORLD_WIDTH + 200, GameConfig.WORLD_HEIGHT);

        int currentHiScore = SaveManager.getInt("all_time_high");
        
        // FIX: Use 1 here so you can see it immediately for testing!
        if (currentHiScore >= GameConfig.LEGACY_UNLOCK_SCORE) { 
            // Move it slightly more toward the center (s(150)) so it's not tucked in the corner
            int x = world.getWidth() - GameConfig.s(150); 
            int y = GameConfig.s(40);
            
            UIText legacyBtn = new UIText("[ L : VIEW LEGACY ]", GameConfig.s(20), Color.YELLOW);
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