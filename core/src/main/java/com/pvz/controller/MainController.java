package com.pvz.controller;

import java.util.regex.Matcher;

import com.pvz.utils.SaveManager;

public class MainController {

    public String logout(Matcher matcher) {
        SaveManager.getInstance().delete("session.json");
        return "logged out";
    }
}