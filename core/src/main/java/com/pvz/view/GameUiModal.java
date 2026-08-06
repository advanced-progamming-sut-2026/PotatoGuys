package com.pvz.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.pvz.models.AppContext;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.Card;
import com.pvz.models.games.card.PlantCard;
import pvz.skin.PvzSkin;

public class GameUiModal extends Table {

    private final Label sunLabel;
    private final Label plantFoodLabel;
    private final Table cardsBarTable;

    public GameUiModal() {
        super();
        setFillParent(true);
        top();
        setVisible(false);

        // Top bar container
        Table topBar = new Table();
        topBar.top().left();
        topBar.pad(15);

        // Sun bank display
        sunLabel = new Label("Sun: 50", PvzSkin.get(), "big");
        sunLabel.setColor(Color.YELLOW);
        topBar.add(sunLabel).padRight(25);

        // Plant food display
        plantFoodLabel = new Label("Plant Food: 0", PvzSkin.get());
        plantFoodLabel.setColor(Color.GREEN);
        topBar.add(plantFoodLabel).padRight(25);

        // Cards bar (selected plants)
        cardsBarTable = new Table();
        cardsBarTable.defaults().size(80, 100).pad(5);
        topBar.add(cardsBarTable);

        add(topBar).top().left().expandX();
    }

    public void updateHud() {
        GameContext context = AppContext.getInstance().getGameContext();
        if (context == null) return;

        sunLabel.setText("Sun: " + context.getCurrentSun());
        plantFoodLabel.setText("Plant Food: " + context.getPlantFoodCount());

        cardsBarTable.clearChildren();
        for (Card card : context.getCards()) {
            if (card instanceof PlantCard pc) {
                String plantName = pc.getPlant().getType().name();
                String text = plantName + "\nCost: " + pc.getCost();
                if (!pc.canUse()) {
                    text += "\nCooldown";
                }
                TextButton cardBtn = new TextButton(text, PvzSkin.get(), "brown");
                cardBtn.getLabel().setWrap(true);
                cardBtn.getLabel().setAlignment(Align.center);
                if (!pc.canUse() || context.getCurrentSun() < pc.getCost()) {
                    cardBtn.setColor(0.6f, 0.6f, 0.6f, 0.9f);
                } else {
                    cardBtn.setColor(1f, 1f, 1f, 1f);
                }
                cardsBarTable.add(cardBtn).size(80, 100);
            }
        }
    }
}
