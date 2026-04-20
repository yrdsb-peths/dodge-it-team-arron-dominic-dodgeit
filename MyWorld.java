import greenfoot.*;

public class MyWorld extends World {
    private GameStateManager gsm;
    
    public PlayingState playingState; 
    //Game state manager is a class we defined to manage game states
    //Game states are stored as classes, with the "blueprint" (interface) GameState, 
    //which requires:
    //an enter method, an update method, and an exit method
    
    public MyWorld() {
        //Initiate a world (arguments: width, height, idk, bound)
        //False means things are not limited by the boundary and can go through it
        //This is helpful as we do not want a limiting boundar
        super(GameConfig.WORLD_WIDTH,GameConfig.WORLD_HEIGHT, 1,false);
        //Initiate a game state manager, pasing this world (MyWorld) as the argument
        gsm = new GameStateManager(this);//gsm stands for game state manager.
        
        //Set the order of rendeirng:
        //UI on top, then Dio, then after images, then the roadrollers
        setPaintOrder(Banner.class, UIText.class,UI_AbilityIcon.class, UI_RewindBar.class, FX_RewindOverlay.class, 
                      Exclaimation.class, PathWarning.class, 
                      TheWorldStand.class, Dio.class, FX_Afterimage.class, Obstacles.class, ScrollingRoad.class);
                      
        //Start the game in the playing state
        //Remember, pushState adds the state on top of the stack,and enters that state
        
        playingState = new PlayingState();
        gsm.pushState(new MenuState());
        //Initilaise the audio manager to load sounds into RAM
        AudioManager.init();
        
        
    }
    
    /*
     * Main function running the game.
     * We "replaced" the "act" method of greenfoot with our own update method to handle 
     * different game states and pausing. 
     * 
     * So actors do not get their movement logic called 60 times a second as in act,
     * but get controlled by our own update method, depending on the game state.
     */
    public void act(){
        //All logic is delegated to the state machine,
        //which delegates the taskto the specific state classes
        gsm.update();
    }
    
    /*
     * Getter method to get the game state manager.
     */
    public GameStateManager getGSM(){
        return gsm;
    }
    
    public boolean isRewinding() {
        GameState s = gsm.peekState();
        if (s instanceof PlayingState) {
            return ((PlayingState) s).isRewinding();
        }
        return false;
    }
    
}
