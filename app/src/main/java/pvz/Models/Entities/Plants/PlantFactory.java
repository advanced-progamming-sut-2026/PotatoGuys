package pvz.Models.Entities.Plants;

import pvz.Models.Entities.Plants.Enums.PlantTag;
import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Entities.Plants.actions.FamilyBuffAction;
import pvz.Models.Entities.Plants.actions.HomingAction;
import pvz.Models.Entities.Plants.actions.LobberAction;
import pvz.Models.Entities.Plants.actions.MeleeAction;
import pvz.Models.Entities.Plants.actions.PassiveAction;
import pvz.Models.Entities.Plants.actions.PlantAction;
import pvz.Models.Entities.Plants.actions.ShooterAction;
import pvz.Models.Entities.Plants.actions.SunProducerAction;
import pvz.Models.Entities.Plants.actions.TriggeredExplosiveAction;
import pvz.Models.Entities.Plants.data.PlantPropertySheet;
import pvz.Models.Entities.Plants.data.PlantRegistry;

/**
 * Creates fully configured {@link Plant} instances from a {@link PlantType},
 * the plant-side mirror of {@link pvz.Models.Entities.Zombies.ZombieFactory}.
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
    public Plant create(PlantType type, int col, int lane, int level, boolean boosted, PlantContext ctx) {
        PlantPropertySheet sheet = REGISTRY.getSheet(type);
        if (sheet == null) {
            throw new IllegalArgumentException("Unknown plant type: " + type);
        }
        return new Plant(sheet, buildAction(sheet), col, lane, level, boosted, ctx);
    }

    /** Convenience overload: level 1, not boosted. */
    public Plant create(PlantType type, int col, int lane, PlantContext ctx) {
        return create(type, col, lane, 1, false, ctx);
    }

    /**
     * Builds an "unplaced" record — a {@link Plant} with no grid position and
     * no {@link PlantContext}, used by catalog/collection UIs that only need
     * type/level/boosted/cost bookkeeping. Must never be registered with a
     * {@link pvz.Models.Engine.GameEngine}.
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

