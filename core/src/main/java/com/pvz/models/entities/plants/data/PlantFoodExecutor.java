package com.pvz.models.entities.plants.data;

import java.util.ArrayList;
import java.util.List;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.CooldownPlantAction;
import com.pvz.models.entities.plants.enums.PlantCategory;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

public final class PlantFoodExecutor {

    private PlantFoodExecutor() {
    }

    public static void execute(Plant plant, GameContext ctx) {
        PlantFoodProfile pf = plant.getSheet().getPlantFood();
        String name = plant.getSheet().getName();

        switch (pf.getKind()) {
            case NONE -> {
            }
            case INSTANT_SUN, RAPID_FIRE -> executeBasicAction(pf, plant, ctx, name);
            case AOE_BURST, MULTI_INSTAKILL -> executeCombatAction(pf, plant, ctx, name);
            case PERMANENT_HP_BOOST, FULL_HEAL_AND_ABSORB -> executeDefenseAction(pf, plant, ctx, name);
            case HYPNOTIZE, FREEZE_ALL, FORCE_MOVE_ALL_IN_LANE, CLONE_SELF,
                    CONVERT_EATER_TO_ALLY, AURA_BUFF, MULTI_DISARM ->
                ctx.log("[PlantFood] " + name + " triggered " + pf.getKind()
                        + " (extension point: wire a concrete PlantContext hook to fully simulate this).");
        }
    }

    private static void executeBasicAction(PlantFoodProfile pf, Plant plant, GameContext ctx, String name) {
        if (pf.getKind() == PlantFoodKind.INSTANT_SUN) {
            int amount = Math.round(pf.getAmount());
            ctx.addSun(amount);
            ctx.log("[PlantFood] " + name + " instantly produced " + amount + " sun.");
        } else if (pf.getKind() == PlantFoodKind.RAPID_FIRE) {
            if (plant.getAction() instanceof CooldownPlantAction cooldown) {
                cooldown.forceReady();
            }
            ctx.log("[PlantFood] " + name + " is unleashing rapid fire!");
        }
    }

    private static void executeCombatAction(PlantFoodProfile pf, Plant plant, GameContext ctx, String name) {
        if (pf.getKind() == PlantFoodKind.AOE_BURST) {
            float dmg = Math.max(plant.getEffectiveDamage() * 3f, 200f);
            int limit = pf.getCount() > 0 ? pf.getCount() : Integer.MAX_VALUE;
            int hit = 0;
            for (Zombie z : ctx.getZombiesInLane(plant.getLane())) {
                if (hit >= limit) {
                    break;
                }
                z.takeDamage(dmg, false);
                hit++;
            }
            ctx.log("[PlantFood] " + name + " unleashed an AoE burst on " + hit + " zombie(s).");
        } else if (pf.getKind() == PlantFoodKind.MULTI_INSTAKILL) {
            executeMultiInstakill(pf, ctx, name);
        }
    }

    private static void executeMultiInstakill(PlantFoodProfile pf, GameContext ctx, String name) {
        int limit = Math.max(1, pf.getCount());
        int killed = 0;
        for (int lane = 0; lane < ctx.getMap().getRows() && killed < limit; lane++) {
            for (Zombie z : ctx.getZombiesInLane(lane)) {
                if (killed >= limit) {
                    break;
                }
                z.takeDamage(Float.MAX_VALUE, true);
                killed++;
            }
        }
        ctx.log("[PlantFood] " + name + " instantly destroyed " + killed + " zombie(s).");
    }

    private static void executeDefenseAction(PlantFoodProfile pf, Plant plant, GameContext ctx, String name) {
        if (pf.getKind() == PlantFoodKind.PERMANENT_HP_BOOST) {
            plant.boostMaxHp(pf.getAmount());
            ctx.log("[PlantFood] " + name + " gained permanent +" + Math.round(pf.getAmount()) + " HP.");
        } else if (pf.getKind() == PlantFoodKind.FULL_HEAL_AND_ABSORB) {
            plant.heal(plant.getMaxHp());
            ctx.log("[PlantFood] " + name + " fully healed itself "
                    + "(pulling nearby zombies is an extension point not yet wired to a world hook).");
        }
    }

    public static void applyMintOwnUpgrades(Plant mint, GameContext ctx) {
        if (!mint.getUnlockedFlags().contains("reset family cooldowns")) {
            return;
        }
        PlantCategory family = mint.getSheet().getCategory();
        for (Plant member : getActivePlantsInFamily(family, ctx)) {
            if (member.getAction() instanceof CooldownPlantAction cooldown) {
                cooldown.forceReady();
            }
        }
    }

    private static List<Plant> getActivePlantsInFamily(PlantCategory family, GameContext ctx) {
        List<Plant> activePlants = new ArrayList<>();
        for (Plant plant : ctx.getPlants()) {
            if (plant.getSheet().getCategory() == family) {
                activePlants.add(plant);
            }
        }
        return activePlants;
    }
}
