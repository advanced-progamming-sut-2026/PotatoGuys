package pvz.View;

import java.util.Scanner;
import java.util.regex.Matcher;

import pvz.Enums.Commands.MainMenuCommand;

public class MenuManager {
    private Menu currentMenu;

    public void handleInput(String Input){
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();
            Matcher matcher = MainMenuCommand.SHOW_CURRENT.getMatcher(input);
            if(matcher != null){
                System.out.println(currentMenu.getName());
                continue;
            }
            Result result = currentMenu.handleInput(input);
            System.out.println(result.getMessage());

            if (result.getNextMenu()!=null) {
                setCurrentMenu(result.getNextMenu());
            }
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