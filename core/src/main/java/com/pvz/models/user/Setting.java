package com.pvz.models.user;

public class Setting {
    private int difficulty;
    private int gameSpeed;
    private boolean showGrid;
    private boolean debugMode;

    // 0.5 - 1.5 (50% - 150%), matches the on-screen brightness overlay range.
    private float brightness;
    // 0f (muted) - 1f (full volume).
    private float musicVolume;
    private float sfxVolume;
    private boolean musicMuted;
    private boolean sfxMuted;
    private boolean fullscreen;

    // creating a setting with default difficulty
    public Setting(){
        difficulty = 1;
        gameSpeed = 1;
        showGrid = false;
        debugMode = false;
        brightness = 1.0f;
        musicVolume = 0.7f;
        sfxVolume = 0.7f;
        musicMuted = false;
        sfxMuted = false;
        fullscreen = true;
    }

    public Setting(int difficulty) {
        this();
        this.difficulty = difficulty;
    }

    public int getDifficulty() {
        return difficulty;
    }
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getGameSpeed() {
        return gameSpeed;
    }
    public void setGameSpeed(int gameSpeed) {
        this.gameSpeed = gameSpeed;
    }

    public boolean isShowGrid() {
        return showGrid;
    }
    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public boolean isDebugMode() {
        return debugMode;
    }
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public float getBrightness() {
        return brightness;
    }
    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public float getMusicVolume() {
        return musicVolume;
    }
    public void setMusicVolume(float musicVolume) {
        this.musicVolume = musicVolume;
    }

    public float getSfxVolume() {
        return sfxVolume;
    }
    public void setSfxVolume(float sfxVolume) {
        this.sfxVolume = sfxVolume;
    }

    public boolean isMusicMuted() {
        return musicMuted;
    }
    public void setMusicMuted(boolean musicMuted) {
        this.musicMuted = musicMuted;
    }

    public boolean isSfxMuted() {
        return sfxMuted;
    }
    public void setSfxMuted(boolean sfxMuted) {
        this.sfxMuted = sfxMuted;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }
    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }
}
