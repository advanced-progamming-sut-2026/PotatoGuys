package com.pvz.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.data.PlantRegistry;
import com.pvz.models.entities.plants.enums.PlantType;
import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.List;

public class PlantSelectModal extends Table {

    private static final int MAX_SELECTED = 7;
    private final List<PlantType> selectedPlants = new ArrayList<>();
    private final Table selectedSlotsTable;
    private final TextButton startButton;
    private final Runnable onStartCallback;

    public PlantSelectModal(Runnable onStartCallback) {
        this.onStartCallback = onStartCallback;
        setFillParent(true);
        center();
        setVisible(false);

        BorderedTable content = new BorderedTable();
        content.center();
        content.pad(25);

        Label titleLabel = new Label("Choose Your Plants", PvzSkin.get(), "big");
        titleLabel.setColor(Color.BLACK);
        content.add(titleLabel).colspan(4).padBottom(15).row();

        // Selected slots table (top)
        selectedSlotsTable = new Table();
        selectedSlotsTable.defaults().size(80, 100).pad(5);
        content.add(selectedSlotsTable).colspan(4).padBottom(15).row();

        // Available plants grid (4 columns) inside a ScrollPane
        Table gridTable = new Table();
        gridTable.top();

        int colCount = 0;
        for (PlantType pt : PlantType.values()) {
            PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(pt);
            String labelText = pt.name() + (sheet != null ? "\nSun: " + sheet.getSunCost() : "");
            TextButton plantBtn = new TextButton(labelText, PvzSkin.get(), "brown");
            plantBtn.getLabel().setWrap(true);
            plantBtn.getLabel().setAlignment(Align.center);

            plantBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    togglePlant(pt);
                }
            });
            gridTable.add(plantBtn).size(155, 75).pad(6);
            colCount++;
            if (colCount % 4 == 0) {
                gridTable.row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(gridTable, PvzSkin.get());
        scrollPane.setFadeScrollBars(false);
        content.add(scrollPane).colspan(4).width(680).height(280).padBottom(20).row();

        // Start / Let's Rock button
        startButton = new TextButton("Let's Rock!", PvzSkin.get(), "purple");
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!selectedPlants.isEmpty() && onStartCallback != null) {
                    onStartCallback.run();
                }
            }
        });
        content.add(startButton).colspan(4).width(200).height(60);

        add(content).size(760, 620).center();
        updateUI();
    }

    private void togglePlant(PlantType pt) {
        if (selectedPlants.contains(pt)) {
            selectedPlants.remove(pt);
        } else {
            if (selectedPlants.size() < MAX_SELECTED) {
                selectedPlants.add(pt);
            }
        }
        updateUI();
    }

    private void updateUI() {
        selectedSlotsTable.clearChildren();
        for (int i = 0; i < MAX_SELECTED; i++) {
            final int index = i;
            Table slotCell = new Table();

            if (i < selectedPlants.size()) {
                PlantType pt = selectedPlants.get(i);
                PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(pt);
                String text = pt.name() + (sheet != null ? "\n(" + sheet.getSunCost() + ")" : "");
                TextButton slotBtn = new TextButton(text, PvzSkin.get(), "brown");
                slotBtn.getLabel().setWrap(true);
                slotBtn.getLabel().setAlignment(Align.center);
                slotBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        selectedPlants.remove(index);
                        updateUI();
                    }
                });
                slotCell.add(slotBtn).grow();
            }
            selectedSlotsTable.add(slotCell).size(80, 100);
        }
    }

    public List<PlantType> getSelectedPlants() {
        return selectedPlants;
    }
}
