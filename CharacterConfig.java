import greenfoot.*;

public enum CharacterConfig {
    
    MoonKnight(
        "Moon Knight", "MoonKnight", "moon_knight_full.png", "white_road.png",
        new String[]{"Idle", "Dash", "Lose", "DarkSpell_01", "DarkSpell_02"}, "Dash",
        GameConfig.MOON_KNIGHT_MOVE_SPEED, 1.2 * GameConfig.SCALE,
        "gothic_bgm", "nightLostVoice", "nightEntryVoice2", BossConfig.DIO,
        new String[]{"Ability_DarkSpell01", "Ability_DarkSpell02", "Ability_StickyFingers"} 
    ),
    omnipotent_DIO(
        "Omnipotent Dio", "Dio", "omnipotent_dio_full.png", "standard_road.png",
        new String[]{"Idle", "Wry", "Dash", "Lose", "DarkSpell_01", "DarkSpell_02"}, "Dash",
        GameConfig.DIO_MOVE_SPEED, 0.8 * GameConfig.SCALE,
        "dio_bgm", "dioLostVoices", "dioBattleCry", BossConfig.DIO,
        new String[]{"Ability_TheWorld", "Ability_StandPunch", "Ability_MadeInHeaven", "Ability_Mandom", "Ability_StickyFingers", "Ability_DarkSpell01", "Ability_DarkSpell02", "Ability_KingCrimson"}
    ),
    DIO(
        "Dio Brando", "Dio", "dio_full.jpg", "punk_road.png",
        new String[]{"Idle", "Wry", "Dash", "Lose"}, "Dash",
        GameConfig.DIO_MOVE_SPEED, 0.8 * GameConfig.SCALE,
        "dio_bgm", "dioLostVoices", "dioBattleCry", BossConfig.DIO,
        new String[]{"Ability_TheWorld", "Ability_StandPunch", "Ability_MadeInHeaven"}
    ),
    Ringo(
        "Ringo Roadagain", "Ringo", "ringo_full.jpg", "standard_road.png",
        new String[]{"Idle", "Dash", "Wry", "Lose"}, "Dash",
        GameConfig.DIO_MOVE_SPEED, 1.5 * GameConfig.SCALE,
        "ringo_theme", "ringoLoseLines", "ringoLines", BossConfig.Ringo,
        new String[]{"Ability_Mandom", "Ability_TheWorld"}
    ),
    DIAVOLO(
        "Diavolo", "diavolo", "diavolo_full.jpg", "standard_road.png", 
        new String[]{"Idle", "Dash", "Lose"}, "Dash",
        GameConfig.DIO_MOVE_SPEED, 0.8 * GameConfig.SCALE,
        "diavolo_theme", "diavoloLoseLines", "diavoloLines", BossConfig.DIO, 
        new String[]{"Ability_KingCrimson"}
    ),
    CUSTOM(
        "Custom", "Dio", "dio_full.jpg", "standard_road.png",
        new String[]{"Idle", "Wry", "Dash", "Lose"}, "Dash",
        GameConfig.DIO_MOVE_SPEED, GameConfig.DIO_BASE_SCALE,
        "dio_bgm", "dioLostVoices", "dioBattleCry", BossConfig.DIO,
        new String[]{}
    );

    // Removed final so we can edit the CUSTOM enum before start
    public String displayName;
    public String folderName;
    public String portraitImage;  
    public String roadImage;
    public String[] animNames;
    public String defaultAnim;
    public int moveSpeed;
    public double scale;
    public String bgmKey;
    public String deathSoundKey;
    public String selectSoundKey;
    public BossConfig bossConfig;
    public String[] abilityClassNames;

    private CharacterConfig(
            String displayName, String folderName, String portraitImage, String roadImage,
            String[] animNames, String defaultAnim, int moveSpeed, double scale,
            String bgmKey, String deathSoundKey, String selectSoundKey,
            BossConfig bossConfig, String[] abilities) {
        this.displayName      = displayName;
        this.folderName       = folderName;
        this.portraitImage    = portraitImage;
        this.roadImage        = roadImage;
        this.animNames        = animNames;
        this.defaultAnim      = defaultAnim;
        this.moveSpeed        = moveSpeed;
        this.scale            = scale;
        this.bgmKey           = bgmKey;
        this.deathSoundKey    = deathSoundKey;
        this.selectSoundKey   = selectSoundKey;
        this.bossConfig       = bossConfig;
        this.abilityClassNames = abilities;
    }
}