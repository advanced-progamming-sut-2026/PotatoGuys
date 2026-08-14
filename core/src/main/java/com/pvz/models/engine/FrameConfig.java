package com.pvz.models.engine;

import java.util.Map;

import com.badlogic.gdx.math.Vector2;

public class FrameConfig {
    public String pamPath;
    public String label;
    public float stateTime;
    public Vector2 position;
    public Vector2 scale;
    public Map<String, Boolean> partsVisibility;
    public boolean looping;
    public float r;
    public float g;
    public float b;
    public float a;

    public FrameConfig(String pamPath, String label, float stateTime, Vector2 position,
                       Vector2 scale, Map<String, Boolean> partsVisibility, boolean looping) {
        this.pamPath = pamPath;
        this.label = label;
        this.stateTime = stateTime;
        this.position = position;
        this.scale = scale;
        this.partsVisibility = partsVisibility;
        this.looping = looping;
        this.r=1f;
        this.g=1f;
        this.b=1f;
        this.a=1f;
    }

    public void setColor(float r, float g, float b, float a){
        this.r=r;
        this.g=g;
        this.b=b;
        this.a=a;
    }
}
