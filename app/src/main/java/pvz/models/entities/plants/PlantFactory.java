package pvz.models.entities.plants;

import pvz.models.entities.plants.actions.FamilyBuffAction;
import pvz.models.entities.plants.actions.HomingAction;
import pvz.models.entities.plants.actions.LobberAction;
import pvz.models.entities.plants.actions.MeleeAction;
import pvz.models.entities.plants.actions.PassiveAction;
import pvz.models.entities.plants.actions.PlantAction;
import pvz.models.entities.plants.actions.SunProducerAction;
import pvz.models.entities.plants.actions.TriggeredExplosiveAction;
import pvz.models.entities.plants.actions.shooters.ShooterAction;
import pvz.models.entities.plants.data.PlantPropertySheet;
import pvz.models.entities.plants.data.PlantRegistry;
import pvz.models.entities.plants.enums.PlantTag;
import pvz.models.entities.plants.enums.PlantType;
import pvz.models.games.GameContext;

/**
 * Creates fully configured {@link Plant} instances from a {@link PlantType},
 * the plant-side mirror of {@link pvz.models.entities.zombies.ZombieFactory}.
 *
 * <p>The factory performs two steps:
 * <ol>
 *   <li>Looks up the {@link PlantPropertySheet} in {@link PlantRegistry} (loaded from JSON).</li>
 *   <li>Builds the single {@link PlantAction} strategy for the sheet's category
 *       (rule: dispatch on the ~9 generic categories/mint-flag, never on individual plant IDs).</li>
 * </ol>
 */
public class PlantFactory {

    private static final PlantRegistry REGISTRY = PlantRegistry.getInstance();

    /** Places a new, live plant on the board at (col, lane), registered against {@code ctx}. */
    public Plant create(PlantType type, int col, int lane, int level, boolean boosted, GameContext ctx) {
        PlantPropertySheet sheet = REGISTRY.getSheet(type);
        if (sheet == null) {
            throw new IllegalArgumentException("Unknown plant type: " + type);
        }
        return new Plant(sheet, buildAction(sheet), col, lane, level, boosted, ctx);
    }

    /** Convenience overload: level 1, not boosted. */
    public Plant create(PlantType type, int col, int lane, GameContext ctx) {
        return create(type, col, lane, 1, false, ctx);
    }

    /**
     * Builds an "unplaced" record — a {@link Plant} with no grid position and
     * no {@link GameContext}, used by catalog/collection UIs that only need
     * type/level/boosted/cost bookkeeping. Must never be registered with a
     * {@link pvz.models.engine.GameEngine}.
     */
    public Plant createUnplaced(PlantType type, int level, boolean boosted) {
        PlantPropertySheet sheet = REGISTRY.getSheet(type);
        if (sheet == null) {
            throw new IllegalArgumentException("Unknown plant type: " + type);
        }
        return new Plant(sheet, buildAction(sheet), -1, -1, level, boosted, null);
    }

    /** Copies an existing (typically owned/unplaced) plant's type/level/boosted state. */
    public Plant copyUnplaced(Plant source) {
        return createUnplaced(source.getType(), source.getLevel(), source.isBoosted());
    }

    // ── Action construction (driven by category, never by plant ID) ──────────

    private PlantAction buildAction(PlantPropertySheet sheet) {
        if (sheet.isMint()) {
            return new FamilyBuffAction();
        }
        Float interval = sheet.getActionIntervalSeconds();
        return switch (sheet.getCategory()) {
            case SUN_PRODUCER -> interval != null ? new SunProducerAction(interval) : PassiveAction.INSTANCE;
            case SHOOTER, STRIKE_THROUGH -> interval != null ? new ShooterAction(interval) : PassiveAction.INSTANCE;
            case LOBBER -> interval != null ? new LobberAction(interval) : PassiveAction.INSTANCE;
            case MELEE -> interval != null ? new MeleeAction(interval) : PassiveAction.INSTANCE;
            case HOMING -> interval != null ? new HomingAction(interval) : PassiveAction.INSTANCE;
            case EXPLOSIVE -> new TriggeredExplosiveAction(sheet.hasTag(PlantTag.TRAP));
            case WALL_NUT, MODIFIER -> PassiveAction.INSTANCE;
        };
    }
}

