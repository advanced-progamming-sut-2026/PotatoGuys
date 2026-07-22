package pvz.models.entities.plants.actions;

import java.util.List;

import pvz.models.entities.plants.Plant;
import pvz.models.entities.plants.data.PlantFoodExecutor;
import pvz.models.entities.plants.enums.PlantCategory;
import pvz.models.games.GameContext;

/**
 * Single-use behaviour for Mint plants (IDs 61-69, rule #5): rather than
 * inventing a bespoke buff payload, a Mint simply re-triggers every currently
 * planted member of its target family's <em>own</em> Plant-Food effect —
 * i.e. it mass-activates {@link Plant#triggerPlantFood} across the family.
 *
 * <p>This is the key design decision that keeps Mints from needing any new
 * data or Java code per family: {@link pvz.models.entities.plants.data.PlantFoodExecutor}
 * already knows how to apply every {@code PlantFoodKind}, so a Mint is just a
 * broadcaster. The Mint's own {@code category} field in the JSON names the
 * family it buffs (matching the source dataset, where a Mint is itself
 * catalogued under that family's category).
 */
public class FamilyBuffAction implements PlantAction {

    private boolean used;

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx) {
        return !used; // instant consumable — fires exactly once, on the tick it is planted
    }

    @Override
    public void execute(Plant plant, GameContext ctx) {
        used = true;
        int buffed = 0;
        for (Plant member : getFamilyMembers(plant, ctx)) {
            member.triggerPlantFood(ctx);
            buffed++;
        }
        ctx.log("[Action] " + plant.getSheet().getName() + " triggered Plant Food on " + buffed
                + " " + plant.getSheet().getCategory() + " plant(s).");
        PlantFoodExecutor.applyMintOwnUpgrades(plant, ctx); // e.g. "reset family cooldowns" flag
        plant.kill();
    }

    private List<Plant> getFamilyMembers(Plant mint, GameContext ctx) {
        PlantCategory family = mint.getSheet().getCategory();
        return ctx.getPlants().stream()
                .filter(p -> p.getSheet().getCategory() == family)
                .filter(p -> p != mint)
                .toList();
    }

    @Override
    public String getName() { return "FamilyBuff"; }
}
