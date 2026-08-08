package com.pvz.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;

import java.util.HashMap;
import java.util.Map;

public class AvatarImages {

    private static final Map<String, Texture> cache = new HashMap<>();

    private AvatarImages() {
    }

    public static Texture getTexture(String path) {
        Texture texture = cache.get(path);
        if (texture == null) {
            if (path == null || !Gdx.files.internal(path).exists()) {
                return createPlaceholder();
            }
            texture = new Texture(Gdx.files.internal(path));
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            cache.put(path, texture);
        }
        return texture;
    }

    private static Texture placeholder;

    private static Texture createPlaceholder() {
        if (placeholder == null) {
            Pixmap pixmap = new Pixmap(128, 128, Pixmap.Format.RGBA8888);
            pixmap.setColor(0.45f, 0.5f, 0.55f, 1f);
            pixmap.fillCircle(64, 64, 62);
            placeholder = new Texture(pixmap);
            pixmap.dispose();
        }
        return placeholder;
    }

    /**
     * Creates a circular avatar texture with soft (feathered) edges from the
     * given source texture. The resulting texture is a circle whose alpha fades
     * out near the border so it blends nicely over any background.
     */
    public static Texture featheredCircle(Texture source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int size = Math.min(width, height);
        float centerX = width / 2f;
        float centerY = height / 2f;
        float radius = size / 2f;
        float feather = Math.max(1f, radius * 0.15f);

        TextureData textureData = source.getTextureData();
        if (!textureData.isPrepared()) {
            textureData.prepare();
        }
        Pixmap sourcePixmap = textureData.consumePixmap();
        Pixmap result = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        Pixmap mask = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        mask.setBlending(Pixmap.Blending.None);
        mask.setColor(1, 1, 1, 1);
        mask.fill();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float dx = x - centerX;
                float dy = y - centerY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float alpha;
                if (dist >= radius) {
                    alpha = 0f;
                } else if (dist >= radius - feather) {
                    alpha = (radius - dist) / feather;
                } else {
                    alpha = 1f;
                }
                int rgba = mask.getPixel(x, y);
                int a = (int) (alpha * 255) & 0xff;
                mask.drawPixel(x, y, (rgba & 0x00ffffff) | (a << 24));
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int maskPixel = mask.getPixel(x, y);
                int maskAlpha = (maskPixel >>> 24) & 0xff;
                if (maskAlpha == 0) {
                    result.drawPixel(x, y, 0);
                    continue;
                }
                int srcPixel = sourcePixmap.getPixel(x, y);
                int srcAlpha = (srcPixel >>> 24) & 0xff;
                int alpha = (srcAlpha * maskAlpha) / 255;
                result.drawPixel(x, y, (srcPixel & 0x00ffffff) | (alpha << 24));
            }
        }

        Texture output = new Texture(result);
        output.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        sourcePixmap.dispose();
        mask.dispose();
        result.dispose();
        return output;
    }

    public static void dispose() {
        for (Texture texture : cache.values()) {
            texture.dispose();
        }
        cache.clear();
    }
}
