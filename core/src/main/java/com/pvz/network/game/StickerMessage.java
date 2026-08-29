package com.pvz.network.game;

/**
 * An emote "sticker" picked from the PvP quick panels. Carries everything the
 * receiving side needs to render it: which PAM to load, which clip inside it,
 * the visual scale, and how long the looping clip stays on screen before being
 * removed.
 */
public class StickerMessage {

    /** PAM path relative to the assets root, e.g. "768/FULL/ZOMBIE/....PAM". */
    public String pamPath;
    /** Clip label inside the PAM, e.g. "eat", "walk", "jam_idle". */
    public String clip;
    /** Draw scale applied around the canvas centre (e.g. 1, 1.2 or 0.5). */
    public float scale;
    /** How long the looping sticker stays on the opponent's screen. */
    public float seconds = 3f;

    public StickerMessage() {
    }

    public StickerMessage(String pamPath, String clip, float scale, float seconds) {
        this.pamPath = pamPath;
        this.clip = clip;
        this.scale = scale;
        this.seconds = seconds;
    }
}