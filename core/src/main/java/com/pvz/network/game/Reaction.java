package com.pvz.network.game;

/**
 * A short quick-reaction sent to the opponent during an active I,Zombie match
 * (Phase 3). Carried inside a {@link GameSyncEnvelope} of kind {@code REACTION}
 * and relayed blind through the server's MATCH_MESSAGE channel. Both the sender
 * and the receiver render it identically at the bottom-centre of the screen.
 */
public class Reaction {
    public enum Kind { TEXT, EMOJI }

    public Kind kind;
    /**
     * TEXT: the message string itself ("Hello!", "Nice", ...).
     * EMOJI: the texture-bank image id to draw (e.g. "IMAGE_UI_JOUST_AVATARS_AVATAR_17").
     */
    public String value;

    public Reaction() {
    }

    public Reaction(Kind kind, String value) {
        this.kind = kind;
        this.value = value;
    }
}