import greenfoot.*;

public class UIText extends Actor {
    private String text;
    private int fontSize;
    private Color color;

    public UIText(String text, int fontSize, Color color) {
        this.text = text;
        this.fontSize = fontSize;
        this.color = color;
        updateImage();
    }

    public void setText(String newText) {
        if (!newText.equals(this.text)) {
            this.text = newText;
            updateImage();
        }
    }

    private void updateImage() {
        // Professional tip: Use a transparent background for UI
        GreenfootImage img = new GreenfootImage(text, fontSize, color, new Color(0,0,0,0));
        setImage(img);
    }
}