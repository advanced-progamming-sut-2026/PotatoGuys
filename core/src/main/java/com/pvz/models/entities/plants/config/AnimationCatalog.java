package com.pvz.models.entities.plants.config;

import com.badlogic.gdx.utils.Array;

public class AnimationCatalog {
    public int count;
    public Array<AnimationData> animations;

    public float getClipDuration(String path, String clipName) {
        if (animations == null || path == null || clipName == null) {
            return -1f;
        }

        for (AnimationData anim : animations) {
            if (path.equalsIgnoreCase(anim.path)) {
                if (anim.clips != null && anim.clips.containsKey(clipName)) {
                    return anim.clips.get(clipName);
                }
            }
        }

        return -1f;
    }
}