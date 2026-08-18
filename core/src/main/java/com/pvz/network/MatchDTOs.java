package com.pvz.network;

/**
 * Small message-body classes for the matchmaking protocol (Phase 2).
 * These are the objects that get JSON-encoded into {@link NetworkMessage#payload}.
 * Kept in one file since each is tiny and they're always used together.
 */
public class MatchDTOs {

    private MatchDTOs() { }

    /** Client -> Server, sent as the payload of an INVITE_SEND request. */
    public static class InviteSendRequest {
        public String targetUsername;
        /** The role the *inviter* wants to play. The invitee gets the opposite. */
        public PlayerRole requestedRole;

        public InviteSendRequest(String targetUsername, PlayerRole requestedRole) {
            this.targetUsername = targetUsername;
            this.requestedRole = requestedRole;
        }
    }

    /** Server -> invitee, pushed as an INVITE_INCOMING message (no requestId match). */
    public static class InviteIncomingPayload {
        public String matchId;
        public String fromUsername;
        /** The role the invitee would play if they accept. */
        public PlayerRole yourRole;
    }

    /** Client -> Server, sent as the payload of an INVITE_RESPONSE request (by the invitee). */
    public static class InviteResponseRequest {
        public String matchId;
        public boolean accepted;

        public InviteResponseRequest(String matchId, boolean accepted) {
            this.matchId = matchId;
            this.accepted = accepted;
        }
    }

    /** Server -> original inviter, pushed as INVITE_REJECTED if the invitee declines. */
    public static class InviteRejectedPayload {
        public String matchId;
        public String byUsername;
    }

    /**
     * Server -> both participants, pushed as MATCH_FOUND once a match is established —
     * either by invite acceptance or by random-queue pairing. This is the single event
     * both the "play with a friend" and "play random" flows converge on.
     */
    public static class MatchFoundPayload {
        public String matchId;
        public String opponentUsername;
        public PlayerRole yourRole;
    }

    /**
     * Generic envelope for anything exchanged *during* an established match
     * (game-state sync, reactions, game-over notice — Phase 3 material). The server
     * relays this blind: it only looks at matchId to know where to forward it, never
     * at the inner payload.
     */
    public static class MatchMessageEnvelope {
        public String matchId;
        public String payload;

        public MatchMessageEnvelope(String matchId, String payload) {
            this.matchId = matchId;
            this.payload = payload;
        }
    }
}
