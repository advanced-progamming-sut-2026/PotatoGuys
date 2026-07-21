package pvz.models.entities.plants.actions;

import pvz.models.entities.plants.Plant;
import pvz.models.entities.plants.data.DamageKind;
import pvz.models.entities.plants.enums.PlantTag;
import pvz.models.entities.zombies.Zombie;
import pvz.models.games.GameContext;

/**
 * Single-use behaviour for {@code EXPLOSIVE} plants (rule: "Action Interval"
 * is {@code "-"} for this whole category — they are consumables, not
 * repeating attackers).
 *
 * <p>Two dataset patterns are unified here:
 * <ul>
 *   <li><b>Contact mines</b> ({@code TRAP} tag: Potato Mine, Squash, Tangle
 *       Kelp, Iceberg Lettuce) — arm after {@code armSeconds}, then detonate
 *       the instant a zombie steps on their cell.</li>
 *   <li><b>Instant blasts</b> (no {@code TRAP} tag: Cherry Bomb, Jalapeno,
 *       Grapeshot, Doom-shroom, Ice-shroom) — detonate immediately, hitting
 *       every zombie in range.</li>
 * </ul>
 * Either way the plant self-destructs immediately after firing once.
 */
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
        return !isTrap || !ctx.getZombiesAt(plant.getCol() , plant.getLane()).isEmpty();
    }

    @Override
    public void execute(Plant plant, GameContext ctx) {
        boolean instaKill = plant.getSheet().getDamage().getKind() == DamageKind.INSTA_KILL;
        float dmg = instaKill ? Float.MAX_VALUE : plant.getEffectiveDamage();
        boolean wholeBoard = !plant.getSheet().hasTag(PlantTag.TRAP);

        int hit = 0;
        if (wholeBoard) {
            for (int lane = 0; lane < ctx.getMap().getRows(); lane++) {
                for (Zombie z : ctx.getZombiesInLane(lane)) {
                    z.takeDamage(dmg, false);
                    hit++;
                }
            }
        } else {
            for (Zombie z : ctx.getZombiesAt(plant.getCol(), plant.getLane())) {
                z.takeDamage(dmg, false);
                hit++;
                break; // a trap only takes out the zombie that triggered it
            }
        }
        ctx.log("[Action] " + plant.getSheet().getName() + " exploded, hitting " + hit + " zombie(s).");
        plant.kill();
    }

    @Override
    public String getName() { return "Explode"; }
}
