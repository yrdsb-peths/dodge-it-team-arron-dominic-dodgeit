import greenfoot.*;

public class Time_ActorMemento {
    public final Actor actor;    // reference to the actual object
    public final int x, y;
    public final Object customData; // actor-specific extras (speed, isDead, etc.)
    
    public Time_ActorMemento(Actor actor, int x, int y, Object customData) {
        this.actor = actor;
        this.x = x;
        this.y = y;
        this.customData = customData;
    }
}