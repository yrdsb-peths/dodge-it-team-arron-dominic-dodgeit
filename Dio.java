/*
 * ─────────────────────────────────────────────────────────────────────────────
 * Dio.java  —  DIO-SPECIFIC BEHAVIOUR (THIN WRAPPER OVER GenericPlayer)
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A minimal subclass of GenericPlayer that hard-codes Dio's character config
 *   and adds Dio-specific behaviour during the time-stop pause state.
 *
 * Why does this class exist?
 *   PlayingState.enter() contains a special check:
 *     if (ACTIVE_CHARACTER == CharacterConfig.DIO) → spawn new Dio()
 *   This allows Dio to have the onPauseUpdate() banner behaviour without
 *   baking it into GenericPlayer.  All other characters use GenericPlayer
 *   directly, since they have no pause-specific behaviour (yet).
 *
 * The boss banner:
 *   When PausedState is active (W was pressed for time-stop), Dio's
 *   onPauseUpdate() spawns the sliding Banner actor once.  bannerSpawned
 *   is reset to false each PlayingState frame so the banner can play
 *   again on the next time-stop.
 *
 * Interacts with:
 *   GenericPlayer (parent), CharacterConfig.DIO, Banner, BossConfig.DIO
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class Dio extends GenericPlayer {

    /**
     * Creates Dio using CharacterConfig.DIO as his data source.
     * All animations, abilities, speed, and sounds are defined there.
     */
    public Dio() {
        super(CharacterConfig.DIO);
    }

    /**
     * Called by Player.act() once per frame while PausedState is active.
     * (Normal PlayingState frames call movementLogic/animationLogic instead.)
     *
     * Behaviour during time-stop:
     *   1. Spawns the boss intro Banner (once, tracked by bannerSpawned).
     *   2. Calls movementLogic() and animationLogic() so Dio plays his
     *      "Wry" animation and any active abilities continue updating.
     */
    @Override
    protected void onPauseUpdate(MyWorld world) {
        // Spawn the banner exactly once per pause session
        if (!bannerSpawned && CharacterConfig.DIO.bossConfig != null) {
            world.addObject(
                new Banner(CharacterConfig.DIO.bossConfig),
                GameConfig.s(1120),  // starts off-screen to the right
                GameConfig.s(200)    // vertical centre
            );
            bannerSpawned = true;
            setAnimation("Wry"); // Dio's dramatic WRYYY pose
        }

        // Keep abilities and animations running during the freeze
        movementLogic();
        animationLogic();
    }
}
