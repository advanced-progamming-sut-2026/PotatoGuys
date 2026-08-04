package com.pvz.view;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

public class NewsModal extends Table {
    public NewsModal(){
        super();
        center();
        setWidth(600);
        setHeight(800);
        pad(200);
        setVisible(false);

        //exit button & wrapper
        Table exitBtnWrapper = new Table();
        exitBtnWrapper.right().top();
        add(exitBtnWrapper).padLeft(850).row();

        //news table
        BorderedTable newsTable=new BorderedTable();
        add(newsTable).size(1000,800);

        ImageButton exitBtn=new ImageButton(PvzSkin.get(),"generic_close");
        exitBtnWrapper.add(exitBtn);
        exitBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                setVisible(false);
            }
        });
    }
}
