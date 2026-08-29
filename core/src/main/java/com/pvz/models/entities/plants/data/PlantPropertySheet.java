package com.pvz.models.entities.plants.data;

import java.util.List;

import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.PlantActionConfig;
import com.pvz.models.entities.plants.enums.PlantCategory;
import com.pvz.models.entities.plants.enums.PlantTag;
import com.pvz.models.entities.plants.enums.PlantType;

public final class PlantPropertySheet {

    private final int id;
    private final String name;
    private final PlantType type;
    private final PlantCategory category;
    private final List<PlantTag> tags;
    private final boolean mint;

    private final int sunCost;
    private final float baseHp;
    private final DamageProfile damage;

    private final Float actionIntervalSeconds;
    private final Float rechargeSeconds;

    public final PlantActionConfig attackConfig;
    public final PlantActionConfig feedConfig;
    public final PamAnimationConfig pamAnimationConfig;

    private final GrowthProfile growth;
    private final List<LevelUpgrade> levelUpgrades;
    private final String description;
    private final String onPlantFoodDescription;
    private final String overallDescription;
    private final String funDescription;

    public PlantPropertySheet(Builder b) {
        this.id = b.id;
        this.name = b.name;
        this.type = b.type;
        this.category = b.category;
        this.tags = List.copyOf(b.tags);
        this.mint = b.mint;
        this.sunCost = b.sunCost;
        this.baseHp = b.baseHp;
        this.damage = b.damage;
        this.actionIntervalSeconds = b.actionIntervalSeconds;
        this.rechargeSeconds = b.rechargeSeconds;
        this.growth = b.growth;
        this.levelUpgrades = List.copyOf(b.levelUpgrades);
        this.description = b.description;
        this.onPlantFoodDescription = b.onPlantFoodDescription;
        this.overallDescription = b.overallDescription;
        this.funDescription = b.funDescription;

        this.attackConfig = b.attackConfig;
        this.feedConfig = b.feedConfig;
        this.pamAnimationConfig = b.pamAnimationConfig;
    }

    public int getId()                             { return id; }
    public String getName()                        { return name; }
    public PlantType getType()                     { return type; }
    public PlantCategory getCategory()             { return category; }
    public List<PlantTag> getTags()                { return tags; }
    public boolean isMint()                        { return mint; }
    public int getSunCost()                        { return sunCost; }
    public float getBaseHp()                       { return baseHp; }
    public DamageProfile getDamage()               { return damage; }
    public Float getActionIntervalSeconds()        { return actionIntervalSeconds; }
    public Float getRechargeSeconds()              { return rechargeSeconds; }
    public GrowthProfile getGrowth()               { return growth; }
    public List<LevelUpgrade> getLevelUpgrades()   { return levelUpgrades; }
    public String getDescription()                 { return description; }
    public String getOnPlantFoodDescription()      { return onPlantFoodDescription; }
    public String getOverallDescription()          { return overallDescription; }
    public String getFunDescription()              { return funDescription; }

    public boolean hasTag(PlantTag tag) { return tags.contains(tag); }

    @Override
    public String toString() {
        return "PlantPropertySheet{id=" + id + ", name='" + name + "', category=" + category + '}';
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static final class Builder {
        private final int id;
        private final String name;
        private final PlantType type;
        private PlantCategory category = PlantCategory.MODIFIER;
        private List<PlantTag> tags = List.of();
        private boolean mint = false;
        private int sunCost = 0;
        private float baseHp = 300f;
        private DamageProfile damage = new DamageProfile(DamageKind.NONE, 0, 1, null);
        private Float actionIntervalSeconds = null;
        private Float rechargeSeconds = null;
        private GrowthProfile growth = null;
        private List<LevelUpgrade> levelUpgrades = List.of();
        private String description = "";
        private String onPlantFoodDescription = "";
        private String overallDescription = "";
        private String funDescription = "";
        private PlantActionConfig attackConfig;
        private PlantActionConfig feedConfig;
        private PamAnimationConfig pamAnimationConfig;

        public Builder(int id, String name, PlantType type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }

        public Builder category(PlantCategory v)               { category = v; return this; }
        public Builder tags(List<PlantTag> v)                  { tags = v; return this; }
        public Builder mint(boolean v)                         { mint = v; return this; }
        public Builder sunCost(int v)                          { sunCost = v; return this; }
        public Builder baseHp(float v)                         { baseHp = v; return this; }
        public Builder damage(DamageProfile v)                 { damage = v; return this; }
        public Builder actionIntervalSeconds(Float v)          { actionIntervalSeconds = v; return this; }
        public Builder rechargeSeconds(Float v)                { rechargeSeconds = v; return this; }
        public Builder growth(GrowthProfile v)                 { growth = v; return this; }
        public Builder levelUpgrades(List<LevelUpgrade> v)     { levelUpgrades = v; return this; }
        public Builder description(String v)                   { description = v; return this; }
        public Builder onPlantFoodDescription(String v)        { onPlantFoodDescription = v; return this; }
        public Builder overallDescription(String v)            { overallDescription = v; return this; }
        public Builder funDescription(String v)                { funDescription = v; return this; }
        public Builder plantAttackConfig(PlantActionConfig v)  { attackConfig = v; return this;}
        public Builder plantFeedConfig(PlantActionConfig v)    { feedConfig = v; return this;}
        public Builder pamAnimationConfig(PamAnimationConfig v){ pamAnimationConfig = v; return this;}

        public PlantPropertySheet build() { return new PlantPropertySheet(this); }
    }
}
