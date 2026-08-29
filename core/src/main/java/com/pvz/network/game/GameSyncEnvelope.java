package com.pvz.network.game;

/**
 * The server's MATCH_MESSAGE channel (see Phase 2) relays an opaque payload
 * string blindly. This is that payload's inner shape: which of two things is
 * this — a state snapshot (host -> guest) or a player action (guest -> host)?
 */
public class GameSyncEnvelope {
    public enum Kind {
        SNAPSHOT, ACTION, QUIT, CHAT
    }

    public Kind kind;
    /**
     * JSON-encoded GameSnapshot (if kind == SNAPSHOT) or GameAction (if kind ==
     * ACTION).
     */
    public String data;

    public GameSyncEnvelope() {
    }

    public GameSyncEnvelope(Kind kind, String data) {
        this.kind = kind;
        this.data = data;
    }
}
