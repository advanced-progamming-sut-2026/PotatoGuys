package pvz.View;

public class Result {
    private String message;
    private Menu nextMenu;

    //print message and stay in the current menu
    public Result(String message){
        this.message=message;
        this.nextMenu=null;
    }

    //print message and go to next menu
    public Result(String message, Menu nextMenu) {
        this.message = message;
        this.nextMenu = nextMenu;
    }

    //print nothing and go to next menu
    public Result(Menu nextMenu){
        this.message="";
        this.nextMenu=nextMenu;
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