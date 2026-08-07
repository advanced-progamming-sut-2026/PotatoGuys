package com.pvz.models.engine;

public interface TickAware {
    void enter();
    void update(float dt);
    default void draw() {};
    void dispose();
}
