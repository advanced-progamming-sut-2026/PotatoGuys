package com.pvz.models.entities.plants.data;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

import com.pvz.models.Constants;
import com.pvz.models.entities.plants.actions.shooters.ShooterPattern;
import com.pvz.models.entities.plants.enums.PlantCategory;
import com.pvz.models.entities.plants.enums.PlantTag;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.projectile.ProjectileType;
import com.pvz.utils.SaveManager;

/**
 * Singleton registry that loads every {@link PlantPropertySheet} from the
 * data-driven {@code plant_profiles.json} resource via {@link SaveManager}
 * (Gson) at startup.
 */
public final class PlantRegistry {

    private static final PlantRegistry INSTANCE = new PlantRegistry();

    private final Map<PlantType, PlantPropertySheet> sheets = new EnumMap<>(PlantType.class);

    private PlantRegistry() {
        load();
    }

    public static PlantRegistry getInstance() { return INSTANCE; }

    public PlantPropertySheet getSheet(PlantType type) { return sheets.get(type); }

    public int size() { return sheets.size(); }

    // ── Loading ───────────────────────────────────────────────────────────────

    private void load() {
        ProfileDto[] dtos = SaveManager.getInstance()
                .loadAbsolute(Constants.PLANT_PROFILES_PATH, ProfileDto[].class);
        if (dtos == null) {
            System.err.println("[PlantRegistry] plant_profiles.json could not be loaded; "
                    + "no plants will be available.");
            return;
        }
        for (ProfileDto dto : dtos) {
            try {
                PlantPropertySheet sheet = toSheet(dto);
                sheets.put(sheet.getType(), sheet);
            } catch (IllegalArgumentException e) {
                System.err.println("[PlantRegistry] Skipping malformed profile id="
                        + dto.id + ": " + e.getMessage());
            }
        }
    }

    private PlantPropertySheet toSheet(ProfileDto dto) {
        PlantType type = PlantType.valueOf(dto.type);
        PlantCategory category = PlantCategory.valueOf(dto.category);
        List<PlantTag> tags = new ArrayList<>();
        if (dto.tags != null) {
            for (String t : dto.tags) tags.add(PlantTag.valueOf(t));
        }


        return new PlantPropertySheet.Builder(dto.id, dto.name, type)
                .category(category)
                .tags(tags)
                .mint(dto.isMint)
                .sunCost(dto.sunCost)
                .baseHp(dto.baseHp)
                .damage(toDamage(dto.damage))
                .actionIntervalSeconds(dto.actionIntervalSeconds)
                .rechargeSeconds(dto.rechargeSeconds)
                .growth(toGrowth(dto.growth))
                .levelUpgrades(toLevelUpgrades(dto.levelUpgrades))
                .description(dto.description == null ? "" : dto.description)
                .build();
    }

    private DamageProfile toDamage(DamageDto d) {
        if (d == null) return new DamageProfile(DamageKind.NONE, 0, 1, null);
        return new DamageProfile(DamageKind.valueOf(d.kind), d.value, Math.max(1, d.count), d.stages);
    }

    private SunProduction toProduction(ProductionDto p) {
        if (p == null) return null;
        return new SunProduction(ProductionKind.valueOf(p.kind), p.amount, p.stages, p.stageSeconds);
    }

    private PlantFoodProfile toPlantFood(PlantFoodDto p) {
        if (p == null) return new PlantFoodProfile(PlantFoodKind.NONE, 0, 0, 0, List.of(), "");
        return new PlantFoodProfile(PlantFoodKind.valueOf(p.kind), p.amount, p.count,
                p.durationSeconds, p.flags == null ? List.of() : p.flags,
                p.description == null ? "" : p.description);
    }

    private GrowthProfile toGrowth(GrowthDto g) {
        if (g == null) return null;
        return new GrowthProfile(g.stageSeconds);
    }

    private List<LevelUpgrade> toLevelUpgrades(List<LevelUpgradeDto> dtos) {
        List<LevelUpgrade> result = new ArrayList<>();
        if (dtos == null) return result;
        for (LevelUpgradeDto dto : dtos) {
            List<StatModifier> stats = new ArrayList<>();
            if (dto.stats != null) {
                for (StatModifierDto s : dto.stats) {
                    stats.add(new StatModifier(StatKey.valueOf(s.stat), s.delta));
                }
            }
            result.add(new LevelUpgrade(dto.level, stats, dto.flags == null ? List.of() : dto.flags));
        }
        return result;
    }

    // ── Gson wire-format DTOs (package-private; mirror plant_profiles.json exactly) ──

    private static final class ProfileDto {
        int id;
        String name;
        String type;
        String category;
        List<String> tags;
        @SerializedName("isMint") boolean isMint;
        int sunCost;
        float baseHp;
        DamageDto damage;
        String projectileType;
        String shooterPattern;
        Float actionIntervalSeconds;
        Float rechargeSeconds;
        ProductionDto production;
        PlantFoodDto plantFood;
        List<LevelUpgradeDto> levelUpgrades;
        GrowthDto growth;
        String description;
    }

    private static final class DamageDto {
        String kind;
        float value;
        int count;
        float[] stages;
    }

    private static final class ProductionDto {
        String kind;
        float amount;
        float[] stages;
        float[] stageSeconds;
    }

    private static final class PlantFoodDto {
        String kind;
        float amount;
        int count;
        float durationSeconds;
        List<String> flags;
        String description;
    }

    private static final class LevelUpgradeDto {
        int level;
        List<StatModifierDto> stats;
        List<String> flags;
    }

    private static final class StatModifierDto {
        String stat;
        float delta;
    }

    private static final class GrowthDto {
        float[] stageSeconds;
    }
}
