package pvz.Models.Entities.Plants.actions;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantContext;
import pvz.Models.Entities.Plants.data.PlantFoodExecutor;

/**
 * Single-use behaviour for Mint plants (IDs 61-69, rule #5): rather than
 * inventing a bespoke buff payload, a Mint simply re-triggers every currently
 * planted member of its target family's <em>own</em> Plant-Food effect —
 * i.e. it mass-activates {@link Plant#triggerPlantFood} across the family.
 *
 * <p>This is the key design decision that keeps Mints from needing any new
 * data or Java code per family: {@link pvz.Models.Entities.Plants.data.PlantFoodExecutor}
 * already knows how to apply every {@code PlantFoodKind}, so a Mint is just a
 * broadcaster. The Mint's own {@code category} field in the JSON names the
 * family it buffs (matching the source dataset, where a Mint is itself
 * catalogued under that family's category).
 */
public class FamilyBuffAction implements PlantAction {

    private boolean used;

    @Override
    public boolean shouldTrigger(Plant plant, PlantContext ctx) {
        return !used; // instant consumable — fires exactly once, on the tick it is planted
    }

    @Override
    public void execute(Plant plant, PlantContext ctx) {
        used = true;
        int buffed = 0;
        for (Plant member : ctx.getActivePlantsInFamily(plant.getSheet().getCategory())) {
            if (member == plant) continue; // don't buff the mint itself
            member.triggerPlantFood(ctx);
            buffed++;
        }
        ctx.log("[Action] " + plant.getSheet().getName() + " triggered Plant Food on " + buffed
                + " " + plant.getSheet().getCategory() + " plant(s).");
        PlantFoodExecutor.applyMintOwnUpgrades(plant); // e.g. "reset family cooldowns" flag
        plant.kill();
    }

    @Override
    public String getName() { return "FamilyBuff"; }
}
