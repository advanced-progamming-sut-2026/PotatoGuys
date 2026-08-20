package com.pvz.network.game;

/**
 * Sent guest -> host: "please do this on my behalf, you're the authority."
 * The host validates and applies it with the exact same isValidPlacement/
 * handlePlacement code it already uses for its own local input — this is
 * just how that input reaches it when it didn't happen on the host's own
 * machine.
 */
public class GameAction {
    public enum Type { PLACE_PLANT, PLACE_ZOMBIE, COLLECT_SUN }

    public Type type;

    /** PLACE_PLANT/PLACE_ZOMBIE: the card's plant/zombie type name (e.g. "PEASHOOTER", "BASIC"). */
    public String cardTypeName;
    public int col;
    public int lane;

    /** COLLECT_SUN: the synthetic id (from the most recent snapshot) of the sun being collected. */
    public int sunId;

    public GameAction() { }

    public static GameAction placeCard(Type type, String cardTypeName, int col, int lane) {
        GameAction a = new GameAction();
        a.type = type;
        a.cardTypeName = cardTypeName;
        a.col = col;
        a.lane = lane;
        return a;
    }

    public static GameAction collectSun(int sunId) {
        GameAction a = new GameAction();
        a.type = Type.COLLECT_SUN;
        a.sunId = sunId;
        return a;
    }
}
