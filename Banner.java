import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;
import java.util.ArrayList;

/**
 * Professional Scaled Banner System.
 * Every dimension and speed is now linked to GameConfig.SCALE.
 */

public class Banner extends Actor {

    // 0: entering 1: in the middle 2: exiting 
    private int state = 0;
    private int timer = 0;
    
    private GreenfootImage baseImage;
    
    // Control the speed - Scaled based on global SCALE
    private double speed = -12.0 * GameConfig.SCALE;
    
    private boolean playedSound = false;
    private BossConfig config;
    
    public Banner(BossConfig config){
        this.config = config;
        
        // Scaled base image size
        baseImage = new GreenfootImage(GameConfig.s(810), GameConfig.s(150));
        baseImage.setColor(config.bgColor);
        baseImage.fill();
        
        for (SpriteOverlay s: config.overlays){
            sprites.add(s);
        }
        
        // Initial height (150) must be base height
        render(150);
    }
    
    public static class SpriteOverlay{
        GreenfootImage image;
        int offsetX;
        int offsetY;
        
        public SpriteOverlay(String fileName, int w, int h, int x, int y){
            this.image = new GreenfootImage(fileName);
            // Scale the overlay sprites as they are loaded
            this.image.scale(GameConfig.s(w), GameConfig.s(h));
            // Scale the offsets
            this.offsetX = GameConfig.s(x);
            this.offsetY = GameConfig.s(y);
        }
    }

    private List<SpriteOverlay> sprites = new ArrayList<>();
    
    private void render (int currentHeight){
        render(currentHeight, 255);
    }

    private void render (int currentHeight, int alpha){
        // Scale the canvas buffer
        GreenfootImage canvas = new GreenfootImage(GameConfig.s(1000), GameConfig.s(400));
        canvas.setColor(new Color(0,0,0,0));
        canvas.fill();
        
        Color c = config.bgColor;
        
        // Scale background dimensions
        int bgH = Math.max(1, GameConfig.s(currentHeight));
        int bgW = GameConfig.s(900);
        int bgX = (canvas.getWidth() - bgW) / 2;
        int bgY = (canvas.getHeight() - bgH) / 2;
        
        canvas.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
        canvas.fillRect(bgX, bgY, bgW, bgH);

        // Set sprites - offsets are already scaled in the SpriteOverlay constructor
        for (SpriteOverlay s : sprites) {
            int drawX = canvas.getWidth()/2 - s.image.getWidth()/2 + s.offsetX;
            int drawY = canvas.getHeight()/2 - s.image.getHeight()/2 + s.offsetY;
            canvas.drawImage(s.image, drawX, drawY);
        }

        setImage(canvas);
    }

    public void act(){
        if(state == 0){
            slideIn();
        }
        else if(state == 1){
            hold();
        }
        else if(state == 2){
            slideOut();
        }
    }

    private void slideIn(){
        if (!playedSound){
            playRandomSound();
            playedSound = true;
        }
        
        // Scaled target X coordinate (400)
        int targetX = GameConfig.s(400);
        int dist = targetX - getX();
        
        if(Math.abs(dist) <= GameConfig.s(4)){
            setLocation(targetX, getY());
            state = 1;
        }
        else{
            int step = (int)(dist * 0.12);
            if(step == 0){
                step = (dist > 0) ? 1 : -1;
            }
            setLocation(getX() + step, getY());
        }
    }
    
    private void hold(){
        // Acceleration scaled by global SCALE
        speed += (0.4 * GameConfig.SCALE);
        setLocation(getX() + ((int)speed), getY());
        
        // Resizing logic - targets are scaled
        int startPoint = GameConfig.s(200);
        int d = getX() - startPoint;
        double ratio = d / (double)startPoint;
        
        // Scale the animated heights
        int newHeight = GameConfig.s(30) + (int)(GameConfig.s(120) * ratio);
        render(newHeight);

        if(speed >= 0){
            state = 2;
            speed = 0;
        }
    }

    private void slideOut(){
        // Increasing speed scaled by global SCALE
        speed += (1.0 * GameConfig.SCALE);
        setLocation(getX() + (int)speed, getY());
        
        // Resizing logic - targets are scaled
        int startPoint = GameConfig.s(200);
        int d = getX() - startPoint;
        double ratio = d / (double)startPoint;
        
        int newHeight = GameConfig.s(30) + (int)(GameConfig.s(120) * ratio);
        int alpha = (int)(255 * (1.0 - ratio/4.0));
        
        if (alpha < 0) alpha = 0;
        if (alpha > 255) alpha = 255;
        
        render(newHeight, alpha);
        
        // Removal check - coordinate scaled
        if (getX() > getWorld().getWidth() + GameConfig.s(600)) {
            getWorld().removeObject(this);
        }   
    }

    public void playRandomSound() {
        String soundsKey = config.soundsKey;
        AudioManager.playPool(soundsKey);
    }
}