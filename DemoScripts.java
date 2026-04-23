import greenfoot.*;

public class DemoScripts {
    
    public static DemoStage getDemoFor(Class<? extends Ability> clazz) {
        
        if (clazz == Ability_StandPunch.class) {
            return new DemoStage("Your Stand destroys incoming obstacles for 3.5s.")
                .spawnRoadrollerAt(20, 1)
                .spawnRoadrollerAt(50, 2)
                .addWaitPoint(90, 
                    (player, world) -> player.isAbilityActive(Ability_StandPunch.class), 
                    "Press E to summon your Stand and destroy them!"
                );
        }
        
        if (clazz == Ability_MadeInHeaven.class) {
            return new DemoStage("Made In Heaven makes you move at extreme speeds.")
                .spawnRoadrollerAt(20, 0)
                .spawnRoadrollerAt(40, 1)
                .addWaitPoint(80, 
                    (player, world) -> player.isAbilityActive(Ability_MadeInHeaven.class), 
                    "Press S to accelerate time and dodge!"
                );
        }
        
        if (clazz == Ability_StickyFingers.class) {
            return new DemoStage("Sticky Fingers lets you hide and warp at edges.")
                .spawnRoadrollerAt(20, 2)
                .addWaitPoint(60, 
                    (player, world) -> player.isHidden(), 
                    "Press F to zip into the road and hide!"
                )
                .addWaitPoint(150, 
                    (player, world) -> !player.isHidden(), 
                    "The danger passed. Press F to surface with invincibility!"
                )
                .addWaitPoint(250, 
                    (player, world) -> player.getAbility(Ability_StickyFingers.class).getSecondaryCooldownPercent() > 0, 
                    "Walk into the top or bottom screen edge to Portal Warp!"
                );
        }
        
        if (clazz == Ability_Mandom.class) {
            return new DemoStage("Mandom rewinds time to fix mistakes. \nIf you've been here before, you have mastered this skill")
                .spawnRoadrollerAt(50, 1)
                .spawnRoadrollerAt(50, 2)
                .spawnRoadrollerAt(80, 0)
                .spawnRoadrollerAt(80, 2)
                .spawnRoadrollerAt(80, 1)
                .addWaitPoint(150, 
                    (player, world) -> world.isRewinding(), 
                    "You're trapped! Press R to rewind time!"
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
                    "An unavoidable wall! HOLD [RIGHT ARROW] to fast-forward into the future."
                );
                
                // NO SECOND WAIT POINT! 
                // Once they hold Right, the game unfreezes and lets them play it out.
                // If they let go of Right too early, they snap back to the past and die.
                // If they wait for the overlap and press Q, they shatter the cars and win!
        }
        
        // Fallback for missing/future abilities
        return new DemoStage("Try out the ability in the sandbox!");
    }
}