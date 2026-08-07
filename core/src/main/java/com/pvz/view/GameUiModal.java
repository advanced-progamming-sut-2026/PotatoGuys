package com.pvz.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.pvz.models.AppContext;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.Card;
import com.pvz.models.games.card.PlantCard;
import pvz.skin.PvzSkin;

import java.util.HashMap;
import java.util.Map;

public class GameUiModal extends Table {

    private final Label sunLabel;
    private final Label plantFoodLabel;
    private final Table cardsBarTable;
    private PlantCard selectedCard = null;
    private final Map<PlantCard, TextButton> cardButtonMap = new HashMap<>();

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

    public PlantCard getSelectedCard() {
        return selectedCard;
    }

    public void setSelectedCard(PlantCard card) {
        this.selectedCard = card;
        updateCardStyles();
    }

    public void initCards() {
        cardsBarTable.clearChildren();
        cardButtonMap.clear();
        GameContext context = AppContext.getInstance().getGameContext();
        if (context == null) return;

        for (Card card : context.getCards()) {
            if (card instanceof PlantCard pc) {
                String plantName = pc.getPlant().getType().name();
                String text = plantName + "\nCost: " + pc.getCost();
                TextButton cardBtn = new TextButton(text, PvzSkin.get(), "brown");
                cardBtn.getLabel().setWrap(true);
                cardBtn.getLabel().setAlignment(Align.center);

                cardBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (selectedCard == pc) {
                            selectedCard = null; // deselect
                        } else {
                            selectedCard = pc; // select
                        }
                        updateCardStyles();
                    }
                });

                cardButtonMap.put(pc, cardBtn);
                cardsBarTable.add(cardBtn).size(80, 100);
            }
        }
        updateCardStyles();
    }

    public void updateHud() {
        GameContext context = AppContext.getInstance().getGameContext();
        if (context == null) return;

        sunLabel.setText("Sun: " + context.getCurrentSun());
        plantFoodLabel.setText("Plant Food: " + context.getPlantFoodCount());
    }

    private void updateCardStyles() {
        for (Map.Entry<PlantCard, TextButton> entry : cardButtonMap.entrySet()) {
            PlantCard pc = entry.getKey();
            TextButton btn = entry.getValue();
            if (selectedCard == pc) {
                btn.setColor(0.8f, 0.5f, 1f, 1f); // highlighted tint when selected
            } else {
                btn.setColor(1f, 1f, 1f, 1f);
            }
        }
    }
}
