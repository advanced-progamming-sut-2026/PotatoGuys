package Models;

import java.util.List;

public interface TickAware {
    default void onFirstTick(){

    }

    void onTick();
}
