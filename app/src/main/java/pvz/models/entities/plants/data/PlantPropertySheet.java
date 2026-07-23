package pvz.models.entities.plants.data;

import java.util.List;

import pvz.models.entities.plants.enums.PlantCategory;
import pvz.models.entities.plants.enums.PlantTag;
import pvz.models.entities.plants.enums.PlantType;
import pvz.models.entities.plants.actions.shooters.ShooterPattern;
import pvz.models.entities.projectile.ProjectileType;

/**
 * Immutable, data-driven description of one plant kind, loaded from
 * {@code plant_profiles.json} by {@link PlantRegistry}.
 */
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

    // --- Shooter Specifics ---
    private final ProjectileType projectileType;
    private final ShooterPattern shooterPattern;

    /** {@code null} means the plant has no autonomous repeating action. */
    private final Float actionIntervalSeconds;
    /** Card re-select cooldown; consumed by a future seed-packet/UI system, not by {@code Plant} itself. */
    private final Float rechargeSeconds;

    private final SunProduction production;   // non-null only for SUN_PRODUCER
    private final PlantFoodProfile plantFood;
    private final GrowthProfile growth;        // non-null only for wramp-up plants
    private final List<LevelUpgrade> levelUpgrades;
    private final String description;

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
        this.projectileType = b.projectileType;
        this.shooterPattern = b.shooterPattern;
        this.actionIntervalSeconds = b.actionIntervalSeconds;
        this.rechargeSeconds = b.rechargeSeconds;
        this.production = b.production;
        this.plantFood = b.plantFood;
        this.growth = b.growth;
        this.levelUpgrades = List.copyOf(b.levelUpgrades);
        this.description = b.description;
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
    public ProjectileType getProjectileType()      { return projectileType; }
    public ShooterPattern getShooterPattern()      { return shooterPattern; }
    public Float getActionIntervalSeconds()        { return actionIntervalSeconds; }
    public Float getRechargeSeconds()              { return rechargeSeconds; }
    public SunProduction getProduction()           { return production; }
    public PlantFoodProfile getPlantFood()         { return plantFood; }
    public GrowthProfile getGrowth()               { return growth; }
    public List<LevelUpgrade> getLevelUpgrades()   { return levelUpgrades; }
    public String getDescription()                 { return description; }

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
        private ProjectileType projectileType = null;
        private ShooterPattern shooterPattern = null;
        private Float actionIntervalSeconds = null;
        private Float rechargeSeconds = null;
        private SunProduction production = null;
        private PlantFoodProfile plantFood = new PlantFoodProfile(PlantFoodKind.NONE, 0, 0, 0, List.of(), "");
        private GrowthProfile growth = null;
        private List<LevelUpgrade> levelUpgrades = List.of();
        private String description = "";

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
        public Builder projectileType(ProjectileType v)        { projectileType = v; return this; }
        public Builder shooterPattern(ShooterPattern v)        { shooterPattern = v; return this; }
        public Builder actionIntervalSeconds(Float v)          { actionIntervalSeconds = v; return this; }
        public Builder rechargeSeconds(Float v)                { rechargeSeconds = v; return this; }
        public Builder production(SunProduction v)             { production = v; return this; }
        public Builder plantFood(PlantFoodProfile v)           { plantFood = v; return this; }
        public Builder growth(GrowthProfile v)                 { growth = v; return this; }
        public Builder levelUpgrades(List<LevelUpgrade> v)     { levelUpgrades = v; return this; }
        public Builder description(String v)                   { description = v; return this; }

        public PlantPropertySheet build() { return new PlantPropertySheet(this); }
    }
}