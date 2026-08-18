package com.pvz.models;

import com.pvz.network.PlayerRole;

/**
 * Info about the I,Zombie online match the current user just got matched into,
 * set by OpponentSelectMenu right before launching GameScreen. Phase 3 reads
 * this (via {@link AppContext#getMatchSession()}) to wire up two-player sync —
 * it's not consumed by anything yet.
 */
public class MatchSession {
    private final String matchId;
    private final String opponentUsername;
    private final PlayerRole myRole;

    public MatchSession(String matchId, String opponentUsername, PlayerRole myRole) {
        this.matchId = matchId;
        this.opponentUsername = opponentUsername;
        this.myRole = myRole;
    }

    public String getMatchId() { return matchId; }
    public String getOpponentUsername() { return opponentUsername; }
    public PlayerRole getMyRole() { return myRole; }
}
