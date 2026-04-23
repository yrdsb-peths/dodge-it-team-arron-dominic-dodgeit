import greenfoot.*;
import java.util.*;

public class UIText extends Actor {

    private String text;
    private int fontSize;
    private Color color;
    private int maxWidth; // New: determines when to skip to the next line

    /**
     * @param maxWidth Pass 0 for a single line, or a pixel value (like 400) to wrap text.
     */
    public UIText(String text, int fontSize, Color color, int maxWidth) {
        this.text = text;
        this.fontSize = fontSize;
        this.color = color;
        this.maxWidth = maxWidth;
        updateImage();
    }

    // Keep the old constructor working for simple things like "SCORE"
    public UIText(String text, int fontSize, Color color) {
        this(text, fontSize, color, 0);
    }

    public void setText(String newText) {
        if (!newText.equals(this.text)) {
            this.text = newText;
            updateImage();
        }
    }

    private void updateImage() {
        if (maxWidth <= 0) {
            // Standard single-line behavior
            setImage(new GreenfootImage(text, fontSize, color, new Color(0, 0, 0, 0)));
            return;
        }

        // --- SMART WRAP LOGIC ---
        List<String> lines = wrapText(text, fontSize, maxWidth);
        int lineSpacing = 2;
        int totalHeight = lines.size() * (fontSize + lineSpacing);
        
        GreenfootImage finalImg = new GreenfootImage(maxWidth, totalHeight);
        
        for (int i = 0; i < lines.size(); i++) {
            GreenfootImage lineImg = new GreenfootImage(lines.get(i), fontSize, color, new Color(0,0,0,0));
            // Center the text horizontally within the maxWidth
            int x = (maxWidth - lineImg.getWidth()) / 2;
            int y = i * (fontSize + lineSpacing);
            finalImg.drawImage(lineImg, x, y);
        }
        setImage(finalImg);
    }

    /** Splits a long string into lines that fit the maxWidth */
    private List<String> wrapText(String text, int size, int width) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        String currentLine = "";

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            // Create a temp image to measure width
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