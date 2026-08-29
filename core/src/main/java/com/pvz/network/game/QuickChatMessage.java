package com.pvz.network.game;

/**
 * One predefined quick-chat line (a short text or an emoji) exchanged during an
 * online I,Zombie match. It rides inside a {@link GameSyncEnvelope} of kind
 * {@code CHAT} over the existing MATCH_MESSAGE relay, so the server forwards it
 * to the opponent blindly without ever looking inside.
 */
public class QuickChatMessage {

    public enum Kind { TEXT, EMOJI }

    /** Whether this is one of the 3 canned messages or one of the 3 emojis. */
    public Kind kind;
    /** Index into the 3 predefined messages (kind == TEXT) or 3 emojis (kind == EMOJI). */
    public int index;

    public QuickChatMessage() {
    }

    public QuickChatMessage(Kind kind, int index) {
        this.kind = kind;
        this.index = index;
    }
}