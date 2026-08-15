package com.pvz.models.entities.plants;

import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.games.GameContext;

/**
 * Pumpkin acts as a protective cover: any zombie trying to eat a plant that
 * shares a tile with a living Pumpkin must chew through the Pumpkin first.
 */
public final class PumpkinShield {

    private PumpkinShield() {
    }

    /**
     * Returns the living Pumpkin shielding {@code protectedPlant}, or {@code null}
     * when the plant is unprotected (or is itself a Pumpkin).
     */
    public static Plant shieldFor(Plant protectedPlant, GameContext ctx) {
        if (protectedPlant.getType() == PlantType.Pumpkin) {
            return null;
        }
        for (Plant other : ctx.getPlantsAt(protectedPlant.getCol(), protectedPlant.getLane())) {
            if (other.getType() == PlantType.Pumpkin && !other.isDead()) {
                return other;
            }
        }
        return null;
    }
}
