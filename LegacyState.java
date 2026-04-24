import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

public class LegacyState implements GameState {

    private List<Actor> ui = new ArrayList<>();

    @Override
    public void enter(MyWorld world) {
        int midX = world.getWidth() / 2;
        int midY = world.getHeight() / 2;

        // 1. DIM THE ENTIRE WORLD
        addUI(world, new FX_DimOverlay(world.getWidth(), world.getHeight()), midX, midY);

        // 2. THE EVALUATION PANEL (Darker and sleek)
        addUI(world, new UI_Panel(world.getWidth() - GameConfig.s(60), world.getHeight() - GameConfig.s(40), new Color(10, 10, 15, 250)), midX, midY);

        // 3. GATHER THE DATA FROM THE ARCHIVE
        int bestScore = SaveManager.getInt("all_time_high");
        int totalSeconds = SaveManager.getInt("total_playtime");
        String timeStr = String.format("%d mins, %d secs", totalSeconds / 60, totalSeconds % 60);
        
        String favChar = SaveManager.getFavoriteCharacter();

        // 4. FIND THE MOST ABUSED POWER
        String[] abilities = {
            "Ability_StandPunch", "Ability_MadeInHeaven", "Ability_StickyFingers", 
            "Ability_Mandom", "Ability_KingCrimson", "Ability_DarkSpell01", "Ability_DarkSpell02","Ability_TheWorld"
        };
        
        String topAbility = "None";
        int maxUses = 0;
        for (String a : abilities) {
            int uses = SaveManager.getInt("use_" + a);
            if (uses > maxUses) {
                maxUses = uses;
                topAbility = a;
            }
        }

        // 5. THE PERSONALITY VERDICT
        String verdict = "PURIST: You barely use abilities. Do you even have a Stand?";
        if (maxUses > 0) {
            switch(topAbility) {
                case "Ability_StandPunch":   verdict = "AGGRESSOR: You let your Stand do all the talking. No fear."; break;
                case "Ability_MadeInHeaven": verdict = "SPEED DEMON: You live life in the fast lane. Blink and you're dead."; break;
                case "Ability_StickyFingers": verdict = "COWARD: You spend half your life hiding underground. Classy."; break;
                case "Ability_Mandom":       verdict = "TIME LORD: You refuse to accept your own mistakes. Rewind again?"; break;
                case "Ability_KingCrimson":  verdict = "EMPEROR: You look at absolute danger and simply skip it."; break;
                case "Ability_DarkSpell01":  verdict = "VOID BRINGER: Why dodge cars when you can just delete them?"; break;
                case "Ability_DarkSpell02":  verdict = "CONTROL FREAK: You like your enemies frozen and helpless."; break;
                case "Ability_TheWorld":  verdict = "ZA WARUDO! You are Dio himself."; break;
            }
        }

        // 6. DRAW THE UI (Surgical Placement)
        int startY = GameConfig.s(60);
        int spacing = GameConfig.s(22);

        addUI(world, new UIText("--- CLASSIFIED STAND DOSSIER ---", GameConfig.s(22), Color.YELLOW), midX, startY);
        
        addUI(world, new UIText("ULTIMATE RECORD: " + bestScore, GameConfig.s(18), Color.WHITE), midX, startY + spacing * 2);
        addUI(world, new UIText("LIFETIME SURVIVED: " + timeStr, GameConfig.s(16), Color.WHITE), midX, startY + spacing * 3);
        addUI(world, new UIText("FAVORITE USER: " + favChar, GameConfig.s(16), Color.WHITE), midX, startY + spacing * 4);
        
        // Highlight the most used ability
        String abilityName = topAbility.replace("Ability_", "").replace("StandPunch", "The World");
        addUI(world, new UIText("MOST ABUSED POWER: " + abilityName + " (" + maxUses + " times)", GameConfig.s(16), Color.CYAN), midX, startY + spacing * 5);
        
        // The sassy personality verdict (Wrapped)
        UIText verdictText = new UIText("\"" + verdict + "\"", GameConfig.s(15), Color.LIGHT_GRAY, world.getWidth() - GameConfig.s(100));
        addUI(world, verdictText, midX, startY + spacing * 7);

        // --- 7. THE CALL TO ACTION (FIXED) ---
        addUI(world, new UIText("--- PROVE YOUR RESOLVE ---", GameConfig.s(18), Color.ORANGE), midX, startY + spacing * 9);
        
        // Split these into two separate actors so wrapping never breaks them
        addUI(world, new UIText("Screenshot this page and send your legacy to:", GameConfig.s(14), Color.WHITE), midX, startY + spacing * 10);
        
        // THE EMAIL - Given its own line and a bright color!
        UIText emailAddress = new UIText("440043978@gapps.yrdsb.ca", GameConfig.s(15), Color.YELLOW);
        addUI(world, emailAddress, midX, startY + spacing * 11);

        // Exit button - Lowered slightly to give the email room
        addUI(world, new UIText("[ ESC : RETURN TO MENU ]", GameConfig.s(18), Color.WHITE), midX, world.getHeight() - GameConfig.s(45));
    }

    @Override
    public void update(MyWorld world) {
        if ("escape".equals(Greenfoot.getKey())) {
            world.getGSM().popState(); 
        }
    }

    @Override public void exit(MyWorld world) { world.removeObjects(ui); }

    private void addUI(MyWorld world, Actor a, int x, int y) {
        world.addObject(a, x, y);
        ui.add(a);
    }
}