package com.pvz.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.pvz.models.leaderboard.Leaderboard.LeaderBoardEntry;
import com.pvz.models.user.User;

/**
 * One persistent TCP connection to {@link com.pvz.server.PvzServer}.
 *
 * Usage pattern everywhere in the client: fire a request, get a callback.
 * The callback always runs back on the libGDX render thread
 * (via {@code Gdx.app.postRunnable}), so it's always safe to touch
 * Stage/Actor/Label objects directly inside it — never call this from
 * a Gdx-UI-touching context off the render thread.
 *
 * Call {@link #connect(String, int)} once at app startup (e.g. in
 * PvZ2#create() or your FirstScreen) before using any other method.
 *
 * Phase 2 addition: besides request/response, the server can also *push*
 * a message with no matching requestId (e.g. someone invited you to a
 * match). Register a handler for those with {@link #setPushListener}
 * or one of the typed on___ methods below.
 */
public class NetworkClient {

    private static NetworkClient instance;

    public static NetworkClient getInstance() {
        if (instance == null) {
            instance = new NetworkClient();
        }
        return instance;
    }

    private final Gson gson = new Gson();
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread readerThread;
    private volatile boolean connected = false;

    private final Map<String, Consumer<NetworkMessage>> pending = new ConcurrentHashMap<>();
    private final Map<MessageType, Consumer<NetworkMessage>> pushListeners = new ConcurrentHashMap<>();

    private NetworkClient() { }

    public boolean isConnected() {
        return connected;
    }

    /** Blocking connect — call this off the render thread (e.g. in a loading screen) if you can. */
    public synchronized void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        OutputStream rawOut = socket.getOutputStream();
        out = new PrintWriter(new java.io.OutputStreamWriter(rawOut, StandardCharsets.UTF_8), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        connected = true;

        readerThread = new Thread(this::readLoop, "network-client-reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    public synchronized void disconnect() {
        connected = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) { }
    }

    private void readLoop() {
        try {
            String line;
            while (connected && (line = in.readLine()) != null) {
                NetworkMessage msg = gson.fromJson(line, NetworkMessage.class);
                if (msg == null) continue;

                // Request/response: someone is waiting on this exact requestId.
                Consumer<NetworkMessage> callback = msg.requestId != null ? pending.remove(msg.requestId) : null;
                if (callback != null) {
                    Gdx.app.postRunnable(() -> callback.accept(msg));
                    continue;
                }

                // Otherwise it's an unprompted server push (invite, match found, ...).
                Consumer<NetworkMessage> pushListener = pushListeners.get(msg.type);
                if (pushListener != null) {
                    Gdx.app.postRunnable(() -> pushListener.accept(msg));
                }
            }
        } catch (IOException e) {
            System.err.println("[NetworkClient] Connection lost: " + e.getMessage());
        } finally {
            connected = false;
        }
    }

    /**
     * Fires a request and returns immediately. {@code callback} runs later,
     * on the GL thread, once the server responds. Always check
     * {@code response.success} first inside the callback.
     */
    public synchronized void sendRequest(MessageType type, Object payloadObject, Consumer<NetworkMessage> callback) {
        if (!connected) {
            NetworkMessage fake = new NetworkMessage();
            fake.type = type;
            fake.success = false;
            fake.errorMessage = "Not connected to server.";
            if (callback != null) {
                Gdx.app.postRunnable(() -> callback.accept(fake));
            }
            return;
        }

        String requestId = UUID.randomUUID().toString();
        String payloadJson = payloadObject != null ? gson.toJson(payloadObject) : null;
        NetworkMessage request = NetworkMessage.request(type, requestId, payloadJson);

        if (callback != null) {
            pending.put(requestId, callback);
        }
        out.println(gson.toJson(request));
    }

    /** Deserializes {@code response.payload} into {@code clazz}. Null if payload is null. */
    public <T> T parsePayload(NetworkMessage response, Class<T> clazz) {
        if (response.payload == null) return null;
        return gson.fromJson(response.payload, clazz);
    }

    /**
     * Registers a handler for messages the server sends without being asked
     * (no matching requestId) — e.g. an incoming invite. Only one handler per
     * type at a time; the newest call replaces the previous one. Call
     * {@link #clearPushListener} when leaving a screen that registered one,
     * so a stale screen doesn't react to pushes meant for whatever's shown now.
     */
    public void setPushListener(MessageType type, Consumer<NetworkMessage> listener) {
        pushListeners.put(type, listener);
    }

    public void clearPushListener(MessageType type) {
        pushListeners.remove(type);
    }

    // ---- Typed convenience wrappers: accounts (Phase 1) --------------------------

    /** {@code newUser} should be fully built client-side (starter progress, security Q&A...)
     *  but with a null id — the server assigns the id and checks username uniqueness. */
    public void register(User newUser, Consumer<NetworkMessage> callback) {
        sendRequest(MessageType.REGISTER, newUser, callback);
    }

    public void login(String username, String password, Consumer<NetworkMessage> callback) {
        LoginRequest req = new LoginRequest(username, password);
        sendRequest(MessageType.LOGIN, req, callback);
    }

    /** Fire-and-forget is fine: pass null as callback if you don't need confirmation. */
    public void saveUser(User user, Consumer<NetworkMessage> callback) {
        sendRequest(MessageType.SAVE_USER, user, callback);
    }

    public void getUser(String username, Consumer<NetworkMessage> callback) {
        sendRequest(MessageType.GET_USER, username, callback);
    }

    public void getLeaderboard(Consumer<List<LeaderBoardEntry>> onLoaded) {
        sendRequest(MessageType.GET_LEADERBOARD, null, response -> {
            if (!response.success) {
                System.err.println("[NetworkClient] getLeaderboard failed: " + response.errorMessage);
                onLoaded.accept(List.of());
                return;
            }
            List<LeaderBoardEntry> entries = gson.fromJson(response.payload,
                    new TypeToken<List<LeaderBoardEntry>>() { }.getType());
            onLoaded.accept(entries);
        });
    }

    public static class LoginRequest {
        public String username;
        public String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    // ---- Typed convenience wrappers: I,Zombie matchmaking (Phase 2) --------------

    /** Invites a specific online user. {@code callback} reports whether the invite
     *  was delivered (target found & online) — not whether they accepted; that
     *  comes later via {@link #onMatchFound} or {@link #onInviteRejected}. */
    public void sendInvite(String targetUsername, PlayerRole requestedRole, Consumer<NetworkMessage> callback) {
        sendRequest(MessageType.INVITE_SEND, new MatchDTOs.InviteSendRequest(targetUsername, requestedRole), callback);
    }

    public void respondToInvite(String matchId, boolean accepted, Consumer<NetworkMessage> callback) {
        sendRequest(MessageType.INVITE_RESPONSE, new MatchDTOs.InviteResponseRequest(matchId, accepted), callback);
    }

    /** Joins the random-opponent queue. If someone's already waiting, both sides
     *  get a {@link #onMatchFound} push almost immediately; otherwise you wait
     *  for it. {@code callback} just confirms the join request was received. */
    public void joinRandomQueue(Consumer<NetworkMessage> callback) {
        sendRequest(MessageType.QUEUE_JOIN, null, callback);
    }

    public void cancelRandomQueue(Consumer<NetworkMessage> callback) {
        sendRequest(MessageType.QUEUE_CANCEL, null, callback);
    }

    /** Someone sent you an invite. Payload includes the matchId you must echo back
     *  in {@link #respondToInvite}. */
    public void onInviteIncoming(Consumer<MatchDTOs.InviteIncomingPayload> handler) {
        setPushListener(MessageType.INVITE_INCOMING,
            msg -> handler.accept(parsePayload(msg, MatchDTOs.InviteIncomingPayload.class)));
    }

    /** The person you invited declined. */
    public void onInviteRejected(Consumer<MatchDTOs.InviteRejectedPayload> handler) {
        setPushListener(MessageType.INVITE_REJECTED,
            msg -> handler.accept(parsePayload(msg, MatchDTOs.InviteRejectedPayload.class)));
    }

    /** A match is ready — via accepted invite or random pairing. Fires for both participants. */
    public void onMatchFound(Consumer<MatchDTOs.MatchFoundPayload> handler) {
        setPushListener(MessageType.MATCH_FOUND,
            msg -> handler.accept(parsePayload(msg, MatchDTOs.MatchFoundPayload.class)));
    }

    /** Phase 3 hook: send something to your current match opponent (game state, a reaction...).
     *  The server relays it blind based on matchId — it doesn't interpret innerPayloadJson. */
    public void sendMatchMessage(String matchId, String innerPayloadJson) {
        sendRequest(MessageType.MATCH_MESSAGE, new MatchDTOs.MatchMessageEnvelope(matchId, innerPayloadJson), null);
    }

    /** Phase 3 hook: receive whatever your opponent sends during an active match. */
    public void onMatchMessage(Consumer<MatchDTOs.MatchMessageEnvelope> handler) {
        setPushListener(MessageType.MATCH_MESSAGE,
            msg -> handler.accept(parsePayload(msg, MatchDTOs.MatchMessageEnvelope.class)));
    }
}
