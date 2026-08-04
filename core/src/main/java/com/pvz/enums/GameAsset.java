package com.pvz.enums;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

public enum GameAsset {
    MAIN_MENU_BG("textures/backgrounds/MainMenu.png", Texture.class),
    LOGO("textures/pvz2_logo_horizontal.png", Texture.class);

    public final String path;
    public final Class<?> type;

    GameAsset(String path, Class<?> type) {
        this.path = path;
        this.type = type;
    }

    // متد کمکی برای لود کردن
    public void load(AssetManager manager) {
        manager.load(path, type);
    }

    // متد کمکی برای دریافت (با ریخت‌casting اتوماتیک)
    @SuppressWarnings("unchecked")
    public <T> T get(AssetManager manager) {
        return (T) manager.get(path, type);
    }
}
