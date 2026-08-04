package com.pvz.controller.user;

import java.util.HashMap;
import java.util.regex.Matcher;

import com.pvz.enums.SecurityQuestions;
import com.pvz.models.AppContext;
import com.pvz.models.user.User;
import com.pvz.utils.PasswordUtils;
import com.pvz.utils.SaveManager;

public class LoginController {

    public User currentUser;

    public String login(Matcher matcher) {
        String username = matcher.group("username");
        String password = matcher.group("password");

        HashMap<String, String> usernames = SaveManager.getInstance().load("users/username.json", HashMap.class);
        if (usernames == null) {
            return "No users found. Please register first.";
        }
        String id = usernames.get(username);
        if (id == null) {
            return "Username is incorrect!";
        }
        User user = SaveManager.getInstance().load("users/" + id + ".json", User.class);
        if (user == null) {
            return "User data not found.";
        }
        if (!PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
            return "Password is incorrect!";
        }

        AppContext.getInstance().setCurrentUser(user);
        user.refreshQuestLog();
        user.saveUser();

        return "Welcome " + user.getNickName() + "!";
    }

    public String forgetPassword(Matcher matcher) {
        String username = matcher.group("username");
        String email = matcher.group("email");

        HashMap<String, String> usernames = SaveManager.getInstance().load("users/username.json", HashMap.class);
        if (usernames == null) {
            return "No users found. Please register first.";
        }
        String id = usernames.get(username);
        if (id == null) {
            return "Username is incorrect!";
        }
        User user = SaveManager.getInstance().load("users/" + id + ".json", User.class);
        if (user == null) {
            return "User data not found.";
        }
        if (!user.getEmail().equals(email)) {
            return "Email is incorrect!";
        }

        return "Answer to this question: " + SecurityQuestions.QUESTIONS.get(Integer.parseInt(user.getSecurityQuestion()));
    }

    public String answer(Matcher matcher) {
        String answer = matcher.group("answer");

        if (!currentUser.getSecurityAnswer().equals(answer)) {
            return "Answer is incorrect!";
        }
        return "Answer is correct!";
    }

    public String resetPassword(Matcher matcher) {
        String newPassword = matcher.group("newPassword");
        currentUser.setPasswordHash(PasswordUtils.hashPassword(newPassword));
        SaveManager.getInstance().save(currentUser, "users/" + currentUser.getId() + ".json");
        return "Password reset successfully!";
    }
}