package com.pvz.network;

/**
 * All message kinds understood by the server.
 *
 * Phase 1 (accounts + leaderboard): REGISTER, LOGIN, SAVE_USER, GET_USER, GET_LEADERBOARD.
 *
 * Phase 2 (I,Zombie matchmaking) adds:
 *   - INVITE_SEND / INVITE_RESPONSE: client -> server requests
 *   - INVITE_INCOMING / INVITE_REJECTED / MATCH_FOUND: server -> client pushes
 *     (these never appear as a *request* type — only ever pushed)
 *   - QUEUE_JOIN / QUEUE_CANCEL: client -> server requests for random matchmaking
 *   - MATCH_MESSAGE: bidirectional, blind relay between two matched clients.
 *     Reserved for Phase 3 (in-match state sync, reactions, game-over).
 */
public enum MessageType {
    REGISTER,
    LOGIN,
    SAVE_USER,
    GET_USER,
    GET_LEADERBOARD,
    CHECK_USERNAME,
    FORGOT_PASSWORD,
    RESET_PASSWORD,
    UPDATE_PROFILE,

    INVITE_SEND,
    INVITE_INCOMING,
    INVITE_RESPONSE,
    INVITE_REJECTED,
    QUEUE_JOIN,
    QUEUE_CANCEL,
    MATCH_FOUND,
    MATCH_MESSAGE,
    OPPONENT_DISCONNECTED
}
