import greenfoot.*;


public class ScrollingRoad extends Actor implements Time_Snapshottable {
    private static int speed = GameConfig.ROAD_SCROLL_SPEED;//Moves 5 pixels per frame
    private static int maxSpeed = GameConfig.ROAD_MAX_SPEED;
    private int width = GameConfig.WORLD_WIDTH;
    // THE LANE MAP: Other classes (like SpawnManager) can use this!
    // For a 400px height with 5 lanes, centers are: 40, 120, 200, 280, 360

    public ScrollingRoad() {
        speed = GameConfig.ROAD_SCROLL_SPEED;
        drawPlaceholderRoad();
    }

    private void drawPlaceholderRoad() {
        GreenfootImage img = new GreenfootImage(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        
        // 1. Draw Asphalt
        img.setColor(new Color(50, 50, 50));
        img.fill();
        
        // 2. Draw Lane Lines
        img.setColor(Color.YELLOW);
        for (int i = 1; i < GameConfig.LANES.length; i++) {
            //Lane locations are stored in game config
            int lineY = (GameConfig.LANES[i] + GameConfig.LANES[i-1]) / 2;
            // Draw dashed lines(they are scaled by game config too)
            for (int x = 0; x < width; x += GameConfig.s(40)) {
                img.fillRect(x, lineY - 2, GameConfig.s(20), GameConfig.s(4));
            }
        }
        setImage(img);
    }

    public void act() {
        // Self-management: Only move if the game is actually "playing"
        MyWorld world = (MyWorld) getWorld();
        // ONLY scroll forward if we are in PlayingState AND NOT rewinding
        if (world.getGSM().isState(PlayingState.class) && !world.isRewinding()) {
            scroll();
        }
    }

    private void scroll() {
        setLocation(getX() - speed, getY());
        
        // If we moved off screen, teleport to the back of the "twin"
        if (getX() <= -width / 2) {
            setLocation(getX() + width * 2, getY());
        }
    }
    
    public static void increaseSpeed(int amount){
        if (speed < maxSpeed)speed+=amount;
    }
    // --- TIME MACHINE ---
    public Time_ActorMemento captureState() {
        // We just save position. No custom data needed (null).
        return new Time_ActorMemento(this, getX(), getY(), null);
    }

    public void restoreState(Time_ActorMemento m) {
        // Manager handles position automatically. Nothing else to restore!
    }
}