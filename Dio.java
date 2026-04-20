import greenfoot.*;

public class Dio extends GenericPlayer {
    public Dio() {
        super(CharacterConfig.DIO);
    }
    
    //should be speicific to dio
    protected void onPauseUpdate(MyWorld world) {
        if (!bannerSpawned && CharacterConfig.DIO.bossConfig != null) {
            world.addObject(new Banner(CharacterConfig.DIO.bossConfig), GameConfig.s(1120), GameConfig.s(200));
            bannerSpawned = true;
            setAnimation("Wry"); // Or whichever intro animation fits
        }
        movementLogic(); 
        animationLogic();
    }
    
}

