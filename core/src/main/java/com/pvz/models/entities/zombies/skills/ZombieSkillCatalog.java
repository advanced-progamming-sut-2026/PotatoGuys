package com.pvz.models.entities.zombies.skills;

import com.pvz.models.entities.zombies.config.ExplorerTorchSkillConfig;
import com.pvz.models.entities.zombies.config.GargantuarSkillConfig;
import com.pvz.models.entities.zombies.config.HunterSnowballSkillConfig;
import com.pvz.models.entities.zombies.config.RaStealSunSkillConfig;
import com.pvz.models.entities.zombies.config.TombRaiserSkillConfig;
import com.pvz.models.entities.zombies.config.WizardZapSkillConfig;
import com.pvz.models.entities.zombies.config.ZombieSkillConfig;
import com.pvz.models.entities.zombies.fsm.ZombieState;

/**
 * Maps a {@link ZombieSkillConfig} (parsed from {@code zombie_actions.json}) to
 * the concrete {@link ZombieSkill} instance — the zombie counterpart of the
 * plants' config→action wiring in {@code PlantFactory.buildConfigAction}.
 *
 * <p>Which skill a zombie gets, and how it behaves, is decided entirely by the
 * JSON config's {@code "class"} tag + parameters. Adding a new skill means
 * adding one {@code instanceof} branch here plus a config subclass.
 */
public final class ZombieSkillCatalog {

    private ZombieSkillCatalog() {
    }

    /** @return the skill for {@code config}, or {@code null} if the class tag is unknown. */
    public static ZombieState create(ZombieSkillConfig config) {
        if (config == null) return null;
        if (config instanceof ExplorerTorchSkillConfig explorerConfig) {
            return new ExplorerTorchSkill(explorerConfig);
        }
        if (config instanceof GargantuarSkillConfig gargantuarConfig) {
            return new GargantuarSkill(gargantuarConfig);
        }
        if (config instanceof RaStealSunSkillConfig raConfig) {
            return new RaStealSunSkill(raConfig);
        }
        if (config instanceof TombRaiserSkillConfig tombRaiserConfig) {
            return new TombRaiserSkill(tombRaiserConfig);
        }
        if (config instanceof WizardZapSkillConfig wizardConfig) {
            return new WizardZapSkill(wizardConfig);
        }
        if (config instanceof HunterSnowballSkillConfig hunterConfig) {
            return new HunterSnowballSkill(hunterConfig);
        }
        return null;
    }
}
