import greenfoot.*;
/**
 *This interface governs gamemodes:
 *All game modes must have an enter, update and exit method. 
 */
public interface GameState  
{
    void enter (MyWorld world);
    
    void update(MyWorld world);
    
    void exit(MyWorld world);
}
