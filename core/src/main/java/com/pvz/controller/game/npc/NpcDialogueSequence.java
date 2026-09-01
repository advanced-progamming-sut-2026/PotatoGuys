package com.pvz.controller.game.npc;

import java.util.List;

/**
 * Data describing a level-intro NPC dialogue: which PAM animation to play, which
 * named clips to use for each phase, and the subtitle lines to type out.
 *
 * <p>Port of group-51's {@code NpcDialogueSequence}.
 */
public record NpcDialogueSequence(
        String pamPath,
        String enterClip,
        String idleClip,
        List<String> talkClips,
        String leaveClip,
        List<String> lines
) {
    public NpcDialogueSequence {
        if (pamPath == null || pamPath.isBlank()) {
            throw new IllegalArgumentException("NPC PAM path cannot be blank.");
        }
        if (enterClip == null || enterClip.isBlank()) {
            throw new IllegalArgumentException("NPC enter clip cannot be blank.");
        }
        if (idleClip == null || idleClip.isBlank()) {
            throw new IllegalArgumentException("NPC idle clip cannot be blank.");
        }
        if (leaveClip == null || leaveClip.isBlank()) {
            throw new IllegalArgumentException("NPC leave clip cannot be blank.");
        }

        talkClips = talkClips == null ? List.of() : List.copyOf(talkClips);
        lines = lines == null ? List.of() : List.copyOf(lines);

        if (talkClips.isEmpty()) {
            throw new IllegalArgumentException("At least one NPC talking clip is required.");
        }
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("At least one NPC dialogue line is required.");
        }
    }
}
