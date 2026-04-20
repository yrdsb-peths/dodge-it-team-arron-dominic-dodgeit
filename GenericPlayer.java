import greenfoot.*;
import java.util.*;

public class GenericPlayer extends Player implements Time_Snapshottable {
    private CharacterConfig config;
    private HashMap<String, Animator> animations = new HashMap<>();
    private Animator currentAnimator;
    private String currentAnimName = "";
    private List<Ability> abilities = new ArrayList<>();
    
    private GameTimer deathTimer = new GameTimer(4.0, false);
    private GameTimer iFrameTimer = new GameTimer(0.5, false);
    private int dieX, dieY;

    public GenericPlayer(CharacterConfig config) {
        this.config = config;
        
        // 1. Load Animations from Config
        for (String name : config.animNames) {
            animations.put(name, new Animator(config.folderName, name, config.scale));
        }
        setAnimation(config.defaultAnim);

        // 2. Load Abilities via Reflection (Very scalable!)
        for (String className : config.abilityClassNames) {
            try {
                abilities.add((Ability) Class.forName(className).getDeclaredConstructor().newInstance());
            } catch (Exception e) { System.out.println("Failed to load ability: " + className); }
        }
    }

    public void setAnimation(String name) {
        if (isDead && !name.equals("Lose")) return;
        if (!currentAnimName.equals(name) && animations.containsKey(name)) {
            currentAnimName = name;
            currentAnimator = animations.get(name);
            currentAnimator.reset();
        }
    }

    protected void movementLogic() {
        iFrameTimer.update((MyWorld)getWorld());

        // Update and Trigger Abilities
        for (Ability a : abilities) {
            a.update(this, (MyWorld)getWorld());
            if (Greenfoot.isKeyDown(a.getKeybind())) {
                a.activate(this, (MyWorld)getWorld());
            }
        }

        if (isDead) {
            deathTimer.update((MyWorld)getWorld());
            if (!deathTimer.isExpired()) shake();
            else if (!((MyWorld)getWorld()).isRewinding()) {
                ((MyWorld)getWorld()).getGSM().changeState(new GameOverState());
            }
        } else {
            handleStandardMovement();
        }
    }

    private void handleStandardMovement() {
        int speed = config.moveSpeed;
        // Check for speed modifiers from abilities (like MIH)
        for(Ability a : abilities) if(a instanceof Ability_MadeInHeaven && a.isActive()) speed *= 2;

        int nextX = getX(), nextY = getY();
        if (Greenfoot.isKeyDown("up")){
            nextY -= speed;
        }
        
        if (Greenfoot.isKeyDown("down")){
            nextY += speed;
        }
        
        int padding = GameConfig.s(30); 
        
        nextX = Math.max(padding, Math.min(getWorld().getWidth() - padding, nextX));
        nextY = Math.max(padding, Math.min(getWorld().getHeight() - padding, nextY));
    
        setLocation(nextX, nextY);
    }

    public void die() {
        if (iFrameTimer.isActive() || isDead) return;
        for (Ability a : abilities) if (a.isActive() && a instanceof Ability_StandPunch) return; // Invincible during punch
        
        isDead = true;
        setAnimation("Lose");
        AudioManager.playPool(config.deathSoundKey);
        dieX = getX(); dieY = getY();
        deathTimer.start();
    }

    protected void animationLogic() {
        if (getWorld() == null) return;
        setImage(currentAnimator.getCurrentFrame());

        if (iFrameTimer.isActive() && !iFrameTimer.isExpired()) {
            if (iFrameTimer.getRemainingFrames() % 4 < 2) {
                getImage().setTransparency(60); 
            } else {
                getImage().setTransparency(255); 
            }
        } else {
            getImage().setTransparency(255);
        }
    }

    // --- Time Machine Implementation ---
    private static class PlayerMemento {
        boolean isDead;
        String anim;
        int deathFrames;
        int iFrameFrames;
        List<Object> abilityStates = new ArrayList<>();
    }

    public Time_ActorMemento captureState() {
        PlayerMemento m = new PlayerMemento();
        m.isDead = isDead; 
        m.anim = currentAnimName; 
        m.deathFrames = deathTimer.getRemainingFrames();
        m.iFrameFrames = iFrameTimer.getRemainingFrames();
        for (Ability a : abilities) m.abilityStates.add(a.captureState());
        return new Time_ActorMemento(this, getX(), getY(), m);
    }

    public void restoreState(Time_ActorMemento m) {
        PlayerMemento data = (PlayerMemento) m.customData;
        this.isDead = data.isDead;
        setAnimation(data.anim);
        deathTimer.setRemainingFrames(data.deathFrames);
        iFrameTimer.setRemainingFrames(data.iFrameFrames);
        if (data.iFrameFrames > 0) iFrameTimer.start(); else iFrameTimer.stop();
        
        for (int i = 0; i < abilities.size(); i++) {
            abilities.get(i).restoreState(data.abilityStates.get(i));
        }
    }
        
    //Get ability of the character
    public Ability getPrimaryAbility() {
        if (!abilities.isEmpty()) return abilities.get(0);
        return null;
    }

    // Hitbox Helper
    public boolean checkCustomHitbox(Actor attacker, double padding) {
        double dist = Math.hypot(getX() - attacker.getX(), getY() - attacker.getY());
        return dist < (getImage().getWidth() * 0.5 * padding);
    }

    private void shake() {
        setLocation(dieX + Greenfoot.getRandomNumber(5) - 2, dieY + Greenfoot.getRandomNumber(5) - 2);
    }
    
    public void startIFrame(double sec) { iFrameTimer.setDuration(sec); iFrameTimer.start(); }
    
    public void setAnimation(String name, int speed) {
        if (isDead && !name.equals("Lose")) return;
        if (animations.containsKey(name)) {
            animations.get(name).setSpeed(speed); 
            setAnimation(name);                   
        }
    }
    
}