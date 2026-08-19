package com.pvz.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.pvz.PvZ2;
import com.pvz.controller.AudioManager;
import com.pvz.models.AppContext;
import com.pvz.models.user.Setting;
import com.pvz.models.user.User;
import com.pvz.utils.SaveManager;

import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

public class SettingsMenu extends ScreenAdapter {

    // Swap these for your real asset paths whenever you add them (green tick / gray
    // tick, matching 's) — missing files fall back to a drawn checkmark instead of
    // crashing the game or showing a plain colored square.
    private static final String CHECKBOX_ON_PATH = "textures/ui/checkbox_on.png";
    private static final String CHECKBOX_OFF_PATH = "textures/ui/checkbox_off.png";

    private final PvZ2 game;
    private Stage stage;
    private Skin skin;
    private Setting setting;

    public SettingsMenu(PvZ2 game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(1920, 1080));
        Gdx.input.setInputProcessor(stage);
        skin = PvzSkin.get();

        User user = AppContext.getInstance().getCurrentUser();
        setting = (user != null) ? user.getSetting() : new Setting();
        normalizeSetting();
        persistSetting();

        MenuUiKit.installRotatingBackground(stage.getRoot(), game.getGlobalAssetManager());

        BorderedTable mainPanel = new BorderedTable();
        mainPanel.setSize(700, 950);
        mainPanel.setPosition((1920 - 700) / 2f, (1080 - 950) / 2f);
        mainPanel.defaults().space(15);
        mainPanel.center();
        stage.addActor(mainPanel);

        // --- Close button (top-right corner of the panel, like Profile/News) ----
        ImageButton closeBtn = new ImageButton(skin, "generic_close_circle");
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new MainMenu(game));
            }
        });
        closeBtn.setSize(48, 48);
        closeBtn.setPosition(mainPanel.getX() + mainPanel.getWidth() - 48 - 10,
                             mainPanel.getY() + mainPanel.getHeight() - 48 - 10);
        stage.addActor(closeBtn);

        Label titleLabel = new Label("Settings", skin, "big");
        titleLabel.setFontScale(1.5f);
        titleLabel.setColor(Color.BLACK);
        mainPanel.add(titleLabel).padBottom(20).row();

        mainPanel.add(sectionLabel("Difficulty:")).left().row();
        mainPanel.add(difficultyRow()).row();

        mainPanel.add(sectionLabel("Game Speed:")).left().row();
        mainPanel.add(speedRow()).row();

        mainPanel.add(sectionLabel("Brightness:")).left().row();
        mainPanel.add(brightnessRow()).fillX().row();

        mainPanel.add(sectionLabel("Music Volume:")).left().row();
        mainPanel.add(musicVolumeRow()).fillX().row();
        mainPanel.add(musicMuteToggle()).left().padTop(-5).row();

        mainPanel.add(sectionLabel("SFX Volume:")).left().row();
        mainPanel.add(sfxVolumeRow()).fillX().row();
        mainPanel.add(sfxMuteToggle()).left().padTop(-5).row();

        mainPanel.add(gridToggle()).left().padTop(10).row();
        mainPanel.add(debugToggle()).left().row();

        // No per-screen brightness preview actor needed anymore — PvZ2.render() draws
        // the brightness overlay globally, on top of every screen including this one,
        // so moving the slider already previews live without any extra wiring here.
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text, skin);
        label.setFontScale(1.2f);
        label.setColor(Color.BLACK);
        return label;
    }

    private Table difficultyRow() {
        Table row = new Table();
        TextButton[] buttons = new TextButton[5];
        int current = setting.getDifficulty();
        for (int i = 0; i < 5; i++) {
            final int diff = i + 1;
            buttons[i] = new TextButton(String.valueOf(diff), skin, "purple");
            buttons[i].getLabel().setFontScale(1.3f);
            buttons[i].getLabel().setColor(Color.BLACK);
            buttons[i].setDisabled(diff != current);
            buttons[i].addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    for (TextButton b : buttons) {
                        b.setDisabled(true);
                    }
                    buttons[diff - 1].setDisabled(false);
                    setting.setDifficulty(diff);
                    persistSetting();
                }
            });
            row.add(buttons[i]).width(60).height(50).pad(5);
        }
        return row;
    }

    private Table speedRow() {
        Table row = new Table();
        TextButton[] buttons = new TextButton[3];
        int current = setting.getGameSpeed();
        for (int i = 0; i < 3; i++) {
            final int speed = i + 1;
            buttons[i] = new TextButton(String.valueOf(speed), skin, "brown");
            buttons[i].getLabel().setFontScale(1.3f);
            buttons[i].getLabel().setColor(Color.BLACK);
            buttons[i].setDisabled(speed != current);
            buttons[i].addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    for (TextButton b : buttons) {
                        b.setDisabled(true);
                    }
                    buttons[speed - 1].setDisabled(false);
                    setting.setGameSpeed(speed);
                    persistSetting();
                }
            });
            row.add(buttons[i]).width(60).height(50).pad(5);
        }
        return row;
    }

    private Table brightnessRow() {
        Table row = new Table();
        Slider slider = new Slider(0.5f, 1.5f, 0.01f, false, safeSliderStyle());
        slider.setValue(setting.getBrightness());

        Label valueLabel = new Label(Math.round(setting.getBrightness() * 100) + "%", skin);
        valueLabel.setColor(Color.BLACK);

        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setting.setBrightness(slider.getValue());
                valueLabel.setText(Math.round(slider.getValue() * 100) + "%");
                persistSetting();
                // PvZ2.render() reads setting.getBrightness() fresh every frame, so the
                // whole game (this screen included) dims/brightens live as you drag.
            }
        });

        row.add(slider).width(350).padRight(10);
        row.add(valueLabel).width(60);
        return row;
    }

    private Table musicVolumeRow() {
        return volumeRow(setting.getMusicVolume(), vol -> {
            setting.setMusicVolume(vol);
            applyMusicVolume();
            persistSetting();
        });
    }

    private Table sfxVolumeRow() {
        return volumeRow(setting.getSfxVolume(), vol -> {
            setting.setSfxVolume(vol);
            persistSetting();
        });
    }

    private Table volumeRow(float initialValue, java.util.function.Consumer<Float> onChange) {
        Table row = new Table();
        Slider slider = new Slider(0f, 1f, 0.01f, false, safeSliderStyle());
        slider.setValue(initialValue);

        Label valueLabel = new Label(Math.round(initialValue * 100) + "%", skin);
        valueLabel.setColor(Color.BLACK);

        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                valueLabel.setText(Math.round(slider.getValue() * 100) + "%");
                onChange.accept(slider.getValue());
            }
        });

        row.add(slider).width(350).padRight(10);
        row.add(valueLabel).width(60);
        return row;
    }

    private CheckBox musicMuteToggle() {
        CheckBox checkBox = new CheckBox(" Music Enabled", tickCheckBoxStyle());
        checkBox.setChecked(!setting.isMusicMuted());
        checkBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setting.setMusicMuted(!checkBox.isChecked());
                applyMusicVolume();
                persistSetting();
            }
        });
        return checkBox;
    }

    /** Pushes the current volume/mute state to whatever music is actually playing right now. */
    private void applyMusicVolume() {
        float effectiveVolume = setting.isMusicMuted() ? 0f : setting.getMusicVolume();
        AudioManager.getInstance().setMusicVolume(effectiveVolume);
    }

    private CheckBox sfxMuteToggle() {
        CheckBox checkBox = new CheckBox(" SFX Enabled", tickCheckBoxStyle());
        checkBox.setChecked(!setting.isSfxMuted());
        checkBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setting.setSfxMuted(!checkBox.isChecked());
                persistSetting();
                // Nothing in the project plays a sound effect yet, so there's nothing
                // audible to toggle right now — AudioManager.playSfx(...) already reads
                // this setting, so it'll work the moment SFX playback is added anywhere.
            }
        });
        return checkBox;
    }

    private CheckBox gridToggle() {
        CheckBox checkBox = new CheckBox(" Show Grid", tickCheckBoxStyle());
        checkBox.setChecked(setting.isShowGrid());
        checkBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setting.setShowGrid(checkBox.isChecked());
                persistSetting();
            }
        });
        return checkBox;
    }

    private CheckBox debugToggle() {
        CheckBox checkBox = new CheckBox(" Debug Mode", tickCheckBoxStyle());
        checkBox.setChecked(setting.isDebugMode());
        checkBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setting.setDebugMode(checkBox.isChecked());
                persistSetting();
            }
        });
        return checkBox;
    }

    /** Green/gray tick checkbox style, matching 's. */
    private CheckBox.CheckBoxStyle tickCheckBoxStyle() {
        CheckBox.CheckBoxStyle style = new CheckBox.CheckBoxStyle();
        style.checkboxOn = new TextureRegionDrawable(loadTickTexture(CHECKBOX_ON_PATH, true));
        style.checkboxOff = new TextureRegionDrawable(loadTickTexture(CHECKBOX_OFF_PATH, false));
        style.font = skin.getFont("FBUSV8C6EI_3");
        style.fontColor = Color.BLACK;
        return style;
    }

    /**
     * Old save files used integer settings (brightness 50-150, volume 0-100) while the
     * current model is float (brightness 0.5-1.5, volumes 0-1). A leftover 100 from the
     * old schema would be read as "100% too bright" and make the overlay fully opaque.
     * Migrate stale values (anything above the float range came from the old 0-100/50-150
     * scale, where 100 meant normal) then clamp, so old saves can never break the screen.
     */
    private void normalizeSetting() {
        float brightness = setting.getBrightness();
        if (brightness > 1.5f) brightness = brightness / 100f;   // old 50-150 -> new 0.5-1.5
        setting.setBrightness(Math.max(0.5f, Math.min(1.5f, brightness)));

        float music = setting.getMusicVolume();
        if (music > 1f) music = music / 100f;                    // old 0-100 -> new 0-1
        setting.setMusicVolume(Math.max(0f, Math.min(1f, music)));

        float sfx = setting.getSfxVolume();
        if (sfx > 1f) sfx = sfx / 100f;
        setting.setSfxVolume(Math.max(0f, Math.min(1f, sfx)));

        if (setting.getGameSpeed() < 1 || setting.getGameSpeed() > 3) {
            setting.setGameSpeed(2);
        }
    }

    private void persistSetting() {
        User user = AppContext.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        user.setSetting(setting);
        user.saveUser();
    }

    private Slider.SliderStyle safeSliderStyle() {
        try {
            if (skin.has("default-horizontal", Slider.SliderStyle.class)) {
                return skin.get("default-horizontal", Slider.SliderStyle.class);
            }
        } catch (RuntimeException ignored) {
            // fall through to the manual style below
        }

        Pixmap bgPixmap = new Pixmap(150, 6, Pixmap.Format.RGBA8888);
        bgPixmap.setColor(new Color(0.3f, 0.3f, 0.3f, 0.8f));
        bgPixmap.fill();
        Texture bgTex = new Texture(bgPixmap);
        bgPixmap.dispose();

        Pixmap knobPixmap = new Pixmap(18, 18, Pixmap.Format.RGBA8888);
        knobPixmap.setColor(new Color(0.9f, 0.75f, 0.2f, 1f));
        knobPixmap.fillCircle(9, 9, 8);
        Texture knobTex = new Texture(knobPixmap);
        knobPixmap.dispose();

        Slider.SliderStyle style = new Slider.SliderStyle();
        style.background = new TextureRegionDrawable(bgTex);
        style.knob = new TextureRegionDrawable(knobTex);
        return style;
    }

    /** Loads a checkbox texture only if the file actually exists; otherwise draws an
     *  actual green/gray checkmark tile so it never falls back to a bare colored square. */
    private Texture loadTickTexture(String path, boolean on) {
        if (path != null && !path.isEmpty() && Gdx.files.internal(path).exists()) {
            Texture texture = new Texture(Gdx.files.internal(path));
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return texture;
        }
        return drawnCheckTexture(on);
    }

    /** Rounded tile + a hand-drawn checkmark, green when on / gray when off — a stand-in
     *  for CHECKBOX_ON_PATH/CHECKBOX_OFF_PATH until those PNGs are added. */
    private Texture drawnCheckTexture(boolean on) {
        int size = 40;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        Color tileColor = on ? new Color(0.20f, 0.65f, 0.25f, 1f) : new Color(0.55f, 0.55f, 0.5f, 1f);
        Color tickColor = Color.WHITE;

        pixmap.setColor(tileColor);
        pixmap.fillRectangle(2, 2, size - 4, size - 4);

        pixmap.setColor(tickColor);
        // simple checkmark: a short rising stroke then a longer falling stroke
        drawThickLine(pixmap, 10, 21, 17, 28, 3);
        drawThickLine(pixmap, 17, 28, 30, 12, 3);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void drawThickLine(Pixmap pixmap, int x1, int y1, int x2, int y2, int thickness) {
        for (int t = -thickness / 2; t <= thickness / 2; t++) {
            pixmap.drawLine(x1, y1 + t, x2, y2 + t);
            pixmap.drawLine(x1 + t, y1, x2 + t, y2);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
