import greenfoot.*;
import java.util.Random;
/**
 * Write a description of class SpawnManager here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class SpawnManager  
{
  
    
    private int globalTimer = 0;
    
    //== Difficulty Settings ==
    private int difficultyLevel = 0;
    private int levelUpTime = GameConfig.LEVEL_UP_TIME;//Level up every 5 seconds
    
    //Control Roadroller rate
    private int roadrollerRate = GameConfig.ROADROLLER_RATE;// number of frames for a car
    private final int roadrollerMin = GameConfig.ROADROLLER_MIN_RATE;
    private int roadrollerSpeed = GameConfig.ROADROLLER_SPEED; // Manager tracks this now
    private final int roadrollerSpeedMax = GameConfig.ROADROLLER_MAX_SPEED; 
    
    //Control train rate
    private int trainRate = GameConfig.TRAIN_RATE;// number of frames for a car, decreass with difficulty
    private final int trainMin = GameConfig.TRAIN_MIN_RATE;
    private int trainSpeed = GameConfig.TRAIN_SPEED;//Increases with difficulty
    private final int trainSpeedMax = GameConfig.TRAIN_MAX_SPEED;
    
    //Unnecessary boolean 
    private boolean scorelessObstacle = false;
    
    private int masterSeed; //A master seed is used so that game reverse can be exactly replicated
    
    
    public SpawnManager() {
        masterSeed = Greenfoot.getRandomNumber(32052320);
    }
    public void update(MyWorld world){
        globalTimer++; 
        //Notice: this timer is exclusive to spawn manager, and only increases
        //when it gets called (which usually means game is runnin)
        
        if(globalTimer % levelUpTime == 0){
            increaseDifficulty();
        }
        
        if(globalTimer % roadrollerRate == 0){
            spawnRoadroller(world);
        }
        
        if(globalTimer % trainRate == 0){
            spawnTrain(world, trainSpeed);
        }
        
    }
    
    private void increaseDifficulty(){
        if(roadrollerRate > roadrollerMin) roadrollerRate -= 5;
        
        if(roadrollerSpeed < roadrollerSpeedMax) roadrollerSpeed += 1;
        
        if(trainRate > trainMin) trainRate -= 5;
        
        if(trainSpeed < trainSpeedMax) trainSpeed += 5;
        
        ScrollingRoad.increaseSpeed(1);
    }
    
    private void spawnRoadroller(MyWorld world) {
        int spawnY = getRandomLane();
        world.addObject(new Roadroller(roadrollerSpeed), world.getWidth(), spawnY);
    }

    private void spawnTrain(MyWorld world, int speed) {
        int spawnY = getRandomLane(); 
        
        int pathHeight = GameConfig.LANE_HEIGHT; 
        int exclaimXOffset = GameConfig.s(20);
        int trainXOffset = GameConfig.s(50);
        
        world.addObject(new Exclaimation(), world.getWidth() - exclaimXOffset, spawnY);
        world.addObject(new PathWarning(world.getWidth(), pathHeight), world.getWidth() / 2, spawnY);
        world.addObject(new Train(speed), world.getWidth() + trainXOffset, spawnY);
    }

    private int getRandomLane() {
        return GameConfig.LANES[GameRNG.getRandomNumber(GameConfig.LANES.length)];
    }
    
    
    

    //placeholder methods to prevent rewind manager form screaming
    //Currently it doesnt consider the rate probably
    // --- GETTERS (For Recording) ---
    public int getSpawnTimer() { return globalTimer; }
    public int getRoadrollerRate() { return roadrollerRate; }
    public int getTrainRate() { return trainRate; }
    // Usually difficulty depends on the global timer, but if you have a 
    // separate difficultyLevel variable, add a getter for it too.
    // --- SETTERS (For Rewinding) ---
    public void setSpawnTimer(int time) { this.globalTimer = time; }
    
    public void setRoadrollerRate(int rate) { this.roadrollerRate = rate; }
    
    public void setTrainRate(int rate) { this.trainRate = rate; }

    // If you want one setter for everything difficulty-related:
    public void restoreState(int timer, int rRate, int tRate) {
        this.globalTimer = timer;
        this.roadrollerRate = rRate;
        this.trainRate = tRate;
    }
    
    public int getDifficultyTimer() { return levelUpTime; }
    
    public void setDifficultyTimer(int l) { this.levelUpTime = l; }

}
