package pvz.Models.Entities.Zombies.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton registry holding all {@link ZombiePropertySheet} and
 * {@link ArmorPropertySheet} definitions.
 *
 * <p>Data is hard-coded from the official JSON property sheets rather than
 * parsed at runtime, because the project uses pure Java without a mandatory
 * JSON-loading phase. New entries can be added to the {@code init*} methods
 * as the game expands.
 *
 * <p>Lookup is by <em>alias</em> string (the first element of the JSON
 * {@code "aliases"} array), e.g. {@code "ZombieMummyDefault"}.
 */
public final class ZombieRegistry {

    private static final ZombieRegistry INSTANCE = new ZombieRegistry();

    private final Map<String, ZombiePropertySheet> zombieSheets = new HashMap<>();
    private final Map<String, ArmorPropertySheet>  armorSheets  = new HashMap<>();

    private ZombieRegistry() {
        initArmorSheets();
        initCoreZombies();
        initEgyptZombies();
        initIceCaveZombies();
        initBeachZombies();
        initDarkAgesZombies();
        initZombosses();
    }

    public static ZombieRegistry getInstance() { return INSTANCE; }

    /** @return the sheet for the given alias, or {@code null} if not registered */
    public ZombiePropertySheet getSheet(String alias) { return zombieSheets.get(alias); }

    /** @return the armour sheet for the given alias, or {@code null} if not registered */
    public ArmorPropertySheet getArmorSheet(String alias) { return armorSheets.get(alias); }

    // ─────────────────────────────────────────────────────────────────────────
    // Armour definitions (from JSON ArmorPropertySheet objects)
    // ─────────────────────────────────────────────────────────────────────────

    private void initArmorSheets() {
        putArmor("ConeDefault",         "Cone",         370f,
                 List.of("damageable","droppable","helm"));
        putArmor("BucketDefault",       "Bucket",      1100f,
                 List.of("metallic","damageable","droppable","helm"));
        putArmor("BrickDefault",        "Brick",       2200f,
                 List.of("damageable","droppable","helm"));
        putArmor("ShoulderArmorDefault","ShoulderArmor",1600f,
                 List.of("damageable","passdamage"));
        putArmor("CrownDefault",        "Crown",       1600f,
                 List.of("damageable","droppable","metallic","helm"));
        putArmor("NewspaperDefault",    "Newspaper",    800f,
                 List.of("damageable"));
    }

    private void putArmor(String alias, String type, float hp, List<String> flags) {
        float[] layers = {0.666f, 0.333f};
        armorSheets.put(alias, new ArmorPropertySheet(alias, type, hp, flags, layers));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Core / all-chapter zombies
    // ─────────────────────────────────────────────────────────────────────────

    private void initCoreZombies() {
        List<ScaledProp> stdScale = standardScale();

        // Basic
        putZombie(basicSheet("ZombieTutorialDefault", "ZombiePropertySheet",
                190f, 100f, 0.185f, 100, 1000, stdScale));

        // Cone-head
        putZombie(basicSheet("ZombieTutorialArmor1Default", "ZombiePropertySheet",
                190f, 100f, 0.185f, 200, 3000, stdScale)
                .armorAliases(List.of("ConeDefault")).build());

        // Bucket-head
        putZombie(basicSheet("ZombieTutorialArmor2Default", "ZombiePropertySheet",
                190f, 100f, 0.185f, 400, 4000, stdScale)
                .armorAliases(List.of("BucketDefault")).build());

        // Brick-head
        putZombie(basicSheet("ZombieTutorialArmor4Default", "ZombiePropertySheet",
                190f, 100f, 0.185f, 700, 3000, stdScale)
                .armorAliases(List.of("BrickDefault")).build());

        // Flag zombie (same stats as basic)
        putZombie(basicSheet("ZombieTutorialFlagDefault", "ZombiePropertySheet",
                190f, 100f, 0.185f, 100, 1000, stdScale));

        // Imp
        putZombie(new ZombiePropertySheet.Builder("ZombieTutorialImpDefault", "ZombiePropertySheet")
                .hitPoints(190f).eatDps(100f).speed(0.22f).wavePointCost(100).weight(1000)
                .canSpawnPlantFood(false).scaledProps(stdScale).imp(true).build());

        // Gargantuar (basic)
        putZombie(gargantuarSheet("ZombieGargantuarBasic",
                "ZombieGargantuarProps", 3600f, 1500f, 0.24f, 1500,
                "ZombieTutorialImpDefault", stdScale));

        // Newspaper zombie
        putZombie(new ZombiePropertySheet.Builder("ZombieModernNewspaperDefault",
                "ZombieModernNewspaperProps")
                .hitPoints(460f).eatDps(200f).speed(0.22f).wavePointCost(700).weight(4000)
                .canSpawnPlantFood(true).scaledProps(stdScale)
                .armorAliases(List.of("NewspaperDefault")).build());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Ancient Egypt
    // ─────────────────────────────────────────────────────────────────────────

    private void initEgyptZombies() {
        List<ScaledProp> stdScale = standardScale();

        putZombie(basicSheet("ZombieMummyDefault",       "ZombiePropertySheet",
                190f, 100f, 0.185f, 100, 1000, stdScale));
        putZombie(basicSheet("ZombieMummyArmor1Default", "ZombiePropertySheet",
                190f, 100f, 0.185f, 200, 3000, stdScale)
                .armorAliases(List.of("ConeDefault")).build());
        putZombie(basicSheet("ZombieMummyArmor2Default", "ZombiePropertySheet",
                190f, 100f, 0.185f, 400, 4000, stdScale)
                .armorAliases(List.of("BucketDefault")).build());
        putZombie(basicSheet("ZombieMummyArmor4Default", "ZombiePropertySheet",
                190f, 100f, 0.185f, 700, 3000, stdScale)
                .armorAliases(List.of("BrickDefault")).build());

        // Ra Zombie (sun-stealer)
        putZombie(new ZombiePropertySheet.Builder("ZombieRaDefault", "ZombieRaProps")
                .hitPoints(190f).eatDps(100f).speed(0.2f).wavePointCost(100).weight(700)
                .canSpawnPlantFood(true).scaledProps(stdScale)
                .maxClaimedSunCurrency(250).build());

        // Explorer (torch)
        putZombie(new ZombiePropertySheet.Builder("ZombieExplorerDefault", "ZombieExplorerProps")
                .hitPoints(250f).eatDps(100f).speed(0.25f).wavePointCost(250).weight(3000)
                .canSpawnPlantFood(true).scaledProps(stdScale).maxTorchReach(37f).build());

        // TombRaiser
        putZombie(new ZombiePropertySheet.Builder("ZombieTombRaiserDefault",
                "ZombieTombRaiserProps")
                .hitPoints(380f).eatDps(100f).speed(0.185f).wavePointCost(300).weight(2000)
                .canSpawnPlantFood(true).scaledProps(stdScale)
                .ammo(5).numberOfTombsToSpawn(2).timeBetweenRaisings(6f).build());

        // Egypt Gargantuar
        putZombie(gargantuarSheet("ZombieEgyptGargantuar",
                "ZombieGargantuarProps", 3600f, 1500f, 0.24f, 1500,
                "ZombieEgyptImpDefault", stdScale));

        // Egypt Imp
        putZombie(new ZombiePropertySheet.Builder("ZombieEgyptImpDefault", "ZombiePropertySheet")
                .hitPoints(190f).eatDps(100f).speed(0.22f).wavePointCost(100).weight(1000)
                .canSpawnPlantFood(false).scaledProps(stdScale).imp(true).build());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Frostbite Caves
    // ─────────────────────────────────────────────────────────────────────────

    private void initIceCaveZombies() {
        List<ScaledProp> stdScale = standardScale();

        putZombie(basicSheet("ZombieIceageDefault",       "ZombiePropertySheet",
                190f, 100f, 0.185f, 100, 1000, stdScale));
        putZombie(basicSheet("ZombieIceageArmor1Default", "ZombiePropertySheet",
                190f, 100f, 0.185f, 200, 3000, stdScale)
                .armorAliases(List.of("ConeDefault")).build());
        putZombie(basicSheet("ZombieIceageArmor2Default", "ZombiePropertySheet",
                190f, 100f, 0.185f, 400, 4000, stdScale)
                .armorAliases(List.of("BucketDefault")).build());

        // Hunter (ranged snowball thrower)
        putZombie(new ZombiePropertySheet.Builder("ZombieIceAgeHunter",
                "ZombieIceAgeHunterProps")
                .hitPoints(700f).eatDps(100f).speed(0.12f).wavePointCost(500).weight(3500)
                .canSpawnPlantFood(true).scaledProps(stdScale)
                .snowballsPerBarrage(3).farAttackRange(4).nearAttackRange(1).build());

        // Troglobite (pushes ice blocks)
        putZombie(new ZombiePropertySheet.Builder("ZombieIceAgeTroglobite",
                "ZombieIceAgeTroglobiteProps")
                .hitPoints(470f).eatDps(100f).speed(0.185f).wavePointCost(600).weight(3500)
                .canSpawnPlantFood(true).scaledProps(stdScale)
                .impType("ZombieIceageImpDefault")
                .numberOfIceblocksToSpawnWith(3).build());

        // Dodo rider (flies over obstacles)
        putZombie(new ZombiePropertySheet.Builder("ZombieIceAgeDodo",
                "ZombieIceAgeDodoProps")
                .hitPoints(490f).eatDps(100f).speed(0.3f).wavePointCost(600).weight(3500)
                .canSpawnPlantFood(true).scaledProps(stdScale).build());

        // Ice-age Gargantuar
        putZombie(gargantuarSheet("ZombieIceAgeGargantuar",
                "ZombieGargantuarProps", 3600f, 1500f, 0.24f, 1500,
                "ZombieIceageImpDefault", stdScale));

        // Ice-age Imp
        putZombie(new ZombiePropertySheet.Builder("ZombieIceageImpDefault", "ZombiePropertySheet")
                .hitPoints(190f).eatDps(100f).speed(0.22f).wavePointCost(100).weight(1000)
                .canSpawnPlantFood(false).scaledProps(stdScale).imp(true).build());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Big Wave Beach
    // ─────────────────────────────────────────────────────────────────────────

    private void initBeachZombies() {
        List<ScaledProp> stdScale = standardScale();

        putZombie(basicSheet("ZombieBeachDefault",       "ZombiePropertySheet",
                190f, 100f, 0.185f, 100, 1000, stdScale));
        putZombie(basicSheet("ZombieBeachArmor1Default", "ZombiePropertySheet",
                190f, 100f, 0.185f, 200, 3000, stdScale)
                .armorAliases(List.of("ConeDefault")).build());
        putZombie(basicSheet("ZombieBeachArmor2Default", "ZombiePropertySheet",
                190f, 100f, 0.185f, 400, 4000, stdScale)
                .armorAliases(List.of("BucketDefault")).build());

        // Snorkel (underwater movement)
        putZombie(new ZombiePropertySheet.Builder("ZombieBeachSnorkel",
                "ZombieBeachSnorkelProps")
                .hitPoints(350f).eatDps(100f).speed(0.185f).wavePointCost(200).weight(3000)
                .canSpawnPlantFood(true).scaledProps(stdScale).build());

        // Fisherman (hooks plants)
        putZombie(new ZombiePropertySheet.Builder("ZombieBeachFisherman",
                "ZombieBeachFishermanProps")
                .hitPoints(1000f).eatDps(100f).speed(0.185f).wavePointCost(700).weight(2500)
                .canSpawnPlantFood(true).scaledProps(stdScale).build());

        // Octopus (throws octopi at plants)
        putZombie(new ZombiePropertySheet.Builder("ZombieBeachOctopus",
                "ZombieBeachOctopusProps")
                .hitPoints(910f).eatDps(100f).speed(0.12f).wavePointCost(900).weight(3500)
                .canSpawnPlantFood(true).scaledProps(stdScale).build());

        // Beach Gargantuar
        putZombie(gargantuarSheet("ZombieBeachGargantuar",
                "ZombieGargantuarProps", 3600f, 1500f, 0.24f, 1500,
                "ZombieBeachImpDefault", stdScale));

        // Beach Imp
        putZombie(new ZombiePropertySheet.Builder("ZombieBeachImpDefault", "ZombiePropertySheet")
                .hitPoints(190f).eatDps(100f).speed(0.22f).wavePointCost(100).weight(1000)
                .canSpawnPlantFood(false).scaledProps(stdScale).imp(true).build());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Dark Ages
    // ─────────────────────────────────────────────────────────────────────────

    private void initDarkAgesZombies() {
        List<ScaledProp> stdScale = standardScale();

        putZombie(basicSheet("ZombieDarkDefault",       "ZombiePropertySheet",
                190f, 100f, 0.185f, 100, 1000, stdScale));
        putZombie(basicSheet("ZombieDarkArmor1Default", "ZombiePropertySheet",
                190f, 100f, 0.185f, 200, 3000, stdScale)
                .armorAliases(List.of("ConeDefault")).build());
        putZombie(basicSheet("ZombieDarkArmor2Default", "ZombiePropertySheet",
                190f, 100f, 0.185f, 400, 4000, stdScale)
                .armorAliases(List.of("BucketDefault")).build());

        // Knight (shoulder + crown)
        putZombie(new ZombiePropertySheet.Builder("ZombieDarkArmor3Default", "ZombiePropertySheet")
                .hitPoints(190f).eatDps(100f).speed(0.185f).wavePointCost(550).weight(4500)
                .canSpawnPlantFood(true).scaledProps(stdScale)
                .armorAliases(List.of("ShoulderArmorDefault","CrownDefault")).build());

        putZombie(basicSheet("ZombieDarkArmor4Default", "ZombiePropertySheet",
                190f, 100f, 0.185f, 700, 3000, stdScale)
                .armorAliases(List.of("BrickDefault")).build());

        // Wizard (transforms plants to cats)
        putZombie(new ZombiePropertySheet.Builder("ZombieWizardDefault", "ZombieDarkWizardProps")
                .hitPoints(490f).eatDps(100f).speed(0.12f).wavePointCost(800).weight(3500)
                .canSpawnPlantFood(true).scaledProps(stdScale).build());

        // Juggler (reflects projectiles)
        putZombie(new ZombiePropertySheet.Builder("ZombieDarkJugglerDefault",
                "ZombieDarkJugglerProps")
                .hitPoints(420f).eatDps(100f).speed(0.2f).wavePointCost(450).weight(3500)
                .canSpawnPlantFood(true).scaledProps(stdScale).build());

        // King (buffs nearby dark zombies)
        putZombie(new ZombiePropertySheet.Builder("ZombieDarkKing", "ZombieDarkKingProps")
                .hitPoints(1000f).eatDps(100f).speed(0.185f).wavePointCost(750).weight(2000)
                .canSpawnPlantFood(true).scaledProps(stdScale).build());

        // Dark Gargantuar
        putZombie(gargantuarSheet("ZombieDarkGargantuar",
                "ZombieGargantuarProps", 3600f, 1500f, 0.24f, 1500,
                "ZombieDarkImpDefault", stdScale));

        // Dark Imp
        putZombie(new ZombiePropertySheet.Builder("ZombieDarkImpDefault", "ZombiePropertySheet")
                .hitPoints(190f).eatDps(100f).speed(0.22f).wavePointCost(150).weight(2000)
                .canSpawnPlantFood(false).scaledProps(stdScale).imp(true).build());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Zombosses
    // ─────────────────────────────────────────────────────────────────────────

    private void initZombosses() {
        // Zomboss sheets use their own HP pools (sum of all stages) and fixed speed.
        // Skills are handled by dedicated Zomboss AI, not the standard skill list.
        List<ScaledProp> constScale = constantScale();

        putZombie(new ZombiePropertySheet.Builder("ZombieZombossMechEgypt",
                "ZombieZombossMechEgyptProps")
                .hitPoints(18500f).eatDps(0f).speed(0.1f).wavePointCost(0).weight(3000)
                .canSpawnPlantFood(false).scaledProps(constScale).build());

        putZombie(new ZombiePropertySheet.Builder("ZombieZombossMechDark",
                "ZombieZombossMechDarkProps")
                .hitPoints(27000f).eatDps(0f).speed(0.1f).wavePointCost(0).weight(3000)
                .canSpawnPlantFood(false).scaledProps(constScale).build());

        putZombie(new ZombiePropertySheet.Builder("ZombieZombossMechCowboy",
                "ZombieZombossMechCowboyProps")
                .hitPoints(25500f).eatDps(0f).speed(0.1f).wavePointCost(0).weight(3000)
                .canSpawnPlantFood(false).scaledProps(constScale).build());

        putZombie(new ZombiePropertySheet.Builder("ZombieZombossMechPirate",
                "ZombieZombossMechPirateProps")
                .hitPoints(26500f).eatDps(0f).speed(0.1f).wavePointCost(0).weight(3000)
                .canSpawnPlantFood(false).scaledProps(constScale).build());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Builder helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Convenience: build + register a zombie sheet. */
    private void putZombie(ZombiePropertySheet sheet) {
        zombieSheets.put(sheet.getAlias(), sheet);
    }

    /** Convenience: build + register from a Builder (already called .build() on it). */
    private void putZombie(ZombiePropertySheet.Builder b) {
        putZombie(b.build());
    }

    /**
     * Returns a Builder for a basic (no armour, no skills) zombie sheet,
     * pre-wired with standard scaling for HP and EatDPS.
     * Caller can chain additional setters before calling {@link ZombiePropertySheet.Builder#build()}.
     */
    private ZombiePropertySheet.Builder basicSheet(String alias, String objClass,
            float hp, float dps, float speed, int waveCost, int weight,
            List<ScaledProp> scaledProps) {
        return new ZombiePropertySheet.Builder(alias, objClass)
                .hitPoints(hp).eatDps(dps).speed(speed)
                .wavePointCost(waveCost).weight(weight)
                .canSpawnPlantFood(true).scaledProps(scaledProps);
    }

    /** Builds and registers a complete Gargantuar-type sheet. */
    private ZombiePropertySheet gargantuarSheet(String alias, String objClass,
            float hp, float smash, float speed, int waveCost,
            String impAlias, List<ScaledProp> scaledProps) {
        return new ZombiePropertySheet.Builder(alias, objClass)
                .hitPoints(hp).eatDps(0f).speed(speed)
                .wavePointCost(waveCost).weight(3000)
                .canSpawnPlantFood(false).scaledProps(scaledProps)
                .impType(impAlias).healthThresholdToThrowImp(0.5f)
                .smashDamage(smash).smashDuration(2f).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ScaledProp presets
    // ─────────────────────────────────────────────────────────────────────────

    /** Standard scaling list as found in most zombie JSON sheets. */
    private List<ScaledProp> standardScale() {
        return List.of(
            new ScaledProp("Hitpoints", ScaledProp.FORMULA_STANDARD, 1.3f, 0.05f),
            new ScaledProp("EatDPS",    ScaledProp.FORMULA_STANDARD, 1.3f, 0.05f),
            new ScaledProp("Speed",     ScaledProp.FORMULA_CONSTANT, 0f,   0f),
            new ScaledProp("WavePointCost", ScaledProp.FORMULA_CONSTANT, 0f, 0f)
        );
    }

    /** All-constant scaling (used for Zomboss stages). */
    private List<ScaledProp> constantScale() {
        return List.of(
            new ScaledProp("Hitpoints",    ScaledProp.FORMULA_CONSTANT, 0f, 0f),
            new ScaledProp("EatDPS",       ScaledProp.FORMULA_CONSTANT, 0f, 0f),
            new ScaledProp("Speed",        ScaledProp.FORMULA_CONSTANT, 0f, 0f),
            new ScaledProp("WavePointCost",ScaledProp.FORMULA_CONSTANT, 0f, 0f)
        );
    }
}
