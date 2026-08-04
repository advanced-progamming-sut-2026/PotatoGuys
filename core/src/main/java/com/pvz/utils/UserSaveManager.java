package com.pvz.utils;

import java.util.HashMap;

import com.pvz.models.user.User;

public class UserSaveManager {

    private static UserSaveManager instance;

    private UserSaveManager() { }

    public static UserSaveManager getInstance() {
        if (instance == null) {
            instance = new UserSaveManager();
        }
        return instance;
    }

    public boolean isUsernameTaken(String username) {
        HashMap<String, String> usernames = SaveManager.getInstance().load("users/username.json", HashMap.class);
        if (usernames == null) {
            return false;
        }
        return usernames.containsKey(username);
    }

    public User loadUserByUsername(String username) {
        HashMap<String, String> usernames = SaveManager.getInstance().load("users/username.json", HashMap.class);
        if (usernames == null) {
            return null;
        }
        String id = usernames.get(username);
        if (id == null) {
            return null;
        }
        return SaveManager.getInstance().load("users/" + id + ".json", User.class);
    }
}