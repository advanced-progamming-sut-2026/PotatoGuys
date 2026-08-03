package com.pvz.models.entities.plants.actions.shooters;

public enum ShooterPattern {
    FORWARD,          // شلیک مستقیم به جلو (Peashooter, Repeater, Snow Pea, Cactus و...)
    BIDIRECTIONAL,    // جلو و عقب (Split Pea)
    THREE_LANE,       // ۳ لاین همزمان: بالا، مستقیم، پایین (Threepeater)
    FIVE_WAY_STAR,    // ۵ جهت ستاره‌ای (Starfruit)
    DIAGONAL_FOUR     // ۴ جهت قطری/مورب (Rotobaga)
}
