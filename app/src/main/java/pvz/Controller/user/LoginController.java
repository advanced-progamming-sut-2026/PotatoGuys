package pvz.Controller.user;

import java.util.HashMap;
import java.util.regex.Matcher;

import pvz.Models.AppContext;
import pvz.Models.User.User;
import pvz.Utils.PasswordUtils;
import pvz.Utils.SaveManager;
import pvz.View.ForgotPasswordMenu;
import pvz.View.LoginMenu;
import pvz.View.MainMenu;
import pvz.View.Menu;
import pvz.View.RegisterMenu;
import pvz.View.ResetPasswordMenu;
import pvz.View.Result;

public class LoginController {
    private User currentUser;

    public Result enterMenu(Matcher matcher) {
        String menuName = matcher.group("menuName");
        menuName = menuName.replaceAll("  ", "");
        Menu nextMenu = null;
        switch (menuName.trim()) {
            case "main":
                nextMenu = new MainMenu();
                break;
            default:
                nextMenu = new LoginMenu();
                return new Result("You cannot enter this menu." , nextMenu);
        }
        
        return new Result("Enterned " + nextMenu.getName() , nextMenu);
    }

    public Result login(Matcher matcher) { 
        String username = matcher.group("username");
        String password = matcher.group("password");

        HashMap<String , String> usernames = SaveManager.getInstance().load("users/username.json", HashMap.class);
        String Id = usernames.get(username);
        if(Id == null){
            return new Result("Username is incorrect!");
        }
        User currentUser = SaveManager.getInstance().load("users/" + Id + ".json", User.class);
        if(!PasswordUtils.verifyPassword(password, currentUser.getPasswordHash())){
            return new Result("Password is incorrect!");
        }

        AppContext.getInstance().setCurrentUser(currentUser);
        return new Result("Wellcome " + currentUser.getNickName() + ".", new MainMenu()); 
    }
    public Result forgetPassword(Matcher matcher) {
        String username = matcher.group("username");
        String email = matcher.group("email");
        
        currentUser = SaveManager.getInstance().load("users/" + username + ".json", User.class);
        if(!currentUser.getEmail().equals(email)){
            return new Result("Email is incorrect!");
        }

        return new Result("Answer to this question:" + currentUser.getSecurityQuestion(), new ForgotPasswordMenu(this)); 
    }
    public Result answer(Matcher matcher) { 
        String answer = matcher.group("answer");

        if(!currentUser.getSecurityAnswer().equals(answer)){
            return new Result("Answer is incorrect!");
        }
        return new Result("Answer is correct!" , new ResetPasswordMenu(this)); 
    }
    public Result resetPassword(Matcher matcher) {
        String newPassword = matcher.group("newPassword");
        currentUser.setPasswordHash(PasswordUtils.hashPassword(newPassword));
        SaveManager.getInstance().save(currentUser , "users/" + currentUser.getUsername() + ".json");
        return new Result("Password reset successfully!", new LoginMenu());
    }
    public Result exit(Matcher matcher) {
        Menu nextMenu = new RegisterMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
}
