package pvz.controller.user;

import java.util.HashMap;
import java.util.regex.Matcher;

import pvz.models.AppContext;
import pvz.models.user.User;
import pvz.utils.PasswordUtils;
import pvz.utils.SaveManager;
import pvz.view.MainMenu;
import pvz.view.Menu;
import pvz.view.Result;

public class ProfileController {
    public Result changeUsername(Matcher matcher) { 
        String newUsername = matcher.group("username");
        User user = AppContext.getInstance().getCurrentUser();
        String lastUsername = user.getUsername();

        if(newUsername == null || newUsername.isEmpty()) {
            return new Result("Username cannot be empty.");
        }

        if(user.getUsername().equals(newUsername)){
            return new Result("Username is same. New username cannot be the same as your current username.");
        }

        if(PatternManager.validateUsername(newUsername) != null) {
            return new Result("Invalid username format.");
        }

        user.setUsername(newUsername);
        updateUser(user , lastUsername);
        return new Result("Username has changed successfully."); 
    }
    public Result changeNickname(Matcher matcher) { 
        String newNickname = matcher.group("nickname");
        User user = AppContext.getInstance().getCurrentUser();

        if(newNickname == null || newNickname.isEmpty()) {
            return new Result("Nickname cannot be empty.");
        }

        if(user.getNickName().equals(newNickname)){
            return new Result("Nickname is same as your current username.");
        }

        if(PatternManager.validateNickname(newNickname) != null) {
            return new Result("Invalid nickname format.");
        }

        user.setNickName(newNickname);
        updateUser(user , user.getUsername());

        return new Result("Nickname has changed successfully"); 
    }
    public Result changeEmail(Matcher matcher) { 
        String newEmail = matcher.group("email");
        User user = AppContext.getInstance().getCurrentUser();

        if(newEmail == null || newEmail.isEmpty()) {
            return new Result("Email cannot be empty.");
        }

        if(user.getEmail().equals(newEmail)){
            return new Result("Email is same as your current username.");
        }

        if(PatternManager.validateEmail(newEmail) != null) {
            return new Result("Invalid email format.");
        }

        user.setEmail(newEmail);
        updateUser(user, user.getUsername());

        return new Result("Email has changed successfully"); 
    }
    public Result changePassword(Matcher matcher) { 
        String newPassword = matcher.group("newPassword");
        String oldPassword = matcher.group("oldPassword");
        User user = AppContext.getInstance().getCurrentUser();

        if(newPassword == null || newPassword.isEmpty() || oldPassword == null || oldPassword.isEmpty()) {
            return new Result("New password and old password cannot be empty.");
        }

        if(PatternManager.validatePassword(newPassword , newPassword) != null) {
            return new Result("Invalid password format.");
        }

        if(!PasswordUtils.verifyPassword(oldPassword, user.getPasswordHash())){
            return new Result("Old password is incorrect.");
        }

        if(PasswordUtils.verifyPassword(newPassword, user.getPasswordHash())){
            return new Result("Password is same as your current username.");
        }

        user.setPasswordHash(PasswordUtils.hashPassword(newPassword));
        updateUser(user , user.getUsername());

        return new Result("Password has changed successfully"); 
    }
    public Result showInfo(Matcher matcher) { 
        StringBuilder result = new StringBuilder();
        User user = AppContext.getInstance().getCurrentUser();

        result.append("Username: " + user.getUsername() + "\n");
        result.append("Nickname: " + user.getNickName() + "\n");
        result.append("Game played: " + user.getProfile().getGamePlayed() + "\n");
        result.append("Coins: " + user.getProfile().getCoins() + "\n");
        result.append("Diamonds: " + user.getProfile().getDiamonds() + "\n");
        // result.append("Season Progresses: " + user.getProfile().getSeasonProgresses().size() + "\n");
        result.append("Miopoint: " + user.getProfile().getMaxMiopoint() + "\n");

        return new Result(result.toString()); 
    }

    public void updateUser(User user , String lastUsername){
        HashMap<String , String> usernames = SaveManager.getInstance().load("users/username.json", HashMap.class);
        usernames.remove(lastUsername);
        usernames.put(user.getUsername() , user.getId());
        SaveManager.getInstance().save(usernames, "users/username.json");
        SaveManager.getInstance().save(user, "users/" + user.getId() + ".json");
    }
    public Result exit(Matcher matcher) {
        Menu nextMenu = new MainMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }

}