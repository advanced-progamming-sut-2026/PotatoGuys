package pvz.Models.Entities.Plants.actions;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Games.GameContext;

/**
 * No-op action for passive plants ({@code WALL_NUT} tanks, purely reactive
 * {@code MODIFIER} plants like Lily Pad/Imitater/Hypno-shroom). They never
 * autonomously trigger; their utility comes from occupying a cell or from
 * being eaten/fed Plant Food, both handled elsewhere.
 */
public final class PassiveAction implements PlantAction {

    public static final PassiveAction INSTANCE = new PassiveAction();

    private PassiveAction() { }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx) { return false; }

    @Override
    public void execute(Plant plant, GameContext ctx) { /* never invoked */ }

    @Override
    public String getName() { return "Passive"; }
}
