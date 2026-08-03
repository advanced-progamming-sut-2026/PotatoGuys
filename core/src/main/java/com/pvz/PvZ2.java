package com.pvz;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.pvz.models.AppContext;
import com.pvz.models.user.User;
import com.pvz.utils.SaveManager;
import com.pvz.view.MainMenu;
import pvz.skin.PvzSkin;

import java.util.HashMap;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class PvZ2 extends Game {
    AssetManager globalAssetManager;
    public PvZ2(){
        globalAssetManager=new AssetManager();
        globalAssetManager.load("textures/backgrounds/MainMenu.png", Texture.class);
        globalAssetManager.load("textures/pvz2_logo_horizontal.png", Texture.class);
        /*String savedUsername = SaveManager.getInstance().load("session.json", String.class);
        if (savedUsername != null) {
            HashMap<String, String> usernames = SaveManager.getInstance().load("users/username.json", HashMap.class);
            if (usernames != null) {
                String id = usernames.get(savedUsername);
                if (id != null) {
                    User user = SaveManager.getInstance().load("users/" + id + ".json", User.class);
                    if (user != null) {
                        user.refreshQuestLog();
                        AppContext.getInstance().setCurrentUser(user);
                        System.out.println("Welcome back, " + user.getNickName() + "!");
                        return;
                    }
                }
            }
            SaveManager.getInstance().delete("session.json");
        }*/
    }
    @Override
    public void create() {
        globalAssetManager.finishLoading();
        setScreen(new MainMenu(this));
    }

    public AssetManager getGlobalAssetManager(){
        return globalAssetManager;
    }
}
