import greenfoot.*;

public class GameWorld extends World {
    
    private GreenfootSound gameMusic;
    
    public GameWorld() {
        super(800, 400, 1);
        
        // add BGM when the game starts
        gameMusic = new GreenfootSound("game-music.mp3");
        gameMusic.playLoop();
        
        prepare();
    }
    
    private void prepare() 
    {
        HeroMaybe hero = new HeroMaybe();
        addObject (hero, 300, 200);
    }
    
    public void stopMusic()
    {
        gameMusic.stop();
    }
}
