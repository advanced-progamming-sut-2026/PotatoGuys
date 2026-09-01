package com.pvz.models.games.map.tile;

public enum TileTags {
    NORMAL("Normal"),
    GRAVE("Grave"),
    ICE_BLOCK("Ice Block"),
    SLIP_UP("Slip up"),
    SLIP_DOWN("Slip down"),
    WATER("Water"),
    LOW_TIDE("Low tide"),
    NECROMANCY("Necromancy"),
    OCTOPUS("Octopus"),
    ENDANGERED_TILE("Endangered Tile");

    public final String typeName;

    TileTags(String typeName){
        this.typeName=typeName;
    }
}
