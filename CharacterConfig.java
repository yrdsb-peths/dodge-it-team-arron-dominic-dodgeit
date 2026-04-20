import greenfoot.*;
import java.util.List;
import java.util.ArrayList;

public enum CharacterConfig {
    
    DIO(
        "Dio Brando",//Stage name user sees when picking charcters
        "Dio", // Folder name
        new String[]{"Idle", "Wry", "Dash", "Lose"}, // Folder names for animations
        "Dash", // Default animation
        GameConfig.DIO_MOVE_SPEED, // Speed
        0.8 * GameConfig.SCALE, // Scale
        "dio_bgm", // Music
        "dioLostVoices", // Death sound
        BossConfig.DIO, // Boss banner (null if none)
        new String[]{"Ability_StandPunch", "Ability_MadeInHeaven","Ability_Mandom"} // Abilities!
        //Ability to stop time is built in
    ),
    
    //Dio 2 is just a tester to test character selection menu
    Dio2(
        "Dio Brando",
        "Dio", // Folder name
        new String[]{"Idle", "Wry", "Dash", "Lose"}, // Folder names for animations
        "Dash", // Default animation
        GameConfig.DIO_MOVE_SPEED, // Speed
        0.8 * GameConfig.SCALE, // Scale
        "dio_bgm", // Music
        "dioLostVoices", // Death sound
        BossConfig.DIO, // Boss banner (null if none)
        new String[]{"Ability_StandPunch", "Ability_Mandom"} // Abilities!
        //Ability to stop time is built in
    );
    
    // To make a character, just add a comma after DIO's block
    // and write KIRA("Kira", new String[]{"Walk", "Die"...}, ...);

    public final String displayName; 
    public final String folderName;
    public final String[] animNames;
    public final String defaultAnim;
    public final int moveSpeed;
    public final double scale;
    public final String bgmKey;
    public final String deathSoundKey;
    public final BossConfig bossConfig;
    public final String[] abilityClassNames;

    private CharacterConfig(String displayName,String folderName, String[] animNames, String defaultAnim, 
                           int moveSpeed, double scale, String bgmKey, 
                           String deathSoundKey, BossConfig bossConfig, String[] abilities) {
        this.displayName = displayName; 
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