package com.pvz.models.engine;

public interface TickAware {
    void enter();
    void update(float dt);
    default FrameConfig draw() { return null; };
    void dispose();
}
