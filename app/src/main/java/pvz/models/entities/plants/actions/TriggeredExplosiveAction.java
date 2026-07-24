package pvz.models.entities.plants.actions;

import pvz.models.entities.plants.Plant;
import pvz.models.entities.plants.data.DamageKind;
import pvz.models.entities.plants.enums.PlantTag;
import pvz.models.entities.plants.enums.PlantType;
import pvz.models.entities.zombies.Zombie;
import pvz.models.games.GameContext;

import java.util.ArrayList;

public class TriggeredExplosiveAction implements PlantAction {

    private static final float DEFAULT_ARM_SECONDS = 15f;

    private final int armTicks;
    private int elapsed;

    public TriggeredExplosiveAction(boolean isTrap) {
        float armSeconds = isTrap ? DEFAULT_ARM_SECONDS : 0f;
        this.armTicks = Math.max(0, Math.round(armSeconds * Plant.TICKS_PER_SECOND));
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx) {
        if (elapsed < armTicks) {
            elapsed++;
            return false;
        }
        boolean isTrap = plant.getSheet().hasTag(PlantTag.TRAP);
        return !isTrap || !ctx.getZombiesAt(plant.getCol(), plant.getLane()).isEmpty();
    }

    @Override
    public void execute(Plant plant, GameContext ctx) {
        boolean instaKill = plant.getSheet().getDamage().getKind() == DamageKind.INSTA_KILL;
        float dmg = instaKill ? Float.MAX_VALUE : plant.getEffectiveDamage();
        boolean isTrap = plant.getSheet().hasTag(PlantTag.TRAP);

        int hit = 0;

        if (isTrap) {
            for (Zombie z : ctx.getZombiesAt(plant.getCol(), plant.getLane())) {
                z.takeDamage(dmg, false);
                hit++;
                
                if (plant.getType() == PlantType.Squash) {
                }
                break; 
            }
        } else {
            PlantType type = plant.getType(); 
            int plantCol = plant.getCol();
            int plantLane = plant.getLane();

            for (Zombie z : new ArrayList<>(ctx.getZombies())) {
                boolean inRange = false;

                if (type == PlantType.Doomshroom) {
                    inRange = true; 
                } else if (type == PlantType.Jalapeno) {
                    inRange = (z.getLane() == plantLane); 
                    if (inRange) {
                        z.fire(); 
                    }
                } else {
                    int zCol = (int)z.getX();
                    int zLane = z.getLane();

                    inRange = Math.abs(zCol - plantCol) <= 1 && Math.abs(zLane - plantLane) <= 1;
                }

                if (inRange) {
                    z.takeDamage(dmg, false);
                    hit++;
                }
            }
        }

        ctx.log("[Action] " + plant.getSheet().getName() + " exploded, hitting " + hit + " zombie(s).");
        plant.kill();
    }

    @Override
    public String getName() {
        return "Explode";
    }
}