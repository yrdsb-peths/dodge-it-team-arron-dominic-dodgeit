import greenfoot.*;
import java.util.ArrayList;
import java.util.List;
/**
 * Write a description of class PausedState here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class PausedState implements GameState
{
    private List<Actor> uiElements = new ArrayList<>();
    
    public void enter(MyWorld world){
        //The game is paused
        //We will show something like a "GAME PAUSED" banner, but currently its empty
        UIText title = new UIText("PAUSED", GameConfig.s(80), Color.RED);
        addUI(world, title, world.getWidth()/2, GameConfig.s(150));

    }
    
    public void update(MyWorld world){
        //Handle normal game logic, like spawning obstacles, movement logics etc.
        
        //Sample of swithicng state: use "p" to unpause, but we can also use a button
        
        if("w".equals(Greenfoot.getKey())){
            //Remove the pause state, resume to normal/whatever state previosly running.
            world.getGSM().popState();
            
        }
        
        //Some eastereggs?
    }
    
    public void exit(MyWorld world){
        //Clean things up as we leave this state, such as removing the "GAME PAUSED" banner
        world.removeObjects(uiElements);
    }
    
    private void addUI(MyWorld world, Actor a, int x, int y) {
        world.addObject(a, x, y);
        uiElements.add(a);
    }
}
