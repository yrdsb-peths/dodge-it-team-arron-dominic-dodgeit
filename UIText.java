import greenfoot.*;
import java.util.*;

public class UIText extends Actor {

    private String text;
    private int fontSize;
    private Color color;
    private int maxWidth;

    public UIText(String text, int fontSize, Color color, int maxWidth) {
        this.text = text;
        this.fontSize = fontSize;
        this.color = color;
        this.maxWidth = maxWidth;
        updateImage();
    }

    public UIText(String text, int fontSize, Color color) {
        this(text, fontSize, color, 0);
    }

    public void setText(String newText) {
        if (!newText.equals(this.text)) {
            this.text = newText;
            updateImage();
        }
    }
    
    // NEW METHOD
    public void setColor(Color newColor) {
        if (!this.color.equals(newColor)) {
            this.color = newColor;
            updateImage();
        }
    }

    private void updateImage() {
        if (maxWidth <= 0) {
            setImage(new GreenfootImage(text, fontSize, color, new Color(0, 0, 0, 0)));
            return;
        }

        List<String> lines = wrapText(text, fontSize, maxWidth);
        int lineSpacing = 2;
        int totalHeight = lines.size() * (fontSize + lineSpacing);
        
        GreenfootImage finalImg = new GreenfootImage(maxWidth, totalHeight);
        
        for (int i = 0; i < lines.size(); i++) {
            GreenfootImage lineImg = new GreenfootImage(lines.get(i), fontSize, color, new Color(0,0,0,0));
            int x = (maxWidth - lineImg.getWidth()) / 2;
            int y = i * (fontSize + lineSpacing);
            finalImg.drawImage(lineImg, x, y);
        }
        setImage(finalImg);
    }

    private List<String> wrapText(String text, int size, int width) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        String currentLine = "";

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            GreenfootImage measure = new GreenfootImage(testLine, size, Color.BLACK, Color.WHITE);
            if (measure.getWidth() > width) {
                lines.add(currentLine);
                currentLine = word;
            } else {
                currentLine = testLine;
            }
        }
        lines.add(currentLine);
        return lines;
    }
}