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
    public boolean loop;
}
