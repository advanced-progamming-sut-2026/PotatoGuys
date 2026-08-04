package com.pvz.view;

import com.badlogic.gdx.Screen;

public class Result {
    private final String message;
    private final Screen screen;

    public Result(String message) {
        this(message, null);
    }

    public Result(String message, Screen screen) {
        this.message = message;
        this.screen = screen;
    }

    public String getMessage() {
        return message;
    }

    public Screen getScreen() {
        return screen;
    }
}