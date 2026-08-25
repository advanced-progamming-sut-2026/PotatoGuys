package com.pvz.models.entities.plants.config;

import com.badlogic.gdx.math.Vector2;

public class SplitPeaConfig extends PlantActionConfig {
    public float intervalSeconds = 1.5f;
    public float damage = 20.0f;

    public String attackFrontClip = "attack";
    public String attackBothClip = "attack2";
    public String attackBackClip = "attack3";

    public float projectileSpeed = 180.0f;
    public float forwardOffsetX = 50.0f;
    public float forwardOffsetY = 15.0f;
    public float backwardOffsetX = -50.0f;
    public float backwardOffsetY = 20.0f;
}
