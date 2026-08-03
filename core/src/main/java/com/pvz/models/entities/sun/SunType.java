package com.pvz.models.entities.sun;

public enum SunType {
    NORMAL(25),
    SPECIAL(100),
    RADIOACTIVE(25);

    private final int amountSun;
    private SunType(int amountSun){
        this.amountSun = amountSun;
    }

    public int getAmountSun(){
        return this.amountSun;
    }

}
