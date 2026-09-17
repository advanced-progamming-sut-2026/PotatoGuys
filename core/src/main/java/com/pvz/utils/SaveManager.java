package com.pvz.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.pvz.models.Constants;

public class SaveManager {

    private static SaveManager instance;
    private final Gson gson;

    private SaveManager() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }

    public static SaveManager getInstance() {
        if (instance == null) {
            instance = new SaveManager();
        }
        return instance;
    }

    public void save(Object data, String path) {
        File file = new File(Constants.SAVE_PATH + path);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(data, writer);
            System.out.println("[GsonManager] Data successfully saved to " + path);
        } catch (IOException e) {
            System.err.println("[GsonManager] Error saving file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public <T> T load(String path, Class<T> classType) {
        return loadAbsolute(Constants.SAVE_PATH + path, classType);
    }

    /**
     * Loads and deserializes JSON from {@code fullPath} taken as-is (no
     * {@link Constants#SAVE_PATH} prefix). Used for static, read-only game
     * data resources such as {@code plant_actions.json}, as opposed to
     * user save files.
     */
    public <T> T loadAbsolute(String fullPath, Class<T> classType) {
        File file = new File(fullPath);

        if (!file.exists()) {
            System.out.println("[GsonManager] File not found at " + fullPath);
            return null;
        }

        try (FileReader reader = new FileReader(file)) {
            T data = gson.fromJson(reader, classType);
            System.out.println("[GsonManager] Data successfully loaded from " + fullPath);
            return data;
        } catch (IOException e) {
            System.err.println("[GsonManager] Error loading file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public <T> T loadInternal(String internalPath, Class<T> classType) {
        com.badlogic.gdx.files.FileHandle handle = Gdx.files.internal(internalPath);

        if (!handle.exists()) {
            System.out.println("[GsonManager] File not found at " + internalPath);
            return null;
        }

        try (java.io.Reader reader = handle.reader()) {
            T data = gson.fromJson(reader, classType);
            System.out.println("[GsonManager] Data successfully loaded from " + internalPath);
            return data;
        } catch (Exception e) {
            System.err.println("[GsonManager] Error loading file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void delete(String path) {
        File file = new File(Constants.SAVE_PATH + path);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("[GsonManager] Save file deleted at " + path);
            } else {
                System.err.println("[GsonManager] Failed to delete save file at " + path);
            }
        } else {
            System.out.println("[GsonManager] Save file not found at " + path);
        }
    }

}
