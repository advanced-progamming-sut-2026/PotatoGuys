package pvz.Models.Engine;

public interface TickAware {
    void onFirstTick();
    void onTick();
    void dispose();
}
