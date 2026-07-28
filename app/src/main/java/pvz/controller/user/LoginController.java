package pvz.controller.user;

import java.util.HashMap;
import java.util.regex.Matcher;

import pvz.enums.SecurityQuestions;
import pvz.models.AppContext;
import pvz.models.user.User;
import pvz.utils.PasswordUtils;
import pvz.utils.SaveManager;
import pvz.view.ForgotPasswordMenu;
import pvz.view.LoginMenu;
import pvz.view.MainMenu;
import pvz.view.Menu;
import pvz.view.RegisterMenu;
import pvz.view.ResetPasswordMenu;
import pvz.view.Result;

public class LoginController {
    public User currentUser;

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
                return new Result("You cannot enter this menu.", nextMenu);
        }

        return new Result("Enterned " + nextMenu.getName(), nextMenu);
    }

    public Result login(Matcher matcher) {
        String username = matcher.group("username");
        String password = matcher.group("password");

        HashMap<String, String> usernames = SaveManager.getInstance().load("users/username.json", HashMap.class);
        if(usernames == null) {
            return new Result("No users found. Please register first.");
        }
        String id = usernames.get(username);
        if (id == null) {
            return new Result("Username is incorrect!");
        }
        User currentUser = SaveManager.getInstance().load("users/" + id + ".json", User.class);
        if (!PasswordUtils.verifyPassword(password, currentUser.getPasswordHash())) {
            return new Result("Password is incorrect!");
        }

        AppContext.getInstance().setCurrentUser(currentUser);
        currentUser.refreshQuestLog();
        currentUser.saveUser();

        if (matcher.group("stayLoggedIn") != null) {
            SaveManager.getInstance().save(username, "session.json");
        }

        return new Result("Welcome " + currentUser.getNickName() + ".", new MainMenu());
    }

    public Result forgetPassword(Matcher matcher) {
        String username = matcher.group("username");
        String email = matcher.group("email");

        HashMap<String, String> usernames = SaveManager.getInstance().load("users/username.json", HashMap.class);
        if(usernames == null) {
            return new Result("No users found. Please register first.");
        }
        String id = usernames.get(username);
        if (id == null) {
            return new Result("Username is incorrect!");
        }
        User currentUser = SaveManager.getInstance().load("users/" + id + ".json", User.class);
        if (!currentUser.getEmail().equals(email)) {
            return new Result("Email is incorrect!");
        }

        return new Result("Answer to this question:" + SecurityQuestions.QUESTIONS.get(Integer.parseInt(currentUser.getSecurityQuestion())), new ForgotPasswordMenu(currentUser));
    }

    public Result answer(Matcher matcher) {
        String answer = matcher.group("answer");

        if (!currentUser.getSecurityAnswer().equals(answer)) {
            return new Result("Answer is incorrect!");
        }
        return new Result("Answer is correct!", new ResetPasswordMenu(this));
    }

    public Result resetPassword(Matcher matcher) {
        String newPassword = matcher.group("newPassword");
        currentUser.setPasswordHash(PasswordUtils.hashPassword(newPassword));
        SaveManager.getInstance().save(currentUser, "users/" + currentUser.getUsername() + ".json");
        return new Result("Password reset successfully!", new LoginMenu());
    }

    public Result exit(Matcher matcher) {
        Menu nextMenu = new RegisterMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
}
