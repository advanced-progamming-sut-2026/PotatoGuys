package com.pvz.models.entities.plants.config;

import com.badlogic.gdx.utils.ObjectMap;

public class AnimationData {
    public String name;
    public String path;
    public float[] canvas;
    // ObjectMap ابزار خود LibGDX برای نگهداری key-value ها است (معادل HashMap)
    public ObjectMap<String, Float> clips;
}