package com.pvz.network.game;

import java.util.List;

/**
 * Host -> guest, sent periodically (see GameController's snapshotAccumulator).
 * A full (not delta) picture of the board — enough for the guest to render an
 * accurate mirror, not enough (nor intended) to let the guest re-derive
 * gameplay decisions locally. The guest never validates anything against
 * this; it only draws what it's told.
 *
 * Known simplification: projectiles aren't included. They're short-lived, so
 * the guest just won't see the pea/etc. flight animation — zombie hp/death
 * still reflects correctly on the very next snapshot regardless.
 */
public class GameSnapshot {
    public int currentSun;
    public boolean gameOver;

    /** I,Zombie specific — null for other modes. */
    public boolean[] brainsEaten;
    public float plantSurvivalSecondsRemaining;

    public List<PlantSnap> plants;
    public List<ZombieSnap> zombies;
    public List<SunSnap> suns;

    public static class PlantSnap {
        /** Synthetic id — System.identityHashCode(plant) on the host. Network-layer only. */
        public int id;
        public String type;   // PlantType enum name
        public int col;
        public int lane;
        public int level;
        public boolean boosted;
        public float hp;
    }

    public static class ZombieSnap {
        public int id;
        /** ZombiePropertySheet alias — what ZombieFactory#create expects. */
        public String alias;
        public float x;
        public int lane;
        public float hp;
        /** True for I,Zombie's auto sun-generating zombies, so the guest can
         *  register them for the same floating-sun-icon overlay the host draws. */
        public boolean sunProducer;
    }

    public static class SunSnap {
        public int id;
        public String sunType; // SunType enum name
        public int col;
        public int lane;
        public int amount;
    }
}
