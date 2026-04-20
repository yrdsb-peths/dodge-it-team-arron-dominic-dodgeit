import java.util.ArrayList;
import greenfoot.*;
import java.io.File;
/**
 * Write a description of class Animator here.
 * 
 * Animator takes in the time and tracks the correct image from the animation
 * Call animator with
 * 
 */


/**
 * ANIMATION LIST FOR DIO
 * ---------------------
 * Dash         - Runs
 * High         - brain massage
 * Idle         - Standing still
 * Intro        - Cool intro
 * Lose         - Defeat state
 * Roll         - Dodge move
 * Scratch       - Brain massag + wry
 * Up           - Up slash
 * WalkLeft     - Moving left
 * WalkRight    - Moving right
 * Wry          - WRYYYYYYY
 * 
 * (special: to be implemented later)
 * Description: Dio teleports backwards as his Stand appears to punch.
 * Requires two animations play simultaneously, and locatio change.
 * WorldPunch   - Special attack
 * Teleport     - pairs with WorldPunch
 * 
 */


public class Animator  
{
    public static final int FRAME_DELAY = 6;//Frames per image, higher = slower; this is the standard speed
    private GreenfootImage[] frames;
    private int currentFrame = 0;
    private int timer = 0;
    private int speed;//Higher = slower (frames between images)
    
    /*
     * The more detailed constructor for animator that takes in more parameters 
     * to help with debugging and customizability. 
     */
    public Animator(String baseFolder,String folderName,String prefix, int frameCount, int speed, double scaleFactor){
        this.speed = speed;
        frames = new GreenfootImage[frameCount]; 
        
        for (int i = 0 ; i < frameCount; i++){
            //%03d => 3 digits with leading zero
            String suffix = String.format("%03d",i);
            String fileName = baseFolder + "/" + folderName +"/" + prefix + "_" + suffix + ".png";
            frames[i] = new GreenfootImage(fileName);
            int newWidth = (int)(frames[i].getWidth() * scaleFactor);
            int newHeight = (int)(frames[i].getHeight() * scaleFactor);
            frames[i].scale(newWidth, newHeight);

        }
    }
    
    /*
     * The more general constructor for animator that only takes in folder name
     * Easy to call
     */
    public Animator(String baseFolder, String folderName, double scaleFactor) {
        this(baseFolder, folderName, folderName, countFrames(baseFolder, folderName), FRAME_DELAY, scaleFactor);
    }

    /*
     * The more general constructor for animator that only takes in folder name AND SPEED
     * Easy to call
     */
    public Animator(String baseFolder, String folderName, int speed, double scaleFactor) {
        this(baseFolder, folderName, folderName, countFrames(baseFolder, folderName), speed, scaleFactor);
    }
    
    /*
     * Counts the number of frames in the folder for easy registration
     * HASDS NOTHING TO DO WITH ANIMATION
     */
    private static int countFrames(String baseFolder, String folderName) {
    // Greenfoot looks for images in the "images" folder of your project
        File dir = new File("images/" + baseFolder + "/"+ folderName);
    
    // Check if the directory exists
        if (dir.exists() && dir.isDirectory()) {
            // Filter to only count .png files
            String[] files = dir.list((d, name) -> name.toLowerCase().endsWith(".png"));
            return (files != null) ? files.length : 0;
        }
        return 0;
    }
    
    /*
     * Find the current image given the current time.
     */
    public GreenfootImage getCurrentFrame(){
        timer++;
        if(timer >= speed){
            timer = 0;
            //In case folder is empty due to some human mistake
            if (frames.length > 0) {
                currentFrame = (currentFrame +1)%frames.length; //loop back to start
            }
        }
        return frames[currentFrame];
    }
    
    /*
     * Resets timer and frame. 
     */
    public void reset(){
        currentFrame = 0; 
        timer = 0;
    }
    
    /*
     * adjust speed upon request
     */
    public void setSpeed(int speed) {
    this.speed = speed;
    }
}
