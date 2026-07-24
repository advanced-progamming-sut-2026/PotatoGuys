package pvz.models.entities.plants.actions;

import java.util.List;

import pvz.models.entities.plants.Plant;
import pvz.models.entities.plants.data.PlantFoodExecutor;
import pvz.models.entities.plants.enums.PlantCategory;
import pvz.models.games.GameContext;

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
