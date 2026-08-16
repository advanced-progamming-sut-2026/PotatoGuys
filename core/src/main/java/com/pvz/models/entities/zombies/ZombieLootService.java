package com.pvz.models.entities.zombies;

import java.util.Random;

import com.pvz.models.AppContext;
import com.pvz.models.entities.effects.LootDrop;
import com.pvz.models.games.GameContext;
import com.pvz.models.greenhouse.GreenHouse;
import com.pvz.models.user.Profile;
import com.pvz.models.user.User;

/**
 * Handles the "coin / diamond / pot" drop every zombie has a chance to leave behind on death.
 *
 * <p>Spec: on death, a zombie has a 10% chance to drop either 1 diamond, 50 coins, or a pot
 * (a free unlock for one of the account's currently locked greenhouse pots). The three outcomes
 * are equally likely once the 10% roll succeeds. This is invoked once from
 * {@code Zombie.triggerDeath()}, mirroring how the glowing-zombie plant-food drop is handled
 * right next to it, so {@link GameContext} itself stays unaware of coins/diamonds/greenhouse.
 *
 * <p>All three outcomes are interactive: a {@link LootDrop} pops out of the zombie's head and
 * only pays out once the player clicks it in the level — coins/diamonds are banked when the
 * drop arcs into its on-screen wallet, and the pot unlocks a random locked greenhouse pot
 * (if every pot is already unlocked, the drop turns into coins instead).
 */
public final class ZombieLootService {

    private static final float DROP_CHANCE = 0.10f;
    private static final int COIN_REWARD = 50;
    private static final int DIAMOND_REWARD = 1;
    /** The loot pops out at roughly head height, above the zombie's feet/center position. */
    private static final float HEAD_OFFSET = 70f;

    private static final Random RANDOM = new Random();

    private ZombieLootService() {
    }

    public static void rollAndApplyLoot(GameContext context, Zombie zombie) {
        if (RANDOM.nextFloat() >= DROP_CHANCE) {
            return;
        }

        User user = AppContext.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        int roll = RANDOM.nextInt(3);
        switch (roll) {
            case 0 -> dropCoins(context, zombie);
            case 1 -> dropDiamond(context, zombie);
            default -> dropPot(context, zombie, user);
        }

        user.saveUser();
    }

    private static void dropCoins(GameContext context, Zombie zombie) {
        context.spawnLootDrop(LootDrop.coin(context, zombie.getX(), zombie.getY() + HEAD_OFFSET, COIN_REWARD));
        context.log("A zombie dropped a coin! Click it to collect.");
    }

    private static void dropDiamond(GameContext context, Zombie zombie) {
        context.spawnLootDrop(LootDrop.diamond(context, zombie.getX(), zombie.getY() + HEAD_OFFSET, DIAMOND_REWARD));
        context.log("A zombie dropped a diamond! Click it to collect.");
    }

    private static void dropPot(GameContext context, Zombie zombie, User user) {
        GreenHouse greenHouse = resolveGreenHouse(user);
        if (greenHouse.getLockedPotCount() <= 0) {
            // Every pot is already unlocked: don't waste the drop, give coins instead.
            Profile profile = user.getProfile();
            profile.addCoins(COIN_REWARD);
            context.log("A zombie droped " + COIN_REWARD + " coins; you have " + profile.getCoins() + " coins now.");
            return;
        }

        context.spawnLootDrop(LootDrop.pot(context, zombie.getX(), zombie.getY() + HEAD_OFFSET));
        context.log("A zombie dropped a greenhouse pot! Click it to unlock.");
    }

    /** Mirrors GreenHouseController's own resolution so the drop always touches the live instance. */
    private static GreenHouse resolveGreenHouse(User user) {
        GreenHouse greenHouse = AppContext.getInstance().getGreenHouse();
        if (greenHouse == null) {
            greenHouse = user.getGreenHouse();
        }
        if (greenHouse == null) {
            greenHouse = new GreenHouse();
        }

        user.setGreenHouse(greenHouse);
        AppContext.getInstance().setGreenHouse(greenHouse);
        return greenHouse;
    }
}
