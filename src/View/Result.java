package View;

public class Result {
    private String message;
    private Menu nextMenu;
    public Result(String message, Menu nextMenu) {
        this.message = message;
        this.nextMenu = nextMenu;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public Menu getNextMenu() {
        return nextMenu;
    }
    public void setNextMenu(Menu nextMenu) {
        this.nextMenu = nextMenu;
    }
}