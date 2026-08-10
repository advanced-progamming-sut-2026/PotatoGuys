package com.pvz.models.user;

public class Setting {
    private int difficulty;
    private int gameSpeed;
    private int brightness;
    private int volume;
    private boolean showGrid;
    private boolean debugMode;

    // creating a setting with default difficulty
    public Setting(){
        difficulty = 3;
        gameSpeed = 2;
        brightness = 100;
        volume = 70;
        showGrid = false;
        debugMode = false;
    }

    public Setting(int difficulty) {
        this.difficulty = difficulty;
        gameSpeed = 2;
        brightness = 100;
        volume = 70;
        showGrid = false;
        debugMode = false;
    }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public int getGameSpeed() { return gameSpeed; }
    public void setGameSpeed(int gameSpeed) { this.gameSpeed = gameSpeed; }

    public int getBrightness() { return brightness; }
    public void setBrightness(int brightness) { this.brightness = brightness; }

    public int getVolume() { return volume; }
    public void setVolume(int volume) { this.volume = volume; }

    public boolean isShowGrid() { return showGrid; }
    public void setShowGrid(boolean showGrid) { this.showGrid = showGrid; }

    public boolean isDebugMode() { return debugMode; }
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }
}