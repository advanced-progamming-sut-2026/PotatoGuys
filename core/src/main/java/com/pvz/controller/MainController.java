package com.pvz.controller;

import com.pvz.utils.SaveManager;

public class MainController {

    public void logout() {
        SaveManager.getInstance().delete("session.json");
    }
}
