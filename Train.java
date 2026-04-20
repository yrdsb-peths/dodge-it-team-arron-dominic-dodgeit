import greenfoot.*;

public class Train extends Obstacles implements Time_Snapshottable {
    private int state = 0; // 0: Waiting/Shaking, 1: Rushing
    private int waitTimer = 65; // Slightly longer than the PathWarning
    private int speed;
    
    public Train(int speed) {
        
        GreenfootImage img = new GreenfootImage("obstacles/ambulence.png");
        setImage(img);
        getImage().scale(GameConfig.TRAIN_WIDTH, GameConfig.TRAIN_WIDTH);
        this.speed = speed;
        /*
        // DRAW THE CAR (No assets needed!)
        GreenfootImage img = new GreenfootImage(120, 40);
        
        // The "Body" - a dark sleek rectangle
        img.setColor(new Color(30, 30, 30));
        img.fill();
        
        // Add "Headlight" glows for a cool effect
        img.setColor(Color.YELLOW);
        img.fillRect(0, 5, 10, 10);
        img.fillRect(0, 25, 10, 10);
        
        // Add a "Trail" or "Wind" lines
        img.setColor(new Color(255, 255, 255, 100));
        img.drawLine(120, 10, 80, 10);
        img.drawLine(120, 30, 80, 30);
        
        setImage(img);
        */
    }

    protected void movementLogic() {
        if (state == 0) {
            // STATE 0: The "Revving" engine feel
            waitTimer--;
            // Shaking vertically to show it's about to zoom
            setLocation(getX(), getY());
            
            if (waitTimer <= 0) {
                state = 1;
                // AudioManager.play("car_zoom"); 
            }
        } else {
            // STATE 1: THE RUSH
            move(-speed);
        }
    }

    protected void collisionLogic() {
        if (state == 0) return; // Can't hit player while waiting
        
        Player p = (Player) getOneIntersectingObject(Player.class);
        if (p != null && !p.isDead()) {
            // At 25 speed, the collision needs to be tight
            if (p.checkCustomHitbox(this, 1)) {
                AudioManager.play("car_crash");
                p.die();
            }
        }
    }

    protected void checkRemove() {
        if (getX() < -100) {
            ScoreManager.addScore(5); // Big points for dodging the fast one
            getWorld().removeObject(this);
        }
    }
    
    
    //Time Machine stuff for time rewinding
    
    //Stored custom data for train
    private static class TrainData {
        int speed, state, waitTimer;
        TrainData(int speed, int state, int waitTimer) {
            this.speed = speed;
            this.state = state;
            this.waitTimer = waitTimer;
        }
    }
    
    //Captures current state and stores it as a momento
    public Time_ActorMemento captureState() {
        return new Time_ActorMemento(this, getX(), getY(),
                                     new TrainData(speed, state, waitTimer));
    }
    
    //Restores previous state according to the momento
    public void restoreState(Time_ActorMemento m) {
        TrainData d = (TrainData) m.customData;
        this.speed = d.speed;
        this.state = d.state;
        this.waitTimer = d.waitTimer;
    }
    

}