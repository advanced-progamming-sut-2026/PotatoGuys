package com.pvz.models.entities.zombies;

import java.util.Random;

import com.pvz.models.AppContext;
import com.pvz.models.games.GameContext;
import com.pvz.models.greenhouse.GreenHouse;
import com.pvz.models.greenhouse.GreenHousePot;
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
 */
public final class ZombieLootService {

    private static final float DROP_CHANCE = 0.10f;
    private static final int COIN_REWARD = 50;
    private static final int DIAMOND_REWARD = 1;

    private static final Random RANDOM = new Random();

    private ZombieLootService() {
    }

    public static void rollAndApplyLoot(GameContext context) {
        if (RANDOM.nextFloat() >= DROP_CHANCE) {
            return;
        }

        User user = AppContext.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        int roll = RANDOM.nextInt(3);
        switch (roll) {
            case 0 -> dropCoins(context, user);
            case 1 -> dropDiamond(context, user);
            default -> dropPot(context, user);
        }

        user.saveUser();
    }

    private static void dropCoins(GameContext context, User user) {
        Profile profile = user.getProfile();
        profile.addCoins(COIN_REWARD);
        context.log("A zombie droped 50 coins; you have " + profile.getCoins() + " coins now.");
    }

    private static void dropDiamond(GameContext context, User user) {
        Profile profile = user.getProfile();
        profile.addDiamonds(DIAMOND_REWARD);
        context.log("A zombie droped a diamond; you have " + profile.getDiamonds() + " diamonds now.");
    }

    private static void dropPot(GameContext context, User user) {
        GreenHouse greenHouse = resolveGreenHouse(user);
        GreenHousePot unlocked = greenHouse.unlockRandomPot();

        if (unlocked == null) {
            // Every pot is already unlocked: don't waste the drop, give coins instead.
            dropCoins(context, user);
            return;
        }

        context.log("A zombie droped a pot; you have " + greenHouse.getUnlockedPotCount() + " pots now.");
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
