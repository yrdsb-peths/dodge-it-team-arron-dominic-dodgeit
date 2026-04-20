
import greenfoot.*;
import java.util.Stack;
/**
 * Write a description of class GameStateManager here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class GameStateManager  
{
    private Stack<GameState> stateStack;
    /*
     * A Stack is a collection that follows the Last-In, First-Out (LIFO) principle. 
     * Imagine a physical stack of plates: 
     * you can only add a new plate to the top, 
     * and you must remove the top plate before you can reach the ones underneath.
     * 
     * For our game state, we must deal with one state before dealing with the one behind it:
     * for example, exit pause game before exiting time-stop ability 
     * 
     * Common methods of stack are:
     * push => adds a new "plate" on top of the stack
     * pop => takes away the top plate, and remove it. 
     * peek => looks at the top plate, but does not remove it
     */
    
    private MyWorld world;
    
    public GameStateManager(MyWorld world){
        //Creates a stack of states for the world
        this.stateStack = new Stack<>();
        this.world = world;
    }
    
    /*
     * Puts a "plate"(state) on top and enter it
     */
    public void pushState(GameState state){
        stateStack.push(state);
        state.enter(world);
    }
    
    /*
     * Throws away the top "plate"(state) and exit it
     */
    public void popState(){
        if(!stateStack.isEmpty()){
            GameState popped = stateStack.pop();
            popped.exit(world);
        }
    }
    
    /*
     * Change the current state to another one by:
     * 1. Pop the current state (and exit it)
     * 2. push the new state  (and enter it)
     */
    public void changeState(GameState state){
        if(!stateStack.isEmpty()){
            stateStack.pop().exit(world);
        }
        stateStack.push(state);
        state.enter(world);
    }
    
    /*
     * The main method that updates the game. 
     * Only update the state on top on the stack.
     */
    public void update(){
        if(!stateStack.isEmpty()){
            stateStack.peek().update(world);
        }
    }
    
    /*
     * Checks if the asked state is currently running.
     * Useful for actors to check the current state.
     * 
     * Parameter: a class (that represents state)
     */
    public boolean isState(Class<?> stateClass){
        if(stateStack.isEmpty()) return false;
        return stateClass.isInstance(stateStack.peek());
    }
    
    /*
     * Returns the current game state (unless its null)
     */
    public GameState peekState() {
        return stateStack.isEmpty() ? null : stateStack.peek();
    }
    
}
