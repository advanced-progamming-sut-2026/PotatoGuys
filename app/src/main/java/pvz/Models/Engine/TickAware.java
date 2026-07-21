package pvz.models.engine;

public interface TickAware {
    void enter();
    void update();
    void dispose();
}
