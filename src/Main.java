import View.MenuManager;
import View.RegisterMenu;

public class Main {
    public static void main(String[] args) {
        MenuManager menuManager = new MenuManager();
        menuManager.setCurrentMenu(new RegisterMenu());
        menuManager.handleInput(null);
    }
}
    
    
