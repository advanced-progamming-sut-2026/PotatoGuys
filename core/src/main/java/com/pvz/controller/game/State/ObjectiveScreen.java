package com.pvz.controller.game.State;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import com.pvz.controller.game.GameController;
import com.pvz.controller.game.npc.LevelDialogueRegistry;
import com.pvz.controller.game.npc.NpcDialogueOverlay;
import com.pvz.controller.game.npc.NpcDialogueSequence;
import com.pvz.view.BorderedPanel;

import pvz.skin.PvzSkin;

/**
 * Objective screen shown before the level starts. Displays the level's
 * objectives
 * in a PvZ2-styled dialog box with a "LET'S GO!" button to proceed.
 */
public class ObjectiveScreen extends State {

    private Table overlay;
    private NpcDialogueOverlay npcOverlay;

    public ObjectiveScreen(GameController controller) {
        super(controller);
    }

    @Override
    public void enter() {
        NpcDialogueSequence dialogue = LevelDialogueRegistry.find(
                controller.getLevel().getSeasonName(),
                controller.getLevel().getLevelNumber());

        if (dialogue != null) {
            npcOverlay = new NpcDialogueOverlay(dialogue, this::showNpcFinished);
            controller.getStage().addActor(npcOverlay);
            controller.getStage().setKeyboardFocus(npcOverlay);
            npcOverlay.toFront();
            Gdx.input.setInputProcessor(controller.getStage());
            return;
        }

        showObjectives();
    }

    private void showNpcFinished() {
        controller.getStage().setKeyboardFocus(null);
        npcOverlay = null;
        showObjectives();
    }

    private void showObjectives() {
        overlay = new Table();
        overlay.setFillParent(true);

        // Semi-transparent dark overlay
        overlay.setBackground(PvzSkin.get().newDrawable("white_pixel", new Color(0f, 0f, 0f, 0.8f)));

        // Main dialog panel
        BorderedPanel panel = new BorderedPanel(Color.valueOf("8F4909"));
        Table content = panel.contentLayer;
        content.pad(30f, 35f, 35f, 35f);

        Label.LabelStyle bigStyle = PvzSkin.get().get("big_outline", Label.LabelStyle.class);
        Label.LabelStyle mediumStyle = PvzSkin.get().get("medium_outline", Label.LabelStyle.class);

        // Title banner
        String seasonName = controller.getLevel().getSeasonName();
        int levelNum = controller.getLevel().getLevelNumber();
        String title = seasonName.toUpperCase();
        if (levelNum > 0) {
            title += "  -  Level " + levelNum;
        }

        Label titleLbl = new Label(title, bigStyle);
        titleLbl.setColor(Color.valueOf("FFD700"));
        titleLbl.setFontScale(1.6f);
        titleLbl.setAlignment(Align.center);

        Image separator = new Image(PvzSkin.get().newDrawable("white_pixel", Color.valueOf("D2B48C")));

        // Objectives header
        Label objectivesHeader = new Label("OBJECTIVES", mediumStyle);
        objectivesHeader.setColor(Color.valueOf("FF8C00"));
        objectivesHeader.setFontScale(1.2f);
        objectivesHeader.setAlignment(Align.center);

        // Objective list
        List<String> objectives = controller.getLevel().getObjectives();
        Table objectivesTable = new Table();
        if (objectives != null && !objectives.isEmpty()) {
            for (String obj : objectives) {
                Label bullet = new Label("\u2022", mediumStyle);
                bullet.setColor(Color.valueOf("FFD700"));
                bullet.setFontScale(1.1f);

                Label objLabel = new Label(obj, mediumStyle);
                objLabel.setColor(Color.WHITE);
                objLabel.setFontScale(1.0f);
                objLabel.setWrap(true);
                objLabel.setAlignment(Align.left);

                objectivesTable.add(bullet).padRight(10f);
                objectivesTable.add(objLabel).width(550f).left().row();
                objectivesTable.padBottom(8f);
            }
        } else {
            Label noObj = new Label("Survive against the zombie hordes!", mediumStyle);
            noObj.setColor(Color.WHITE);
            noObj.setFontScale(1.0f);
            noObj.setAlignment(Align.center);
            objectivesTable.add(noObj);
        }

        Image separator2 = new Image(PvzSkin.get().newDrawable("white_pixel", Color.valueOf("D2B48C")));

        // "LET'S GO!" button
        TextButton goBtn = new TextButton("LET'S GO!", PvzSkin.get(), "purple");
        goBtn.getLabel().setFontScale(1.0f);
        goBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dismiss();
            }
        });

        // Build dialog layout
        content.add(titleLbl).padBottom(12f).row();
        content.add(separator).growX().height(3f).padBottom(20f).row();
        content.add(objectivesHeader).padBottom(12f).row();
        content.add(objectivesTable).padBottom(20f).row();
        content.add(separator2).growX().height(3f).padBottom(20f).row();
        content.add(goBtn).size(220f, 60f);

        panel.pack();
        overlay.add(panel).center();
        overlay.getColor().a = 0f;
        overlay.addAction(Actions.fadeIn(0.5f));

        controller.getStage().addActor(overlay);
        Gdx.input.setInputProcessor(controller.getStage());
    }

    @Override
    public void update(float dt) {
        super.update(dt);
    }

    @Override
    public void exit() {
        if (npcOverlay != null) {
            npcOverlay.remove();
            npcOverlay = null;
        }
        if (overlay != null) {
            overlay.remove();
            overlay = null;
        }
        controller.getStage().setKeyboardFocus(null);
    }

    private void dismiss() {
        if (overlay != null && overlay.getActions().size == 0) {
            overlay.addAction(Actions.sequence(
                    Actions.fadeOut(0.3f),
                    Actions.run(() -> controller.changeState(new PanningForward(controller)))));
        }
    }
}
