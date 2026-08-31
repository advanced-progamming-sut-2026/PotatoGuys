package com.pvz.models;

public class Constants {
    public static final String SAVE_PATH = "core/src/main/java/com/pvz/saves/";
    public static final int DEFAULT_ROWS = 5;
    public static final int DEFAULT_COLS = 9;
    public static final int DEFAULT_INITIAL_SUN = 75;
    public static final int DEFAULT_GRAVE_HP= 700;
    public static final float TICK_PER_SECOND = 10f;

    /** Static, read-only game-data resource (not a user save file). */
    public static final String PLANT_ACTIONS_PATH = "core/src/main/java/com/resources/plant_actions.json";

    /** Static, read-only game-data resource (not a user save file).
     *  Single source of truth for zombies: stats, scaling presets, armour
     *  definitions, animations and skills. The old {@code zombie_profiles.json}
     *  was merged into this file and is no longer read. */
    public static final String ZOMBIE_ACTIONS_PATH = "core/src/main/java/com/resources/zombie_actions.json";
}
