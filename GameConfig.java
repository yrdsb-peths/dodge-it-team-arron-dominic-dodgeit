import greenfoot.*;

public class GameConfig {
    //Character active is at the very bottom
    
    // 0. GLOBAL SCALING: This scales every image. By default, it is 1. To make things larger and occupy more screen, it is currently 1.5 
    public static final float SCALE = 1.5f; 
    
    // 2. WORLD SETTINGS
    public static final int WORLD_WIDTH = s(600);//World widht is 600 pixels
    public static final int WORLD_HEIGHT = s(400);// World height is 400 pixels
    public static final int LANE_HEIGHT = s(80); // Lanes are 80px apart
    
    // 3. PLAYER SETTINGS (DIO)
    public static final int DIO_MOVE_SPEED = s(5);//Dio moves 5 pixels per frame
    public static final double DIO_BASE_SCALE = 0.8 * SCALE; //Dio should be 0.8 its image size

    // 4. ENEMY SETTINGS
    //a. Obstacle Size
    public static final int ROADROLLER_WIDTH = s(80);//Roadroller is 80 pixels wide
    
    public static final int TRAIN_WIDTH = s(130);
        // Train is 130 pixels wide. (Its an Ambulance, but its called a train because it works likes a train and looks like an ambulance).
        //If you dont know why an ambulance is this powerful, you probably have to ask Kira Yoshkage, i think he knows the answer.
    
    //b. Difficulty settings
    public static final int LEVEL_UP_TIME = 200;//Game gets more difficulty per 200 frame
    public static final int ROADROLLER_RATE = 30;// number of frames for a car, decreass with difficulty;
    public static final int ROADROLLER_MIN_RATE= 17;
    public static final int ROADROLLER_SPEED = s(6);//Roadroller moves 6 frames per second
    public static final int ROADROLLER_MAX_SPEED = s(10);
    
    public static final int TRAIN_RATE = 200;// number of frames for a car, decreass with difficulty;
    public static final int TRAIN_MIN_RATE = 36;
    public static final int TRAIN_SPEED = s(25);//Train moves at 25 pixesl per frame by default
    public static final int TRAIN_MAX_SPEED = s(50);//Train can move at 50 pixels per frame at its peak
      
    
    // Rewind Time Settings
    
    public static final int MAX_REWIND_TIME = 360; //6 seconds
    public static final int REWIND_TIME = 120; // 2 seconds
    
    //Ability: Made In Heaven Settings
    public static final int MIH_TICK_SPEED = 48; 
    public static final int NORMAL_TICK_SPEED = 50; 
    public static final int MIH_COOLDOWN = 3;// (3 seconds)
    
    //Ability: World Punch Setting
    public static final double WORLD_PUNCH_DURATION = 3.5; //in seconds
    public static final double WORLD_PUNCH_COOLDOWN = 5.0; //in seconds
    
    //Ability: Sticky Fingers Settings
    public static final String STICKY_FINGER_BUTTON = "f";
    public static final int PORTAL_MARGIN = s(35);       // how close to edge triggers portal
    public static final int PORTAL_COOLDOWN_FRAMES = 20; // frames before you can portal again
    public static final double PORTAL_IFRAME_DURATION = 1; // Invincibility time
    public static final double PORTAL_COOLDOWN_DURATION = 2; // Time before you can portal again  
    
    //Volume Setting
    public static final int MASTER_VOLUME = 50;
    
    //Buttons
    
    public static final String TIME_STOP_BUTTON = "w";//Handled in PlayingState
    public static final String REWIND_TIME_BUTTON = "r";//Hanlded in PlayingState
    public static final String MIH_BUTTON = "s";//Handled in Dio
    public static final String STAND_PUNCH_BUTTON = "e";//Hanlded in Dio
    
    // Road & Lanes
    public static final int ROAD_SCROLL_SPEED = s(5);//The background "roads" moves at 5 pixels per second
    public static final int ROAD_MAX_SPEED = s(5);
    // We calculate lanes based on the scaled world height
    public static final int[] LANES = { s(40), s(120), s(200), s(280), s(360) };//These are the locations of the "lanes"
    
        
    // UI (Font Size)
    public static final int FONT_SIZE_LARGE = s(80);
    public static final int FONT_SIZE_SMALL = s(30);
    
    // THE HELPER: Scales any pixel value based on the global SCALE
    public static int s(int pixels) {
        return Math.round(pixels * SCALE);
    }
    
    //Character Active- later will implement character choosing menu
    public static CharacterConfig ACTIVE_CHARACTER = CharacterConfig.DIO;
}