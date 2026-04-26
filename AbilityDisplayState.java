import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

public class AbilityDisplayState implements GameState, IActiveGameState {

    private enum Mode { SELECTING, PLAYING }
    private Mode mode = Mode.SELECTING;

    private List<Actor> uiElements = new ArrayList<>();
    private List<Actor> hiddenActors = new ArrayList<>();
    
    private List<UI_AbilityIcon> icons = new ArrayList<>();
    private UI_HighlightBox highlightBox;
    private UIText titleText;
    private UIText descText;
    private UIText btnEsc;
    private UIText btnEnter;
    
    private int selectedIndex = 0;
    private GenericPlayer dummyPlayer; 
    
    // --- SANDBOX ENGINE ---
    private GenericPlayer activePlayer;
    private DemoStage currentStage;
    private SpawnManager demoSpawnManager;
    private Time_RewindManager rewindManager;
    private FX_RewindOverlay rewindOverlay;
    private FX_DimOverlay dimOverlay;
    private UI_RewindBar activeRewindBar; 
    private boolean isFrozen = false;
    private boolean playerDiedThisFrame = false;
    private UIText moneyDisplay;

    @Override
    public boolean isGameFrozen() { return isFrozen || Ability_KingCrimson.ERASING || Ability_TheWorld.TIME_STOPPED;  }

    @Override
    public void enter(MyWorld world) {   
        for (Actor a : world.getObjects(Actor.class)) {
            if (a.getImage() != null && a.getImage().getTransparency() > 0) {
                a.getImage().setTransparency(0);
                hiddenActors.add(a);
            }
        }

        int splitY = GameConfig.DEMO_BOTTOM_BOUND;
        int worldH = world.getHeight();
        int uiHeight = worldH - splitY;
        int midX = world.getWidth() / 2;
        
        addUI(world, new UI_Panel(world.getWidth(), uiHeight, new Color(30, 30, 30)), world.getWidth() / 2, splitY + (uiHeight / 2));

        if (GameConfig.ACTIVE_CHARACTER == CharacterConfig.DIO) dummyPlayer = new Dio();
        else dummyPlayer = new GenericPlayer(GameConfig.ACTIVE_CHARACTER);
        
        // --- FIX 1: Load EVERY ability directly from the character's config ---
        String[] allAbilityNames = GameConfig.ACTIVE_CHARACTER.abilityClassNames;
        int iconCount = allAbilityNames.length;
        
        int baseSpacing = GameConfig.s(70);
        if (iconCount > 4) baseSpacing = GameConfig.s(55); 
        
        int startX = world.getWidth() / 2 - ((iconCount - 1) * baseSpacing) / 2;
        int startY = splitY + GameConfig.s(85); 

        // Generate the icons
        for (int i = 0; i < iconCount; i++) {
            try {
                // Create a temporary ability instance just so the icon knows what to draw
                Ability tempAbility = (Ability) Class.forName(allAbilityNames[i]).getDeclaredConstructor().newInstance();
                UI_AbilityIcon icon = new UI_AbilityIcon(dummyPlayer, tempAbility);
                icons.add(icon);
                addUI(world, icon, startX + (i * baseSpacing), startY);
            } catch (Exception e) {
                System.out.println("Could not load icon for: " + allAbilityNames[i]);
            }
        }
        
        moneyDisplay = new UIText("MONEY: $" + SaveManager.getInt("money"), GameConfig.s(18), Color.YELLOW);
        addUI(world, moneyDisplay, world.getWidth() - GameConfig.s(100), splitY + GameConfig.s(18));
        
        if (!icons.isEmpty()) {
            highlightBox = new UI_HighlightBox(GameConfig.s(56));
            addUI(world, highlightBox, icons.get(0).getX(), icons.get(0).getY());
        }

        titleText = new UIText("", GameConfig.s(22), Color.YELLOW);
        descText  = new UIText("", GameConfig.s(16), Color.WHITE);
        
        addUI(world, titleText, world.getWidth() / 2, splitY + GameConfig.s(18));
        addUI(world, descText,  world.getWidth() / 2, splitY + GameConfig.s(45));
        
        btnEsc = new UIText("[ ESC : Back ]", GameConfig.s(18), Color.CYAN);
        btnEnter = new UIText("[ ENTER : Try Ability ]", GameConfig.s(18), Color.GREEN);
        addUI(world, btnEsc, GameConfig.s(90), worldH - GameConfig.s(20));
        addUI(world, btnEnter, world.getWidth() - GameConfig.s(85), worldH - GameConfig.s(20));

        updateMenuSelection();
    }

    @Override
    public void update(MyWorld world) {
        String key = Greenfoot.getKey();
        if (mode == Mode.SELECTING) handleSelectingMode(world, key);
        else if (mode == Mode.PLAYING) handlePlayingMode(world, key);
    }

    private void handleSelectingMode(MyWorld world, String key) {
        if ("escape".equals(key)) { world.getGSM().popState(); return; }
        if (icons.isEmpty()) return;
        
        // Use the icon to get the ability, NOT the dummy player
        Ability selectedAbility = icons.get(selectedIndex).getAbility();
        String shopKey = "ability_" + selectedAbility.getClass().getSimpleName();
    
        // --- START DEMO LOGIC ---
        if ("enter".equals(key)) { 
            if (ShopManager.isUnlocked(shopKey)) {
                startDemo(world); 
            } else {
                // Ability is locked! Tell the player or play an error sound
                AudioManager.playPool("error_buzzer"); 
            }
            return; 
        }
    
        if ("left".equals(key)) {
            selectedIndex = (selectedIndex - 1 + icons.size()) % icons.size();
            updateMenuSelection();
        } else if ("right".equals(key)) {
            selectedIndex = (selectedIndex + 1) % icons.size();
            updateMenuSelection();
        }
    }

    private void updateMenuSelection() {
        if (icons.isEmpty()) return;
        
        UI_AbilityIcon selectedIcon = icons.get(selectedIndex); 
        highlightBox.setLocation(selectedIcon.getX(), selectedIcon.getY());
        
        Ability ability = selectedIcon.getAbility(); // Read from the icon
        String abilityName = ability.getClass().getSimpleName();
        String shopKey = "ability_" + abilityName;
        
        boolean unlocked = ShopManager.isUnlocked(shopKey);
    
        // 1. Set Title and Description
        if (unlocked) {
            titleText.setText("Ability: " + abilityName.replace("Ability_", ""));
            titleText.setColor(Color.YELLOW);
            descText.setText(DemoScripts.getDemoFor(ability.getClass()).getDefaultText());
            btnEnter.setText("[ ENTER : Try Ability ]");
            btnEnter.setColor(Color.GREEN);
        } else {
            // BLACKED OUT TEXT
            titleText.setText("[LOCKED] " + abilityName.replace("Ability_", ""));
            titleText.setColor(Color.DARK_GRAY);
            descText.setText("Visit the SHOP in the Main Menu to unlock this skill.");
            btnEnter.setText("LOCKED");
            btnEnter.setColor(Color.RED);
        }
    
        // 2. Visual dimming of ALL icons on the screen based on ownership
        for (int i = 0; i < icons.size(); i++) {
            String currentKey = "ability_" + icons.get(i).getAbility().getClass().getSimpleName();
            boolean isOwned = ShopManager.isUnlocked(currentKey);
            // 255 = Normal, 50 = Very dark
            icons.get(i).getImage().setTransparency(isOwned ? 255 : 50);
        }
    }

    private void startDemo(MyWorld world) {
        mode = Mode.PLAYING;
        clearSandbox(world); 
        
        // Read from icon, NOT dummyPlayer
        Ability selectedAbility = icons.get(selectedIndex).getAbility();
        currentStage = DemoScripts.getDemoFor(selectedAbility.getClass());
        
        demoSpawnManager = new SpawnManager();
        demoSpawnManager.setSpawnTimer(0);
        rewindManager = new Time_RewindManager();
        
        world.addObject(new ScrollingRoad(), world.getWidth() / 2, world.getHeight() / 2);
        world.addObject(new ScrollingRoad(), world.getWidth() + world.getWidth() / 2, world.getHeight() / 2);
        
        if (GameConfig.ACTIVE_CHARACTER == CharacterConfig.DIO) activePlayer = new Dio();
        else activePlayer = new GenericPlayer(GameConfig.ACTIVE_CHARACTER);
        
        if (selectedAbility.getClass() == Ability_DarkSpell01.class) {
            activePlayer.setDemoAbilityFilter(Ability_DarkSpell01.class, Ability_DarkSpell02.class);
        } else {
            activePlayer.setDemoAbilityFilter(selectedAbility.getClass());
        }
        
        world.addObject(activePlayer, GameConfig.s(80), GameConfig.DEMO_BOTTOM_BOUND / 2);
        
        if (activePlayer.hasAbility(Ability_Mandom.class)) {
            activeRewindBar = new UI_RewindBar(rewindManager);
            world.addObject(activeRewindBar, world.getWidth() - GameConfig.s(100), GameConfig.s(20));
        }
        
        titleText.setText("");
        descText.setText(currentStage.getDefaultText());
        btnEnter.setText("[ R : Replay ]");
        titleText.setText("Press [H] for Detailed Guide");
        btnEsc.setText("[ ESC : Back to Menu ]");
    }

    private void handlePlayingMode(MyWorld world, String key) {
        if ("escape".equals(key)) { stopDemo(world); return; }
        
        if ("h".equals(key)) {
            Ability selected = icons.get(selectedIndex).getAbility(); // Read from icon
            setSandboxUIVisible(false);
            world.getGSM().pushState(new AbilityGuideState(selected.getClass(), this));
            return; 
        }

        if ("r".equals(key) && !rewindManager.isRewinding()) { startDemo(world); return; }
        if (playerDiedThisFrame) { playerDiedThisFrame = false; startDemo(world); return; }

       if (rewindManager.isRewinding()) {
            if (dimOverlay != null) {
                world.removeObject(dimOverlay);
                dimOverlay = null;
            }

            boolean stillGoing = rewindManager.rewindStep(world, demoSpawnManager,GameConfig.REWIND_SPEED);
            if (!stillGoing) {
                if (rewindOverlay != null && rewindOverlay.getWorld() != null) world.removeObject(rewindOverlay);
                rewindOverlay = null;
                Greenfoot.setSpeed(50);
                AudioManager.setAllSoundsPaused(false);
                activePlayer.startIFrame(1.0);
            }
        } else {
            if (!isFrozen) rewindManager.record(world, demoSpawnManager);
            
            int frame = demoSpawnManager.getSpawnTimer();
            String blockedPrompt = currentStage.evaluateWait(frame, activePlayer, world);
            
            if (blockedPrompt != null) {
                isFrozen = true;
                descText.setText(blockedPrompt);
                if (dimOverlay == null) {
                    dimOverlay = new FX_DimOverlay(world.getWidth(), GameConfig.DEMO_BOTTOM_BOUND);
                    world.addObject(dimOverlay, world.getWidth()/2, GameConfig.DEMO_BOTTOM_BOUND/2);
                }
            } else {
                if (isFrozen) demoSpawnManager.setSpawnTimer(demoSpawnManager.getSpawnTimer() + 1);
                isFrozen = false;
                
                if (currentStage.isComplete(frame)) {
                    isFrozen = true;
                    world.removeObjects(world.getObjects(Obstacles.class));
                    world.removeObjects(world.getObjects(PathWarning.class));
                    world.removeObjects(world.getObjects(Exclaimation.class));
                    titleText.setText("Demo Complete!");
                    descText.setText("You've mastered this ability. Press [R] to Replay or [ESC] to Exit.");
                } else {
                    isFrozen = false;
                    descText.setText(currentStage.getDefaultText());
                    if (dimOverlay != null) {
                        world.removeObject(dimOverlay);
                        dimOverlay = null;
                    }
                    currentStage.fireSpawns(frame, world);
                    demoSpawnManager.setSpawnTimer(frame + 1);
                }
            }
        }
    }

    private void stopDemo(MyWorld world) {
        mode = Mode.SELECTING;
        clearSandbox(world);
        updateMenuSelection();
        btnEsc.setText("[ ESC : Back ]");
    }

    private void clearSandbox(MyWorld world) {
        isFrozen = false;
        playerDiedThisFrame = false;
        AudioManager.stopAllAbilities();
        Ability_DarkSpell02.CURRENT_FROZEN_LANE_Y = -1;

        world.removeObjects(world.getObjects(ScrollingRoad.class));
        world.removeObjects(world.getObjects(Obstacles.class));
        world.removeObjects(world.getObjects(Player.class));
        world.removeObjects(world.getObjects(PathWarning.class));
        world.removeObjects(world.getObjects(Exclaimation.class));
        world.removeObjects(world.getObjects(TheWorldStand.class));
        world.removeObjects(world.getObjects(FX_DimOverlay.class));
        world.removeObjects(world.getObjects(FX_ZipperGround.class));
        world.removeObjects(world.getObjects(FX_Afterimage.class));
        if (activeRewindBar != null && activeRewindBar.getWorld() != null) world.removeObject(activeRewindBar);
        if (rewindOverlay != null && rewindOverlay.getWorld() != null) world.removeObject(rewindOverlay);
        dimOverlay = null;
        world.removeObjects(world.getObjects(FX_KingCrimsonOverlay.class));
        world.removeObjects(world.getObjects(FX_EpitaphRevert.class));
        rewindOverlay = null;
        activeRewindBar = null;
    }

    public void notifyPlayerDied() { playerDiedThisFrame = true; }
    @Override public boolean isRewinding() { return rewindManager != null && rewindManager.isRewinding(); }

    @Override
    public void triggerRewind(MyWorld world) {
        if (mode == Mode.PLAYING && rewindManager.canRewind()) {
            AudioManager.setAllSoundsPaused(true);
            AudioManager.playPool("rewind");
            rewindManager.startRewind();
            rewindOverlay = new FX_RewindOverlay();
            world.addObject(rewindOverlay, world.getWidth() / 2, GameConfig.DEMO_BOTTOM_BOUND / 2);
        }
    }

    @Override
    public void exit(MyWorld world) {
        world.removeObjects(uiElements);
        Ability_TheWorld.TIME_STOPPED = false;
        Ability_KingCrimson.ERASING   = false;
        clearSandbox(world);
        for (Actor a : hiddenActors) {
            if (a.getWorld() != null && a.getImage() != null) a.getImage().setTransparency(255);
        }
        Greenfoot.setSpeed(50);
    }

    private void addUI(MyWorld world, Actor a, int x, int y) {
        world.addObject(a, x, y);
        uiElements.add(a);
    }
    
    @Override public SpawnManager getSpawnManager() { return demoSpawnManager; }
    @Override public Time_RewindManager getRewindManager() { return rewindManager; }
    
    public void setSandboxUIVisible(boolean visible) {
        int alpha = visible ? 255 : 0;
        for (Actor a : uiElements) {
            if (a instanceof UIText || a instanceof UI_AbilityIcon || a instanceof UI_HighlightBox) {
                if (a.getImage() != null) a.getImage().setTransparency(alpha);
            }
        }
        if (activeRewindBar != null && activeRewindBar.getImage() != null) {
            activeRewindBar.getImage().setTransparency(alpha);
        }
    }
}