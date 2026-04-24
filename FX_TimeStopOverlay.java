import greenfoot.*;

public class FX_TimeStopOverlay extends Actor {
    public FX_TimeStopOverlay() {
        GreenfootImage img = new GreenfootImage(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        // Darken the screen slightly
        img.setColor(new Color(0, 0, 0, 120)); 
        img.fill();
        
        // Add text
        GreenfootImage text = new GreenfootImage("TIME STOPPED", GameConfig.s(40), Color.RED, new Color(0,0,0,0));
        img.drawImage(text, (img.getWidth() - text.getWidth()) / 2, GameConfig.s(100));
        
        setImage(img);
    }
    
    @Override
    public void act() {
        // Automatically remove itself when time resumes
        if (!Ability_TheWorld.TIME_STOPPED) {
            if (getWorld() != null) getWorld().removeObject(this);
        }
    }
}