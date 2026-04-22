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
    private UI_RewindBar activeRewindBar; // Tracked for cleanup
    private boolean isFrozen = false;
    private boolean playerDiedThisFrame = false;

    @Override
    public boolean isGameFrozen() { return isFrozen; }

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
        
        // FIX 1: Use getAllAbilities() so Mandom is included in the count
        List<Ability> abilities = dummyPlayer.getAllAbilities();
        int iconCount = abilities.size();
        
        // FIX 2: Dynamic Layout. If > 4 abilities, make icons smaller and spacing tighter
        int baseSpacing = GameConfig.s(70);
        if (iconCount > 4) baseSpacing = GameConfig.s(55); 
        
        int startX = world.getWidth() / 2 - ((iconCount - 1) * baseSpacing) / 2;
        int startY = splitY + GameConfig.s(85); 

        for (int i = 0; i < iconCount; i++) {
            UI_AbilityIcon icon = new UI_AbilityIcon(dummyPlayer, abilities.get(i));
            icons.add(icon);
            addUI(world, icon, startX + (i * baseSpacing), startY);
        }

        highlightBox = new UI_HighlightBox(GameConfig.s(56));
        addUI(world, highlightBox, icons.get(0).getX(), icons.get(0).getY());

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
        if ("enter".equals(key)) { startDemo(world); return; }

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
        
        Ability ability = dummyPlayer.getAllAbilities().get(selectedIndex);
        titleText.setText("Ability: " + ability.getClass().getSimpleName().replace("Ability_", ""));
        descText.setText(DemoScripts.getDemoFor(ability.getClass()).getDefaultText());
    }

    private void startDemo(MyWorld world) {
        mode = Mode.PLAYING;
        clearSandbox(world); 
        
        Ability selectedAbility = dummyPlayer.getAllAbilities().get(selectedIndex);
        currentStage = DemoScripts.getDemoFor(selectedAbility.getClass());
        
        demoSpawnManager = new SpawnManager();
        demoSpawnManager.setSpawnTimer(0);
        rewindManager = new Time_RewindManager();
        
        world.addObject(new ScrollingRoad(), world.getWidth() / 2, world.getHeight() / 2);
        world.addObject(new ScrollingRoad(), world.getWidth() + world.getWidth() / 2, world.getHeight() / 2);
        
        if (GameConfig.ACTIVE_CHARACTER == CharacterConfig.DIO) activePlayer = new Dio();
        else activePlayer = new GenericPlayer(GameConfig.ACTIVE_CHARACTER);
        
        //Get ability list
        activePlayer.setDemoAbilityFilter(selectedAbility.getClass());
        
        world.addObject(activePlayer, GameConfig.s(80), GameConfig.DEMO_BOTTOM_BOUND / 2);
        
        // FIX 3: Ensure Rewind Bar appears during the Mandom demo
        if (activePlayer.hasAbility(Ability_Mandom.class)) {
            activeRewindBar = new UI_RewindBar(rewindManager);
            world.addObject(activeRewindBar, world.getWidth() - GameConfig.s(100), GameConfig.s(20));
        }
        
        titleText.setText("");
        descText.setText(currentStage.getDefaultText());
        btnEnter.setText("[ R : Replay ]");
        btnEsc.setText("[ ESC : Back to Menu ]");
    }

    private void handlePlayingMode(MyWorld world, String key) {
        if ("escape".equals(key)) { stopDemo(world); return; }
        if ("r".equals(key) && !rewindManager.isRewinding()) { startDemo(world); return; }
        if (playerDiedThisFrame) { playerDiedThisFrame = false; startDemo(world); return; }

        // --- REWIND LOGIC ---
       if (rewindManager.isRewinding()) {
            
            // ADD THESE LINES TO REMOVE THE DIM OVERLAY:
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
            // CRUCIAL: Record history every frame, even when frozen, so Mandom works!
            if (!isFrozen) {
                rewindManager.record(world, demoSpawnManager);
            }
            
            int frame = demoSpawnManager.getSpawnTimer();
            
            // Check for Wait Points
            String blockedPrompt = currentStage.evaluateWait(frame, activePlayer, world);
            
            if (blockedPrompt != null) {
                isFrozen = true;
                descText.setText(blockedPrompt);
                if (dimOverlay == null) {
                    dimOverlay = new FX_DimOverlay(world.getWidth(), GameConfig.DEMO_BOTTOM_BOUND);
                    world.addObject(dimOverlay, world.getWidth()/2, GameConfig.DEMO_BOTTOM_BOUND/2);
                }
            } else {
                if (isFrozen) {
                    demoSpawnManager.setSpawnTimer(demoSpawnManager.getSpawnTimer() + 1);
                }

                isFrozen = false;
                // Not blocked by a wait point
                if (currentStage.isComplete(frame)) {
                    isFrozen = true;
                    // Clear the road for the "Finish" screen
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
                    demoSpawnManager.setSpawnTimer(frame + 1); // Advance timeline
                }
            }
        }
    }

    private void stopDemo(MyWorld world) {
        mode = Mode.SELECTING;
        clearSandbox(world);
        updateMenuSelection();
        btnEnter.setText("[ ENTER : Try Ability ]");
        btnEsc.setText("[ ESC : Back ]");
    }

    private void clearSandbox(MyWorld world) {
        isFrozen = false;
        playerDiedThisFrame = false;
        AudioManager.stopAllPools();
        AudioManager.stopAllAbilities();
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
            // COVER ONLY THE SANDBOX AREA
            world.addObject(rewindOverlay, world.getWidth() / 2, GameConfig.DEMO_BOTTOM_BOUND / 2);
        }
    }

    @Override
    public void exit(MyWorld world) {
        world.removeObjects(uiElements);
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
}