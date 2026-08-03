package com.pvz.models.user;

import java.util.ArrayList;
import java.util.List;

public class GreenHouseCollection {
    private List<MyPlant> plants;

    public List<MyPlant> getPlants() {
        return plants;
    }

    public void setPlants(List<MyPlant> plants) {
        this.plants = plants;
    }

    public GreenHouseCollection() {
        this.plants = new ArrayList<>();
    }
}
