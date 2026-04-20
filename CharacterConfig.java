import greenfoot.*;
import java.util.List;
import java.util.ArrayList;

public enum CharacterConfig {
    
    DIO(
        "Dio", 
        new String[]{"Idle", "Wry", "Dash", "Lose", "WalkLeft", "WalkRight", "Intro", "Scratch", "Roll", "High", "Teleport"},
        "Dash", 
        GameConfig.DIO_MOVE_SPEED,
        0.8 * GameConfig.SCALE, // Base Scale
        "dio_bgm",
        "dioLostVoices",
        BossConfig.DIO,
        new String[]{"Ability_StandPunch", "Ability_MadeInHeaven"} 
    );
    
    // To make a character, just add a comma after DIO's block
    // and write KIRA("Kira", new String[]{"Walk", "Die"...}, ...);

    public final String folderName;
    public final String[] animNames;
    public final String defaultAnim;
    public final int moveSpeed;
    public final double scale;
    public final String bgmKey;
    public final String deathSoundKey;
    public final BossConfig bossConfig;
    public final String[] abilityClassNames;

    private CharacterConfig(String folderName, String[] animNames, String defaultAnim, 
                           int moveSpeed, double scale, String bgmKey, 
                           String deathSoundKey, BossConfig bossConfig, String[] abilities) {
        this.folderName = folderName;
        this.animNames = animNames;
        this.defaultAnim = defaultAnim;
        this.moveSpeed = moveSpeed;
        this.scale = scale;
        this.bgmKey = bgmKey;
        this.deathSoundKey = deathSoundKey;
        this.bossConfig = bossConfig;
        this.abilityClassNames = abilities;
    }
}