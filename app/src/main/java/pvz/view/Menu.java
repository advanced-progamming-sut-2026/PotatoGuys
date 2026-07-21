package pvz.view;

public interface Menu {
    Result handleInput(String input);
    String getName();
    Result onEnter();
}
