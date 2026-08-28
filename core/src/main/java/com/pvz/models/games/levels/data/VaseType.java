package com.pvz.models.games.levels.data;

public enum VaseType {
    NORMAL, // کوزه معمولی (شانس تصادفی زامبی، گیاه، خورشید یا پوچ)
    PLANT, // کوزه تضمینی کارت گیاه
    GARGANTUAR; // کوزه تضمینی غول

    public static VaseType getTypeByName(String type) {
        for (VaseType v : values()) {
            if (v.name().equalsIgnoreCase(type))
                return v;
        }
        return NORMAL;
    }
}
