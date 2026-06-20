package View;

import java.util.Scanner;

public class MenuManager {
    private Menu currentMenu;

    public void handleInput(String input){
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            Result result = currentMenu.handleInput(scanner.nextLine().trim());
            System.out.println(result.getMessage());
            setCurrentMenu(result.getNextMenu());
        }

        scanner.close();
    }

    public Menu getCurrentMenu() {
        return currentMenu;
    }
    public void setCurrentMenu(Menu currentMenu) {
        this.currentMenu = currentMenu;
    }
}