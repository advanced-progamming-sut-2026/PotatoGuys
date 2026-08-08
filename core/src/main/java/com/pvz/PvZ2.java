package com.pvz;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.pvz.models.AppContext;
import com.pvz.models.user.User;
import com.pvz.utils.SaveManager;
import com.pvz.view.MainMenu;
import com.pvz.view.RegisterMenu;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;

import java.util.HashMap;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class PvZ2 extends Game {
    public static TextureBank textureBank;
    public static PamPlayer pamPlayer;
    public static SpriteBatch batch;
    AssetManager globalAssetManager;
    public PvZ2(){
        globalAssetManager=new AssetManager();
        globalAssetManager.load("textures/backgrounds/MainMenu.png", Texture.class);
        globalAssetManager.load("textures/pvz2_logo_horizontal.png", Texture.class);
        globalAssetManager.load("textures/ui/news_button.png", Texture.class);
        globalAssetManager.load("news_preview/news_selected2.png", Texture.class);
        globalAssetManager.load("textures/ui/buttons_hud_back_normal.png", Texture.class);
        String savedUsername = SaveManager.getInstance().load("session.json", String.class);
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
        }
    }
    @Override
    public void create() {
        globalAssetManager.finishLoading();
        textureBank=new TextureBank("768",Gdx.files.internal("./assets/pvz assets/"));
        pamPlayer=new PamPlayer(textureBank,Gdx.files.internal("./assets/pvz assets/"));
        batch=new SpriteBatch();
        if (AppContext.getInstance().getCurrentUser()==null){
            setScreen(new RegisterMenu(this));
        } else {
            setScreen(new MainMenu(this));
        }
    }

    @Override
    public void render() {
        super.render();
        textureBank.update();
    }

    public AssetManager getGlobalAssetManager(){
        return globalAssetManager;
    }
}
