package pvz.Models.Entities.Plants.data;

import pvz.Models.Entities.Plants.Enums.PlantCategory;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantContext;
import pvz.Models.Entities.Plants.actions.CooldownPlantAction;
import pvz.Models.Entities.Zombies.Zombie;

/**
 * Interprets a plant's {@link PlantFoodProfile} at the moment its Plant Food
 * is consumed (either fed directly, or broadcast by a Mint's
 * {@link pvz.Models.Entities.Plants.actions.FamilyBuffAction}).
 *
 * <p>Dispatches on the bounded {@link PlantFoodKind} vocabulary — never on
 * plant identity — so every current and future plant automatically gets a
 * working effect simply by declaring a kind in its JSON profile.
 */
public final class PlantFoodExecutor {

    private PlantFoodExecutor() { }

    public static void execute(Plant plant, PlantContext ctx) {
        PlantFoodProfile pf = plant.getSheet().getPlantFood();
        String name = plant.getSheet().getName();
        switch (pf.getKind()) {
            case NONE -> { /* single-use plants already consumed themselves */ }

            case INSTANT_SUN -> {
                int amount = Math.round(pf.getAmount());
                ctx.addSun(amount);
                ctx.log("[PlantFood] " + name + " instantly produced " + amount + " sun.");
            }

            case RAPID_FIRE -> {
                if (plant.getAction() instanceof CooldownPlantAction cooldown) {
                    cooldown.forceReady();
                }
                ctx.log("[PlantFood] " + name + " is unleashing rapid fire!");
            }

            case AOE_BURST -> {
                float dmg = Math.max(plant.getEffectiveDamage() * 3f, 200f);
                int limit = pf.getCount() > 0 ? pf.getCount() : Integer.MAX_VALUE;
                int hit = 0;
                for (Zombie z : ctx.getZombiesInLane(plant.getLane())) {
                    if (hit >= limit) break;
                    ctx.dealDamageToZombie(z, dmg, false);
                    hit++;
                }
                ctx.log("[PlantFood] " + name + " unleashed an AoE burst on " + hit + " zombie(s).");
            }

            case MULTI_INSTAKILL -> {
                int limit = Math.max(1, pf.getCount());
                int killed = 0;
                for (int lane = 0; lane < ctx.getLanes() && killed < limit; lane++) {
                    for (Zombie z : ctx.getZombiesInLane(lane)) {
                        if (killed >= limit) break;
                        ctx.dealDamageToZombie(z, Float.MAX_VALUE, true);
                        killed++;
                    }
                }
                ctx.log("[PlantFood] " + name + " instantly destroyed " + killed + " zombie(s).");
            }

            case PERMANENT_HP_BOOST -> {
                plant.boostMaxHp(pf.getAmount());
                ctx.log("[PlantFood] " + name + " gained permanent +" + Math.round(pf.getAmount()) + " HP.");
            }

            case FULL_HEAL_AND_ABSORB -> {
                plant.heal(plant.getMaxHp());
                ctx.log("[PlantFood] " + name + " fully healed itself "
                        + "(pulling nearby zombies is an extension point not yet wired to a world hook).");
            }

            case HYPNOTIZE, FREEZE_ALL, FORCE_MOVE_ALL_IN_LANE, CLONE_SELF,
                 CONVERT_EATER_TO_ALLY, AURA_BUFF, MULTI_DISARM ->
                ctx.log("[PlantFood] " + name + " triggered " + pf.getKind()
                        + " (extension point: wire a concrete PlantContext hook to fully simulate this).");
        }
    }

    /**
     * Applies a Mint's own level-upgrade flags after it broadcasts the family
     * buff — currently implements {@code "reset family cooldowns"} by forcing
     * every family member's cooldown action to fire again immediately.
     */
    public static void applyMintOwnUpgrades(Plant mint) {
        if (!mint.getUnlockedFlags().contains("reset family cooldowns")) return;
        PlantContext ctx = mint.getContext();
        PlantCategory family = mint.getSheet().getCategory();
        for (Plant member : ctx.getActivePlantsInFamily(family)) {
            if (member.getAction() instanceof CooldownPlantAction cooldown) {
                cooldown.forceReady();
            }
        }
    }
}
