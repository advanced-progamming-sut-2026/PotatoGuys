package com.pvz.models.entities.projectile;

public enum ProjectileType {
    PEA("768/INITIAL/EFFECTS/T_PEA_PROJECTILE/T_PEA_PROJECTILE.PAM", "animation",
        "768/INITIAL/EFFECTS/SPLAT_PEA/SPLAT_PEA.PAM", "animation", 1f),
    SNOW_PEA("768/INITIAL/EFFECTS/T_SNOW_PEA/T_SNOW_PEA.PAM", "animation",
        "768/INITIAL/EFFECTS/SPLAT_SNOW_PEA/SPLAT_SNOW_PEA.PAM", "animation", 1f),
    FIRE_PEA("768/INITIAL/EFFECTS/T_FIRE_PEA/T_FIRE_PEA.PAM", "animation",
        "768/INITIAL/EFFECTS/SPLAT_FIRE_PEA_BLUE/SPLAT_FIRE_PEA_BLUE.PAM", "animation", 1f),
    GOO_PEA("768/INITIAL/EFFECTS/GOOPEASHOOTER_PROJECTILES/GOOPEASHOOTER_PROJECTILES.PAM", "projectile_t1",
        null, null, 1f),
    SPIKE("768/INITIAL/EFFECTS/CACTUS_PROJECTILE/CACTUS_PROJECTILE.PAM", "idle",
        null, null, 1f),
    FUME("768/INITIAL/EFFECTS/FUMESHROOM_BUBBLES/FUMESHROOM_BUBBLES.PAM", "special",
        null, null, 1f),
    SPORE("768/INITIAL/EFFECTS/T_SPORESHROOM_PROJECTILE/T_SPORESHROOM_PROJECTILE.PAM", "animation",
        null, null, 1f),
    STAR("768/INITIAL/EFFECTS/T_STARFRUIT_PROJECTILE/T_STARFRUIT_PROJECTILE.PAM", "animation",
        null, null, 1f),
    ROTOBAGA_PROJECTILE("768/FULL/EFFECTS/ROTORUTABAGA_PROJECTILE2/ROTORUTABAGA_PROJECTILE2.PAM", "animation",
        null, null, 1f),
    CITRON_BALL("768/FULL/EFFECTS/CITRON_CITRUS_ORB/CITRON_CITRUS_ORB.PAM", "Citron_Citrus_Orb",
        null, null, 1f),
    BULB_CYAN("768/FULL/EFFECTS/BOWLINGBULB_PROJECTILE1/BOWLINGBULB_PROJECTILE1.PAM", "animation",
        null, null, 1f),
    BULB_BLUE("768/FULL/EFFECTS/BOWLINGBULB_PROJECTILE2/BOWLINGBULB_PROJECTILE2.PAM", "animation",
        null, null, 1f),
    HOMING_BOLT("768/INITIAL/EFFECTS/HOMING_THISTLE_PROJECTILE/HOMING_THISTLE_PROJECTILE.PAM", "animation",
        null, null, 1f),
    BULB_ORANGE("768/FULL/EFFECTS/BOWLINGBULB_PROJECTILE3/BOWLINGBULB_PROJECTILE3.PAM", "animation",
        null, null, 1f),
    PEPPER_BALL("768/FULL/EFFECTS/PEPPERPULT_PROJECTILE/PEPPERPULT_PROJECTILE.PAM", "animation",
        "768/FULL/EFFECTS/PEPPERPULT_PROJECTILE_SPLAT/PEPPERPULT_PROJECTILE_SPLAT.PAM", "animation", 1f),
    MELON("768/INITIAL/EFFECTS/T_MELON_PROJECTILE/T_MELON_PROJECTILE.PAM", "animation", 0.45f,
        "768/INITIAL/EFFECTS/T_SPLAT_MELONPULT/T_SPLAT_MELONPULT.PAM", "animation", 1f),
    WINTER_MELON("768/FULL/EFFECTS/T_WINTERMELON_PROJECTILE/T_WINTERMELON_PROJECTILE.PAM", "animation", 0.45f,
        "768/FULL/EFFECTS/T_SPLAT_WINTERMELON/T_SPLAT_WINTERMELON.PAM", "animation", 1f),
    CABBAGE("768/INITIAL/EFFECTS/T_CABBAGEPULT_PROJECTILE/T_CABBAGEPULT_PROJECTILE.PAM", "animation", 0.45f,
        "768/INITIAL/EFFECTS/SPLAT_CABBAGEPULT/SPLAT_CABBAGEPULT.PAM", "animation", 1f),
    KERNEL("768/INITIAL/EFFECTS/T_KERNALPULT_PROJECTILE/T_KERNALPULT_PROJECTILE.PAM", "animation",
        "768/INITIAL/EFFECTS/SPLAT_KERNALPULT_KERNAL/SPLAT_KERNALPULT_KERNAL.PAM", "animation", 1f),
    BUTTER("768/INITIAL/EFFECTS/T_KERNALPULT_PROJECTILE/T_KERNALPULT_PROJECTILE.PAM", "animation",
        "768/INITIAL/EFFECTS/SPLAT_KERNALPULT_BUTTER/SPLAT_KERNALPULT_BUTTER.PAM", "animation", 1f);


    public String path;
    public String lable;
    public float scale = 1f;
    public String splatPamPath;
    public String splatClip;
    public float splatScale;

    ProjectileType(String animPath, String lable, String splatPamPath, String splatClip, float splatScale) {
        this(animPath, lable, 1f, splatPamPath, splatClip, splatScale);
    }

    ProjectileType(String animPath, String lable, float projectileScale,
                   String splatPamPath, String splatClip, float splatScale) {
        this.path = animPath;
        this.lable = lable;
        this.scale = projectileScale;
        this.splatPamPath = splatPamPath;
        this.splatClip = splatClip;
        this.splatScale = splatScale;
    }

    public boolean hasSplat() {
        return splatPamPath != null;
    }
}
