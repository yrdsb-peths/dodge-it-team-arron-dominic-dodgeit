import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

public class GameOverState implements GameState {
    private List<Actor> uiElements = new ArrayList<>();

    public void enter(MyWorld world) {
        AudioManager.playLoop("lost_bgm"); 
        
        ScoreManager.updateHighScore();
        int finalScore = ScoreManager.getScore();
        int bestScore = ScoreManager.getHighScore();
        
        // Create UI Elements with scaled font sizes
        UIText title = new UIText("RETIRED", GameConfig.s(80), Color.RED);
        UIText scoreTxt = new UIText("Final Score: " + finalScore, GameConfig.s(30), Color.RED);
        UIText bestTxt = new UIText("Best Survival: " + bestScore, GameConfig.s(30), Color.RED);
        UIText restartPrompt = new UIText("Press ENTER to Restart", GameConfig.s(25), Color.BLACK);

        // Position them using scaled Y-coordinates
        int midX = world.getWidth() / 2;
        addUI(world, title, midX, GameConfig.s(150));
        addUI(world, scoreTxt, midX, GameConfig.s(200));
        addUI(world, bestTxt, midX, GameConfig.s(250));
        addUI(world, restartPrompt, midX, GameConfig.s(300));
    }

    public void update(MyWorld world) {
        if ("enter".equals(Greenfoot.getKey())) {
            world.getGSM().changeState(new PlayingState());
        }
    }

    public void exit(MyWorld world) {
        AudioManager.stop("lost_bgm"); 
        world.removeObjects(uiElements);
    }

    private void addUI(MyWorld world, Actor a, int x, int y) {
        world.addObject(a, x, y);
        uiElements.add(a);
    }
}