package pvz.models.entities.zombies;

/**
 * Enum of all zombie types defined in the game.
 * Each constant corresponds to a JSON alias registered in
 * {@link pvz.models.entities.zombies.data.ZombieRegistry}.
 *
 * <p>The {@link #alias} field is the exact string used for registry lookups
 * via {@link ZombieFactory#create(String, float, int, ZombieGameContext, int, int)}.
 */
public enum ZombieType {

    // ── All chapters ──────────────────────────────────────────────────────────
    BASIC           ("ZombieTutorialDefault"),
    CONEHEAD        ("ZombieTutorialArmor1Default"),
    BUCKETHEAD      ("ZombieTutorialArmor2Default"),
    BRICKHEAD       ("ZombieTutorialArmor4Default"),
    FLAG            ("ZombieTutorialFlagDefault"),
    IMP             ("ZombieTutorialImpDefault"),
    GARGANTUAR      ("ZombieGargantuarBasic"),
    NEWSPAPER       ("ZombieModernNewspaperDefault"),

    // ── Ancient Egypt ─────────────────────────────────────────────────────────
    MUMMY           ("ZombieMummyDefault"),
    MUMMY_CONE      ("ZombieMummyArmor1Default"),
    MUMMY_BUCKET    ("ZombieMummyArmor2Default"),
    MUMMY_BRICK     ("ZombieMummyArmor4Default"),
    RA              ("ZombieRaDefault"),
    EXPLORER        ("ZombieExplorerDefault"),
    TOMB_RAISER     ("ZombieTombRaiserDefault"),
    EGYPT_GARG      ("ZombieEgyptGargantuar"),
    EGYPT_IMP       ("ZombieEgyptImpDefault"),

    // ── Frostbite Caves ───────────────────────────────────────────────────────
    ICE_AGE         ("ZombieIceageDefault"),
    ICE_AGE_CONE    ("ZombieIceageArmor1Default"),
    ICE_AGE_BUCKET  ("ZombieIceageArmor2Default"),
    HUNTER          ("ZombieIceAgeHunter"),
    TROGLOBITE      ("ZombieIceAgeTroglobite"),
    DODO            ("ZombieIceAgeDodo"),
    ICE_AGE_GARG    ("ZombieIceAgeGargantuar"),
    ICE_AGE_IMP     ("ZombieIceageImpDefault"),

    // ── Big Wave Beach ────────────────────────────────────────────────────────
    BEACH           ("ZombieBeachDefault"),
    BEACH_CONE      ("ZombieBeachArmor1Default"),
    BEACH_BUCKET    ("ZombieBeachArmor2Default"),
    SNORKEL         ("ZombieBeachSnorkel"),
    FISHERMAN       ("ZombieBeachFisherman"),
    OCTOPUS         ("ZombieBeachOctopus"),
    BEACH_GARG      ("ZombieBeachGargantuar"),
    BEACH_IMP       ("ZombieBeachImpDefault"),

    // ── Dark Ages ─────────────────────────────────────────────────────────────
    DARK            ("ZombieDarkDefault"),
    DARK_CONE       ("ZombieDarkArmor1Default"),
    DARK_BUCKET     ("ZombieDarkArmor2Default"),
    KNIGHT          ("ZombieDarkArmor3Default"),
    DARK_BRICK      ("ZombieDarkArmor4Default"),
    WIZARD          ("ZombieWizardDefault"),
    JUGGLER         ("ZombieDarkJugglerDefault"),
    KING            ("ZombieDarkKing"),
    DARK_GARG       ("ZombieDarkGargantuar"),
    DARK_IMP        ("ZombieDarkImpDefault"),

    // ── Zombosses ─────────────────────────────────────────────────────────────
    ZOMBOSS_EGYPT   ("ZombieZombossMechEgypt"),
    ZOMBOSS_PIRATE  ("ZombieZombossMechPirate"),
    ZOMBOSS_COWBOY  ("ZombieZombossMechCowboy"),
    ZOMBOSS_DARK    ("ZombieZombossMechDark");

    private final String alias;

    ZombieType(String alias) {
        this.alias = alias;
    }

    /** The JSON alias string used for {@link ZombieFactory} and registry lookups. */
    public String getAlias() {
        return alias;
    }

    /**
     * Reverse-lookup: find the {@link ZombieType} for a given alias string.
     *
     * @return matching type, or {@code null} if the alias is not mapped to an enum constant
     */
    public static ZombieType fromAlias(String alias) {
        for (ZombieType t : values()) {
            if (t.alias.equalsIgnoreCase(alias)) return t;
        }
        return null;
    }

    public static ZombieType fromTypeString(String type){
        for (ZombieType t : values()) {
            if (t.toString().equalsIgnoreCase(type)) return t;
        }
        return null;
    }
}
