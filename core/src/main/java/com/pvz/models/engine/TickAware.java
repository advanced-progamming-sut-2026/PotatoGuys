package com.pvz.models.engine;

import java.util.List;

public interface TickAware {
    void enter();

    void update(float dt);

    default List<FrameConfig> draw() {
        return null;
    };

    void dispose();
}
