import greenfoot.*;

public class DemoScripts {
    
    public static DemoStage getDemoFor(Class<? extends Ability> clazz) {
        
        if (clazz == Ability_StandPunch.class) {
            return new DemoStage("Your Stand destroys incoming obstacles for 3.5s.")
                .spawnRoadrollerAt(20, 1)
                .spawnRoadrollerAt(50, 2)
                .addWaitPoint(90, 
                    (player, world) -> player.isAbilityActive(Ability_StandPunch.class), 
                    "Press [E] to summon your Stand and destroy them!"
                );
        }
        
        if (clazz == Ability_MadeInHeaven.class) {
            return new DemoStage("Made In Heaven makes you move at extreme speeds.")
                .spawnRoadrollerAt(20, 0)
                .spawnRoadrollerAt(40, 1)
                .addWaitPoint(80, 
                    (player, world) -> player.isAbilityActive(Ability_MadeInHeaven.class), 
                    "Press [S] to accelerate time and dodge!"
                );
        }
        
        if (clazz == Ability_StickyFingers.class) {
            return new DemoStage("Sticky Fingers lets you hide and warp at edges.")
                .spawnRoadrollerAt(20, 2)
                .addWaitPoint(60, 
                    (player, world) -> player.isHidden(), 
                    "Press[F] to zip into the road and hide!"
                )
                .addWaitPoint(150, 
                    (player, world) -> !player.isHidden(), 
                    "The danger passed. Press[F] to surface with invincibility!"
                )
                .addWaitPoint(250, 
                    (player, world) -> player.getAbility(Ability_StickyFingers.class).getSecondaryCooldownPercent() > 0, 
                    "Walk into the top or bottom screen edge to Portal Warp!"
                );
        }
        
        if (clazz == Ability_Mandom.class) {
            return new DemoStage("Mandom rewinds time to fix mistakes.")
                .spawnRoadrollerAt(50, 1)
                .spawnRoadrollerAt(50, 2)
                .spawnRoadrollerAt(80, 0)
                .spawnRoadrollerAt(80, 2)
                .spawnRoadrollerAt(80, 1)
                .addWaitPoint(150, 
                    (player, world) -> world.isRewinding(), 
                    "You're trapped! Press[R] to rewind time!"
                );
        }
        
        if (clazz == Ability_TheWorld.class) {
            return new DemoStage("The World: Total mastery over time. No limits.")
                .spawnRoadrollerAt(20, 2)
                .spawnRoadrollerAt(20, 3)
                .addWaitPoint(60, 
                    (player, world) -> player.isAbilityActive(Ability_TheWorld.class), 
                    "Press [W] to STOP TIME! Walk around the frozen cars safely!"
                )
                .addWaitPoint(120, 
                    (player, world) -> !player.isAbilityActive(Ability_TheWorld.class), 
                    "Whenever you are ready, press [W] again to RESUME time."
                );
        }
        
        if (clazz == Ability_KingCrimson.class) {
            return new DemoStage("King Crimson: HOLD [RIGHT] to see the future.\nPRESS [Q] to skip to the future you want!")
                // Spawn an inescapable wall across ALL 5 lanes
                .spawnRoadrollerAt(20, 0)
                .spawnRoadrollerAt(20, 1)
                .spawnRoadrollerAt(20, 2)
                .spawnRoadrollerAt(20, 3)
                .spawnRoadrollerAt(20, 4)
                
                // Freeze the game right before the wall hits
                .addWaitPoint(80, 
                    (player, world) -> player.isAbilityActive(Ability_KingCrimson.class), 
                    "HOLD [RIGHT ARROW] to fast-forward into the future, Q to confirm"
                );
                
                // NO SECOND WAIT POINT! 
                // Once they hold Right, the game unfreezes and lets them play it out.
                // If they let go of Right too early, they snap back to the past and die.
                // If they wait for the overlap and press Q, they shatter the cars and win!
        }

        if (clazz == Ability_DarkSpell01.class) {
            return new DemoStage("Dark Spell 1: Erases everything in a massive circle around you.")
                // --- PHASE 1: NORMAL MODE ---
                .spawnRoadrollerAt(20, 2)
                .addWaitPoint(60, 
                    (player, world) -> player.isAbilityActive(Ability_DarkSpell01.class), 
                    "Normal Mode: Press [V] to trigger the Void Nuke and clear the circle!"
                )
                
                // --- THE HACK: Wait until the cast FINISHES, then reset cooldown ---
                .addWaitPoint(130, // Changed from 100 to 130 to allow the 54-frame cast to finish!
                    (player, world) -> {
                        player.getAbility(Ability_DarkSpell01.class).cancel();
                        player.setAnimation("Dash"); // Safety catch to stop the loop!
                        return true; 
                    }, 
                    ""
                )
                
                // --- PHASE 3: RESONANCE COMBO SETUP ---
                .spawnRoadrollerAt(150, 2)
                .spawnRoadrollerAt(170, 2)
                .addWaitPoint(210, 
                    (player, world) -> player.isAbilityActive(Ability_DarkSpell02.class), 
                    "RESONANCE COMBO! First, line up with the cars and press [C] to freeze the lane."
                )
                
                // --- PHASE 4: BEAM EXECUTION ---
                .addWaitPoint(220, // Snappy transition!
                    (player, world) -> player.isAbilityActive(Ability_DarkSpell01.class), 
                    "Quick! While the lane is frozen, press [V] to fire the Annihilation Beam!"
                );
        }
        
        if (clazz == Ability_DarkSpell02.class) {
            return new DemoStage("Dark Spell 2: Curses your current lane, stopping all incoming traffic.")
                .spawnRoadrollerAt(30, 2) 
                .spawnRoadrollerAt(40, 2)
                .spawnRoadrollerAt(50, 2)
                .addWaitPoint(70, 
                    (player, world) -> player.isAbilityActive(Ability_DarkSpell02.class), 
                    "Line up with the car and press [C] to freeze the lane!"
                )
                .addWaitPoint(150,
                    (player, world) -> true, 
                    "Look! Incoming cars in this lane freeze instantly upon entry."
                );
        }
        
        return new DemoStage("Try out the ability in the sandbox!");
    }
}