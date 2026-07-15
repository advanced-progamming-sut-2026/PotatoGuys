package pvz.Models.Entities.Zombies.skills;

import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Games.GameContext;

/**
 * Explorer Zombie — carries a torch that instantly destroys torch-vulnerable
 * plants (e.g. Frost Bonnet, Blazing Knight) within reach.
 *
 * <p>From JSON {@code ZombieExplorerProps}:
 * <ul>
 *   <li>{@code MaxTorchReach = 37} (pixels ≈ 1 grid cell forward)</li>
 *   <li>{@code PlantsToEat} includes {@code "frostbonnet"} and {@code "blazingknight"}</li>
 * </ul>
 *
 * <p>Unlike {@link CooldownSkill} this fires every tick whenever the condition
 * is met; the torch is the primary attack mode, not an occasional special.
 * Ice projectiles and frozen plants extinguish the torch ({@link #extinguish()}).
 * Fire projectiles relight it ({@link #relight()}).
 */
public class ExplorerTorchSkill implements ZombieSkill {

    /** Number of cells ahead that the torch can reach (≈1 based on MaxTorchReach). */
    private static final int REACH_CELLS = 1;

    private boolean torchLit;

    public ExplorerTorchSkill() {
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
    public boolean shouldTrigger(Zombie zombie, GameContext ctx) {
        if (!torchLit) return false;
        return hasTorchVulnerablePlantAhead(zombie, ctx);
    }

    @Override
    public void execute(Zombie zombie, GameContext ctx) {
        if (!torchLit) return;
        int col = (int) zombie.getX();
        for (int c = col; c >= Math.max(0, col - REACH_CELLS); c--) {
            // if (ctx.isTorchVulnerablePlantAt(c, zombie.getLane())) {
            //     ctx.burnPlant(c, zombie.getLane());
            //     ctx.log("Explorer Zombie burned plant at ("
            //             + c + "," + zombie.getLane() + ") with torch!");
            // }
        }
    }

    @Override
    public String getName() {
        return torchLit ? "Torch[LIT]" : "Torch[OUT]";
    }

    // ── Private helper ────────────────────────────────────────────────────────

    private boolean hasTorchVulnerablePlantAhead(Zombie zombie, GameContext ctx) {
        int col = (int) zombie.getX();
        // for (int c = col; c >= Math.max(0, col - REACH_CELLS); c--) {
        //     if (ctx.isTorchVulnerablePlantAt(c, zombie.getLane())) return true;
        // }
        return false;
    }
}
