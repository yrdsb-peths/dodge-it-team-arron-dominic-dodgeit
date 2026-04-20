import greenfoot.*;

public class UI_Preview extends Actor {
    private Animator anim;

    public UI_Preview(CharacterConfig config) {
        // Load the character's default animation (usually Idle or Dash)
        this.anim = new Animator(config.folderName, config.defaultAnim, config.scale);
    }

    public void act() {
        // Just cycle the animation frames
        setImage(anim.getCurrentFrame());
    }
}