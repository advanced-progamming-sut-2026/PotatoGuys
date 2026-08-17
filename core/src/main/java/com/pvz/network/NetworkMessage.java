package com.pvz.network;

/**
 * One line of this (as JSON, via Gson) = one message over the socket.
 * Both requests and responses use the same envelope:
 *
 *   - Client -> Server: type + requestId + payload (the request body)
 *   - Server -> Client: same requestId echoed back, success/errorMessage set,
 *                       payload = the response body (or null)
 *
 * Keeping requests and responses in one class means you don't need a second
 * protocol later for pushed (server-initiated) messages, like game invites
 * in Phase 3 — those just skip requestId matching on the client.
 */
public class NetworkMessage {
    public MessageType type;
    public String requestId;
    public boolean success = true;
    public String errorMessage;
    /** JSON-encoded body. What it deserializes to depends on {@link #type}. */
    public String payload;

    public NetworkMessage() { }

    public static NetworkMessage request(MessageType type, String requestId, String payload) {
        NetworkMessage m = new NetworkMessage();
        m.type = type;
        m.requestId = requestId;
        m.payload = payload;
        return m;
    }

    public static NetworkMessage ok(NetworkMessage request, String payload) {
        NetworkMessage m = new NetworkMessage();
        m.type = request.type;
        m.requestId = request.requestId;
        m.success = true;
        m.payload = payload;
        return m;
    }

    public static NetworkMessage error(NetworkMessage request, String errorMessage) {
        NetworkMessage m = new NetworkMessage();
        m.type = request.type;
        m.requestId = request.requestId;
        m.success = false;
        m.errorMessage = errorMessage;
        return m;
    }
}
