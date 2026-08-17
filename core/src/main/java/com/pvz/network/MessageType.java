package com.pvz.network;

/**
 * All message kinds understood by the Phase-1 server (accounts + leaderboard).
 * Extend this enum in later phases (matchmaking, I-Zombie sync, reactions...)
 * instead of creating a second protocol — everything rides the same
 * {@link NetworkMessage} envelope.
 */
public enum MessageType {
    REGISTER,
    LOGIN,
    SAVE_USER,
    GET_USER,
    GET_LEADERBOARD
}
