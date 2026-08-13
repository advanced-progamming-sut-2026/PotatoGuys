package com.pvz.models.entities.projectile;

public enum ProjectileType {
    PEA("768/INITIAL/EFFECTS/T_PEA_PROJECTILE/T_PEA_PROJECTILE.PAM", "animation"),
    SNOW_PEA("768/INITIAL/EFFECTS/T_SNOW_PEA/T_SNOW_PEA.PAM", "animation"),
    FIRE_PEA("768/INITIAL/EFFECTS/T_FIRE_PEA/T_FIRE_PEA.PAM", "animation"),
    GOO_PEA("768/INITIAL/EFFECTS/GOOPEASHOOTER_PROJECTILES/GOOPEASHOOTER_PROJECTILES.PAM", "projectile_t1"),
    SPIKE("768/INITIAL/EFFECTS/CACTUS_PROJECTILE/CACTUS_PROJECTILE.PAM", "idle"),
    FUME("768/INITIAL/EFFECTS/FUMESHROOM_BUBBLES/FUMESHROOM_BUBBLES.PAM", "special"),
    SPORE("768/INITIAL/EFFECTS/T_SPORESHROOM_PROJECTILE/T_SPORESHROOM_PROJECTILE.PAM", "animation"),
    STAR("768/INITIAL/EFFECTS/T_STARFRUIT_PROJECTILE/T_STARFRUIT_PROJECTILE.PAM", "animation"),
    ROTOBAGA_PROJECTILE("768/FULL/EFFECTS/ROTORUTABAGA_PROJECTILE2/ROTORUTABAGA_PROJECTILE2.PAM", "animation"),
    CITRON_BALL("768/FULL/EFFECTS/CITRON_CITRUS_ORB/CITRON_CITRUS_ORB.PAM", "Citron_Citrus_Orb"),
    BULB_CYAN("768/FULL/EFFECTS/BOWLINGBULB_PROJECTILE1/BOWLINGBULB_PROJECTILE1.PAM", "animation"),
    BULB_BLUE("768/FULL/EFFECTS/BOWLINGBULB_PROJECTILE2/BOWLINGBULB_PROJECTILE2.PAM", "animation"),
    HOMING_BOLT("768/INITIAL/EFFECTS/HOMING_THISTLE_PROJECTILE/HOMING_THISTLE_PROJECTILE.PAM", "animation"),
    BULB_ORANGE("768/FULL/EFFECTS/BOWLINGBULB_PROJECTILE3/BOWLINGBULB_PROJECTILE3.PAM", "animation"),

    // --- New for the projectile State Pattern pass ---
    PEPPER_BALL("768/FULL/EFFECTS/PEPPERPULT_PROJECTILE/PEPPERPULT_PROJECTILE.PAM", "animation"),   // Pepper-pult: lobbed, area fire effect
    MELON("768/INITIAL/EFFECTS/T_MELON_PROJECTILE/T_MELON_PROJECTILE.PAM", "animation" , 0.45f),         // Melon-pult: lobbed, area effect
    WINTER_MELON("768/FULL/EFFECTS/T_WINTERMELON_PROJECTILE/T_WINTERMELON_PROJECTILE.PAM", "animation" , 0.45f),  // Winter Melon-pult: lobbed, area effect + chill
    CABBAGE("768/INITIAL/EFFECTS/T_CABBAGEPULT_PROJECTILE/T_CABBAGEPULT_PROJECTILE.PAM", "animation", 0.45f),  // Cabbage-pult: lobbed, single target (scaled down, the PAM art is very large)
    KERNEL("768/INITIAL/EFFECTS/T_KERNALPULT_PROJECTILE/T_KERNALPULT_PROJECTILE.PAM", "animation"),  // Kernel-pult: lobbed kernel, single target
    BUTTER("768/INITIAL/EFFECTS/SPLAT_KERNALPULT_BUTTER/SPLAT_KERNALPULT_BUTTER.PAM", "animation");  // Kernel-pult butter: lobbed, stuns target


    public String path;
    public String lable;
    /** Uniform render scale for this projectile's animation; defaults to 1 (full size). */
    public float scale = 1f;

    ProjectileType(String animPath, String lable) {
        this(animPath, lable, 1f);
    }

    ProjectileType(String animPath, String lable, float scale) {
        this.path = animPath;
        this.lable = lable;
        this.scale = scale;
    }


}
