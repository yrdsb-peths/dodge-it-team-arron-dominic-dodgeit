
import greenfoot.*;
import java.util.List;

public class TheWorldStand extends Actor implements Time_Snapshottable {
    private Animator punchAnim;
    private Ability_StandPunch ability;

    public TheWorldStand(Ability_StandPunch ability) {
        punchAnim = new Animator("Dio", "WorldPunch", ability.standAnimSpeed, GameConfig.DIO_BASE_SCALE);
        this.ability = ability;
        AudioManager.play("summon_stand");
    }

    public void act() {
        MyWorld world = (MyWorld) getWorld();
        if (world == null) return;

        // --- FIX: Check for removal FIRST ---
        // If the ability is no longer active, remove the Stand immediately.
        // This must happen even if the game is paused or rewinding!
        if (!ability.isActive()) {
            world.removeObject(this);
            return;
        }

        // Now do the safety checks for movement/animation
        if (!world.getGSM().isState(PlayingState.class) || world.isRewinding()) return;

        setImage(punchAnim.getCurrentFrame());

        // Follow Dio
        List<Dio> dios = world.getObjects(Dio.class);
        if (!dios.isEmpty()) {
            Dio dio = dios.get(0);
            // If Dio is dead, the Stand should probably stop following or vanish
            if (dio.isDead()) {
                world.removeObject(this);
                return;
            }
            setLocation(dio.getX() + GameConfig.s(80), dio.getY());
        }

        // Destroy obstacles
        List<Obstacles> hitObstacles = getIntersectingObjects(Obstacles.class);
        for (Obstacles obs : hitObstacles) {
            //world.addObject(new Exclaimation(), obs.getX(), obs.getY());
            world.removeObject(obs);
            ScoreManager.addScore(2);
        }
    }

    public Time_ActorMemento captureState() {
        return new Time_ActorMemento(this, getX(), getY(), null);
    }

    public void restoreState(Time_ActorMemento m) { }
}