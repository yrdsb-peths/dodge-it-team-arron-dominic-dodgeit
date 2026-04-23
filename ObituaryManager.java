import greenfoot.*;
import java.util.*;
import java.io.*;

public class ObituaryManager {
    private static Map<CharacterConfig, List<String>> obituaryCache = new HashMap<>();
    
    // The name of the sub-folder in your project directory
    private static final String FOLDER_NAME = "obituaries";

    public static String getRandomObituary(CharacterConfig config) {
        if (!obituaryCache.containsKey(config)) {
            loadObituaries(config);
        }

        List<String> lines = obituaryCache.get(config);
        if (lines == null || lines.isEmpty()) {
            return "Fate is cruel. (Check your obituaries folder!)";
        }

        return lines.get(Greenfoot.getRandomNumber(lines.size()));
    }

    private static void loadObituaries(CharacterConfig config) {
        List<String> lines = new ArrayList<>();
        
        // Path logic: "obituaries/obituaries_DIO.txt"
         String fileName = "obituaries/obituaries_" + config.name() + ".txt";
        
        // Use Greenfoot's way of finding the project directory via standard Java File
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                System.out.println("MISSING FILE: Create " + fileName);
                lines.add("A silent end for " + config.displayName);
            } else {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        lines.add(line.trim());
                    }
                }
                br.close();
            }
        } catch (IOException e) {
            System.out.println("Error reading " + fileName);
            lines.add("The words escaped him in the end.");
        }
        
        obituaryCache.put(config, lines);
    }
}