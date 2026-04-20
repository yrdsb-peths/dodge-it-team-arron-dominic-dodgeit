import greenfoot.*;

/**
 * Write a description of class BossConfig here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public enum BossConfig  
{
   DIO(
        Color.BLACK, 
        "dioBattleCry", 
        new Banner.SpriteOverlay[] {
            // file name, width, heigh, x offset, y offset
            new Banner.SpriteOverlay("dio_full.png", 150, 150, -200, 0),
            new Banner.SpriteOverlay("dio_label.png", 300, 100, 0, 0)
        }
    );
    
    public final Color bgColor;
    public final String soundsKey;
    public final Banner.SpriteOverlay[] overlays;

    private BossConfig(Color color, String soundsKey, Banner.SpriteOverlay[] overlays) {
        this.bgColor = color;
        this.soundsKey = soundsKey;
        this.overlays = overlays;
    }
}
