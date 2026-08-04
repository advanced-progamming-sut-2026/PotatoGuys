package com.pvz.controller;

import java.util.regex.Matcher;

import com.pvz.utils.SaveManager;

public class MainController {

    public void logout() {
        SaveManager.getInstance().delete("session.json");
    }
}
