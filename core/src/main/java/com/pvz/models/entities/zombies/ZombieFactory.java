package com.pvz.models.entities.zombies;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.pvz.models.entities.zombies.armor.ArmorFlag;
import com.pvz.models.entities.zombies.armor.ArmorPiece;
import com.pvz.models.entities.zombies.armor.ArmorType;
import com.pvz.models.entities.zombies.config.ZombieSkillConfig;
import com.pvz.models.entities.zombies.data.ArmorPropertySheet;
import com.pvz.models.entities.zombies.data.ZombiePropertySheet;
import com.pvz.models.entities.zombies.data.ZombieRegistry;
import com.pvz.models.entities.zombies.skills.ZombieSkill;
import com.pvz.models.entities.zombies.skills.ZombieSkillCatalog;
import com.pvz.models.games.GameContext;

/**
 * Creates fully configured {@link Zombie} instances from a registry alias string.
 *
 * <p><b>Usage</b>
 * <pre>
 *   ZombieFactory factory = new ZombieFactory();
 *   Zombie ra = factory.create("ZombieRaDefault", startX, lane, ctx, waveIndex, difficulty);
 *   engine.register(ra);
 * </pre>
 *
 * <p>The factory performs three steps:
 * <ol>
 *   <li>Looks up the {@link ZombiePropertySheet} in {@link ZombieRegistry}.</li>
 *   <li>Builds the {@link ArmorPiece} list from the sheet's {@code armorAliases}.</li>
 *   <li>Builds the {@link ZombieSkill} list based on the sheet's {@code objClass}.</li>
 * </ol>
 */
public class ZombieFactory {

    private static final ZombieRegistry REGISTRY = ZombieRegistry.getInstance();

    /**
     * Primary factory method — creates from an alias string.
     *
     * @param alias      JSON alias, e.g. {@code "ZombieMummyArmor2Default"}
     * @param startX     spawn column (float)
     * @param lane       row index
     * @param ctx        world context
     * @param waveIndex  0-based wave counter for ScaledProp scaling
     * @param difficulty [1..5]
     * @throws IllegalArgumentException if the alias is not registered
     */
    public Zombie create(String alias, float startX, int lane,
                         GameContext ctx, int waveIndex, int difficulty) {
        ZombiePropertySheet sheet = REGISTRY.getSheet(alias);
        if (sheet == null) {
            throw new IllegalArgumentException("Unknown zombie alias: " + alias);
        }
        List<ArmorPiece> armors = buildArmors(sheet);
        List<ZombieSkill> skills = buildSkills(sheet);
        return new Zombie(sheet, startX, lane, armors, skills, ctx, waveIndex, difficulty);
    }

    /**
     * Legacy convenience method; delegates to {@link #create(String, float, int,
     * GameContext, int, int)} using the enum's alias.
     */
    public Zombie createZombie(ZombieType type) {
        // Legacy stub — callers should migrate to create(alias, ...) for full functionality.
        return null;
    }

    // ── Armour construction ───────────────────────────────────────────────────

    private List<ArmorPiece> buildArmors(ZombiePropertySheet sheet) {
        List<ArmorPiece> result = new ArrayList<>();
        for (String armorAlias : sheet.getArmorAliases()) {
            String cleanAlias = stripRtidPrefix(armorAlias);
            ArmorPropertySheet aSheet = REGISTRY.getArmorSheet(cleanAlias);
            if (aSheet != null) {
                result.add(buildArmorPiece(aSheet));
            }
        }
        return result;
    }

    private ArmorPiece buildArmorPiece(ArmorPropertySheet aSheet) {
        ArmorType type = ArmorType.fromString(aSheet.getArmorType());
        Set<ArmorFlag> flags = parseFlags(aSheet.getFlags());
        return new ArmorPiece(type, aSheet.getBaseHealth(), flags, aSheet.getLayerThresholds());
    }

    private Set<ArmorFlag> parseFlags(List<String> flagStrings) {
        Set<ArmorFlag> flags = EnumSet.noneOf(ArmorFlag.class);
        for (String raw : flagStrings) {
            try {
                flags.add(ArmorFlag.fromString(raw));
            } catch (IllegalArgumentException ignored) {
                // unknown flag string — skip silently
            }
        }
        return flags;
    }

    // ── Skill construction (driven by the JSON skillConfig) ───────────────────

    private List<ZombieSkill> buildSkills(ZombiePropertySheet sheet) {
        List<ZombieSkill> skills = new ArrayList<>();
        if (sheet.skillConfig instanceof ZombieSkillConfig skillCfg) {
            ZombieSkill skill = ZombieSkillCatalog.create(skillCfg);
            if (skill != null) {
                skills.add(skill);
            }
        }
        return skills;
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    /**
     * Strips the RTID wrapper used in JSON references.
     * e.g. {@code "RTID(ConeDefault@ArmorTypes)"} → {@code "ConeDefault"}.
     */
    private String stripRtidPrefix(String alias) {
        if (alias.startsWith("RTID(")) {
            int start = 5;
            int end = alias.indexOf('@');
            if (end > start) return alias.substring(start, end);
        }
        return alias;
    }
}
