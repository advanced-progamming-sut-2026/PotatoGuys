package com.pvz.models.entities.zombies.skills;

import com.pvz.controller.game.GameController;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.data.DamageKind;
import com.pvz.models.entities.plants.enums.PlantTag;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.config.ExplorerTorchSkillConfig;
import com.pvz.models.games.GameContext;

import java.util.List;

/**
 * Explorer Zombie — carries a torch that instantly destroys torch-vulnerable
 * plants (e.g. Frost Bonnet, Blazing Knight) within reach.
 *
 * <p>This is a state, not a one-shot plugin: when {@link #shouldTrigger} finds
 * a torch-vulnerable plant in the zombie's tile, the zombie switches into this
 * skill's state, the torch burns the plant on entry, and the "power" clip plays
 * until it finishes before the zombie resumes {@code WalkState}.
 *
 * <p>Ice projectiles and frozen plants extinguish the torch ({@link #extinguish()}).
 * Fire projectiles relight it ({@link #relight()}).
 */
public class ExplorerTorchSkill extends ZombieSkill {

    private boolean torchLit;

    public ExplorerTorchSkill(ExplorerTorchSkillConfig config) {
        super(config);
        this.torchLit = true;
    }

    // ── Torch state mutators (called by game/plant events) ────────────────────

    public void extinguish() {
        torchLit = false;
    }

    public void relight() {
        torchLit = true;
    }

    public boolean isTorchLit() {
        return torchLit;
    }

    // ── ZombieSkill ───────────────────────────────────────────────────────────

    @Override
    public boolean shouldTrigger(Zombie zombie, GameContext ctx, float dt) {
        return torchLit && hasTorchVulnerablePlantAhead(zombie, ctx);
    }

    @Override
    protected void doExecute(Zombie zombie, GameContext ctx) {
        if (!torchLit) return;
        int col = GameController.worldXtoCol(zombie.getX());
        int row = GameController.worldYtoLane(zombie.getY());
        List<Plant> plants = ctx.getPlantsAt(col, row);
        if (plants.isEmpty()) return;
        Plant plant = plants.getFirst();
        if (plant.getSheet().hasTag(PlantTag.ICE)) {
            extinguish();
            ctx.log("Explorer Zombie's torch was extinguished by an icy plant at ("
                    + col + "," + row + ")!");
            return;
        }
        plant.takeDamage(999f, DamageKind.INSTA_KILL);
        ctx.log("Explorer Zombie burned plant at ("
                + col + "," + row + ") with torch!");
    }

    @Override
    public String getName() {
        return torchLit ? "Torch[LIT]" : "Torch[OUT]";
    }

    // ── Private helper ────────────────────────────────────────────────────────

    private boolean hasTorchVulnerablePlantAhead(Zombie zombie, GameContext ctx) {
        int col = GameController.worldXtoCol(zombie.getX());
        return ctx.isPlantAt(col, GameController.worldYtoLane(zombie.getY()));
    }
}
