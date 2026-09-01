package com.pvz.controller;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import com.pvz.models.AppContext;
import com.pvz.models.user.User;

public class AudioManager {
    private static AudioManager instance;
    private Music currentMusic;
    private String currentMusicPath;

    // Sounds are short and get reused a lot (hits, clicks, plants dying, ...), so they're
    // cached by path instead of reloaded from disk on every single play() call.
    private final Map<String, Sound> soundCache = new HashMap<>();

    // Short SFX that live as Music (e.g. MP3 typing clips) are cached here so
    // they're decoded once and can be retriggered cheaply.
    private final Map<String, Music> sfxMusicCache = new HashMap<>();

    private AudioManager() {}

    public static AudioManager getInstance() {
        if (instance == null) instance = new AudioManager();
        return instance;
    }

    /** Resolves the volume background music should actually play at, based on the
     *  logged-in user's saved Setting (music volume + mute). Falls back to 0.7f
     *  if there's no user yet (e.g. login/register screens before sign-in). */
    public float getUserMusicVolume() {
        User user = AppContext.getInstance().getCurrentUser();
        if (user == null || user.getSetting() == null) {
            return 0.7f;
        }
        return user.getSetting().isMusicMuted() ? 0f : user.getSetting().getMusicVolume();
    }

    /** Same idea as {@link #getUserMusicVolume()}, but for one-shot sound effects. */
    public float getUserSfxVolume() {
        User user = AppContext.getInstance().getCurrentUser();
        if (user == null || user.getSetting() == null) {
            return 0.7f;
        }
        return user.getSetting().isSfxMuted() ? 0f : user.getSetting().getSfxVolume();
    }

    public void playMusic(String path, boolean looping, float volume) {
        if (currentMusicPath != null && currentMusicPath.equals(path)) {
            if (currentMusic != null) {
                currentMusic.setVolume(volume);
                if (!currentMusic.isPlaying()) currentMusic.play();
            }
            return;
        }
        stopMusic();
        currentMusic = Gdx.audio.newMusic(Gdx.files.internal(path));
        currentMusic.setLooping(looping);
        currentMusic.setVolume(volume);
        currentMusic.play();
        currentMusicPath = path;
    }

    public void setMusicVolume(float volume) {
        if (currentMusic != null) {
            currentMusic.setVolume(volume);
        }
    }

    public float getMusicVolume() {
        return currentMusic != null ? currentMusic.getVolume() : 0f;
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
            currentMusicPath = null;
        }
    }

    /**
     * Plays a one-shot sound effect at the user's current SFX volume/mute setting.
     * Nothing in the project calls this yet (there's no SFX playback anywhere else in
     * the codebase to hook it up to), but it's here and working so the SFX slider on
     * the Settings screen has somewhere real to plug in as soon as you add one, e.g.:
     * {@code AudioManager.getInstance().playSfx("audio/sfx/plant.mp3");}
     */
    public void playSfx(String path) {
        float volume = getUserSfxVolume();
        if (volume <= 0f) return;
        if (path == null || path.isEmpty() || !Gdx.files.internal(path).exists()) return;

        Sound sound = soundCache.get(path);
        if (sound == null) {
            sound = Gdx.audio.newSound(Gdx.files.internal(path));
            soundCache.put(path, sound);
        }
        sound.play(volume);
    }

    /**
     * Plays a short MP3/OGG effect through the Music player, which decodes MP3
     * cleanly (playing an MP3 as a {@link Sound} can emit a static burst on some
     * backends). The clip is decoded once, cached, and retriggered from the start
     * each call. Respects the user's SFX volume/mute setting.
     *
     * @param path internal asset path, e.g. {@code "assets/audio/music/typing.mp3"}.
     */
    public void playSfxMusic(String path) {
        float volume = getUserSfxVolume();
        if (volume <= 0f) return;
        if (path == null || path.isEmpty()) return;

        Music music = sfxMusicCache.get(path);
        if (music == null) {
            com.badlogic.gdx.files.FileHandle handle = Gdx.files.internal(path);
            if (!handle.exists()) return;
            music = Gdx.audio.newMusic(handle);
            sfxMusicCache.put(path, music);
        }
        music.setVolume(volume);
        music.setPosition(0f);
        music.play();
    }

    /**
     * Immediately stops a short MP3/OGG effect currently playing through the
     * Music player (e.g. when the user skips/advances a dialogue line mid-type).
     * Safe to call even if the clip is not currently playing.
     *
     * @param path internal asset path, e.g. {@code "assets/audio/music/typing.mp3"}.
     */
    public void stopSfxMusic(String path) {
        if (path == null || path.isEmpty()) return;
        Music music = sfxMusicCache.get(path);
        if (music != null) {
            music.stop();
        }
    }

    /**
     * Frees every cached Sound. Call this once, on app shutdown. */
    public void disposeSounds() {
        for (Sound sound : soundCache.values()) {
            sound.dispose();
        }
        soundCache.clear();
        for (Music music : sfxMusicCache.values()) {
            music.dispose();
        }
        sfxMusicCache.clear();
    }
}
