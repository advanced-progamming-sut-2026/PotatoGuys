package com.pvz.controller.user;

import java.util.HashMap;
import java.util.regex.Matcher;

import com.pvz.enums.SecurityQuestions;
import com.pvz.models.AppContext;
import com.pvz.models.user.User;
import com.pvz.utils.PasswordUtils;
import com.pvz.utils.SaveManager;

public class LoginController {
/*
    public User currentUser;

    public Result enterMenu(Matcher matcher) {
        String menuName = matcher.group("menuName");
        menuName = menuName.replaceAll("  ", "");
        Menu nextMenu = null;
        switch (menuName.trim()) {
            case "main":
                nextMenu = new pvz.view.OldMainMenu();
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

        return new Result("Welcome " + currentUser.getNickName() + ".", new pvz.view.OldMainMenu());
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
        SaveManager.getInstance().save(currentUser, "users/" + currentUser.getId() + ".json");
        return new Result("Password reset successfully!", new LoginMenu());
    }

    public Result exit(Matcher matcher) {
        Menu nextMenu = new RegisterMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
*/
}
