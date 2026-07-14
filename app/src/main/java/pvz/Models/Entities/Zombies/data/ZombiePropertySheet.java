package pvz.Models.Entities.Zombies.data;

import java.util.List;

/**
 * Immutable data object representing a parsed zombie JSON property sheet.
 *
 * <p>Covers every {@code objClass} variant found in the game data:
 * {@code ZombiePropertySheet}, {@code ZombieGargantuarProps}, {@code ZombieRaProps},
 * {@code ZombieExplorerProps}, {@code ZombieTombRaiserProps},
 * {@code ZombieIceAgeHunterProps}, {@code ZombieDarkWizardProps}, etc.
 *
 * <p>Optional fields that do not apply to a given {@code objClass} default to
 * sensible no-op values ({@code 0}, {@code null}, {@code emptyList()}).
 *
 * <p>Use {@link Builder} to construct instances.
 */
public final class ZombiePropertySheet {

    // ── Identity ──────────────────────────────────────────────────────────────
    private final String alias;
    private final String objClass;

    // ── Core stats (pre-scaling) ──────────────────────────────────────────────
    private final float hitPoints;
    private final float eatDps;
    private final float speed;
    private final int wavePointCost;
    private final int weight;
    private final boolean canSpawnPlantFood;

    // ── Scaling recipes ───────────────────────────────────────────────────────
    private final List<ScaledProp> scaledProps;

    // ── Armour references (alias strings from ZombieArmorProps JSON array) ───
    private final List<String> armorAliases;

    // ── Display labels ────────────────────────────────────────────────────────
    private final List<ZombieStatEntry> zombieStats;

    // ── Gargantuar-specific ───────────────────────────────────────────────────
    private final String impType;
    private final float healthThresholdToThrowImp;
    private final float smashDamage;
    private final float smashDuration;

    // ── Ra Zombie-specific ────────────────────────────────────────────────────
    private final int maxClaimedSunCurrency;

    // ── TombRaiser-specific ───────────────────────────────────────────────────
    private final int ammo;
    private final int numberOfTombsToSpawn;
    private final float timeBetweenRaisings;

    // ── Explorer-specific ─────────────────────────────────────────────────────
    private final float maxTorchReach;

    // ── Hunter-specific ───────────────────────────────────────────────────────
    private final int snowballsPerBarrage;
    private final int farAttackRange;
    private final int nearAttackRange;

    // ── IceAgeTroglobite-specific ────────────────────────────────────────────
    private final int numberOfIceblocksToSpawnWith;

    // ── Size/misc ─────────────────────────────────────────────────────────────
    private final boolean imp; // size == "imp" in JSON → smaller/faster unit

    // ─────────────────────────────────────────────────────────────────────────

    private ZombiePropertySheet(Builder b) {
        this.alias = b.alias;
        this.objClass = b.objClass;
        this.hitPoints = b.hitPoints;
        this.eatDps = b.eatDps;
        this.speed = b.speed;
        this.wavePointCost = b.wavePointCost;
        this.weight = b.weight;
        this.canSpawnPlantFood = b.canSpawnPlantFood;
        this.scaledProps = List.copyOf(b.scaledProps);
        this.armorAliases = List.copyOf(b.armorAliases);
        this.zombieStats = List.copyOf(b.zombieStats);
        this.impType = b.impType;
        this.healthThresholdToThrowImp = b.healthThresholdToThrowImp;
        this.smashDamage = b.smashDamage;
        this.smashDuration = b.smashDuration;
        this.maxClaimedSunCurrency = b.maxClaimedSunCurrency;
        this.ammo = b.ammo;
        this.numberOfTombsToSpawn = b.numberOfTombsToSpawn;
        this.timeBetweenRaisings = b.timeBetweenRaisings;
        this.maxTorchReach = b.maxTorchReach;
        this.snowballsPerBarrage = b.snowballsPerBarrage;
        this.farAttackRange = b.farAttackRange;
        this.nearAttackRange = b.nearAttackRange;
        this.numberOfIceblocksToSpawnWith = b.numberOfIceblocksToSpawnWith;
        this.imp = b.imp;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getAlias()                      { return alias; }
    public String getObjClass()                   { return objClass; }
    public float getHitPoints()                   { return hitPoints; }
    public float getEatDps()                      { return eatDps; }
    public float getSpeed()                       { return speed; }
    public int getWavePointCost()                 { return wavePointCost; }
    public int getWeight()                        { return weight; }
    public boolean isCanSpawnPlantFood()          { return canSpawnPlantFood; }
    public List<ScaledProp> getScaledProps()      { return scaledProps; }
    public List<String> getArmorAliases()         { return armorAliases; }
    public List<ZombieStatEntry> getZombieStats() { return zombieStats; }
    public String getImpType()                    { return impType; }
    public float getHealthThresholdToThrowImp()   { return healthThresholdToThrowImp; }
    public float getSmashDamage()                 { return smashDamage; }
    public float getSmashDuration()               { return smashDuration; }
    public int getMaxClaimedSunCurrency()         { return maxClaimedSunCurrency; }
    public int getAmmo()                          { return ammo; }
    public int getNumberOfTombsToSpawn()          { return numberOfTombsToSpawn; }
    public float getTimeBetweenRaisings()         { return timeBetweenRaisings; }
    public float getMaxTorchReach()               { return maxTorchReach; }
    public int getSnowballsPerBarrage()           { return snowballsPerBarrage; }
    public int getFarAttackRange()                { return farAttackRange; }
    public int getNearAttackRange()               { return nearAttackRange; }
    public int getNumberOfIceblocksToSpawnWith()  { return numberOfIceblocksToSpawnWith; }
    public boolean isImp()                        { return imp; }

    @Override
    public String toString() {
        return "ZombiePropertySheet{alias='" + alias + "', objClass='" + objClass + "'}";
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    /** Fluent builder for {@link ZombiePropertySheet}. */
    public static final class Builder {
        // required
        private final String alias;
        private final String objClass;

        // optional – sensible defaults mirror the most common JSON values
        private float hitPoints = 190f;
        private float eatDps = 100f;
        private float speed = 0.185f;
        private int wavePointCost = 100;
        private int weight = 1000;
        private boolean canSpawnPlantFood = true;
        private List<ScaledProp> scaledProps = List.of();
        private List<String> armorAliases = List.of();
        private List<ZombieStatEntry> zombieStats = List.of();
        private String impType = null;
        private float healthThresholdToThrowImp = 0.5f;
        private float smashDamage = 0f;
        private float smashDuration = 2f;
        private int maxClaimedSunCurrency = 0;
        private int ammo = 0;
        private int numberOfTombsToSpawn = 2;
        private float timeBetweenRaisings = 6f;
        private float maxTorchReach = 1f;
        private int snowballsPerBarrage = 3;
        private int farAttackRange = 4;
        private int nearAttackRange = 1;
        private int numberOfIceblocksToSpawnWith = 0;
        private boolean imp = false;

        public Builder(String alias, String objClass) {
            this.alias = alias;
            this.objClass = objClass;
        }

        public Builder hitPoints(float v)                { hitPoints = v; return this; }
        public Builder eatDps(float v)                   { eatDps = v; return this; }
        public Builder speed(float v)                    { speed = v; return this; }
        public Builder wavePointCost(int v)              { wavePointCost = v; return this; }
        public Builder weight(int v)                     { weight = v; return this; }
        public Builder canSpawnPlantFood(boolean v)      { canSpawnPlantFood = v; return this; }
        public Builder scaledProps(List<ScaledProp> v)   { scaledProps = v; return this; }
        public Builder armorAliases(List<String> v)      { armorAliases = v; return this; }
        public Builder zombieStats(List<ZombieStatEntry> v) { zombieStats = v; return this; }
        public Builder impType(String v)                 { impType = v; return this; }
        public Builder healthThresholdToThrowImp(float v){ healthThresholdToThrowImp = v; return this; }
        public Builder smashDamage(float v)              { smashDamage = v; return this; }
        public Builder smashDuration(float v)            { smashDuration = v; return this; }
        public Builder maxClaimedSunCurrency(int v)      { maxClaimedSunCurrency = v; return this; }
        public Builder ammo(int v)                       { ammo = v; return this; }
        public Builder numberOfTombsToSpawn(int v)       { numberOfTombsToSpawn = v; return this; }
        public Builder timeBetweenRaisings(float v)      { timeBetweenRaisings = v; return this; }
        public Builder maxTorchReach(float v)            { maxTorchReach = v; return this; }
        public Builder snowballsPerBarrage(int v)        { snowballsPerBarrage = v; return this; }
        public Builder farAttackRange(int v)             { farAttackRange = v; return this; }
        public Builder nearAttackRange(int v)            { nearAttackRange = v; return this; }
        public Builder numberOfIceblocksToSpawnWith(int v){ numberOfIceblocksToSpawnWith = v; return this; }
        public Builder imp(boolean v)                    { imp = v; return this; }

        public ZombiePropertySheet build() {
            return new ZombiePropertySheet(this);
        }
    }
}
