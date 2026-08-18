package com.pvz.network;

/** Which side of the I,Zombie minigame a matched player controls. */
public enum PlayerRole {
    PLANT,
    ZOMBIE;

    public PlayerRole opposite() {
        return this == PLANT ? ZOMBIE : PLANT;
    }
}
