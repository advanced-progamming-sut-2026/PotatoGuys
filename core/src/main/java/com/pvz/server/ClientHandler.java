package com.pvz.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;

import com.pvz.models.leaderboard.Leaderboard;
import com.pvz.models.leaderboard.Leaderboard.LeaderBoardEntry;
import com.pvz.models.user.User;
import com.pvz.network.MatchDTOs;
import com.pvz.network.MessageType;
import com.pvz.network.NetworkClient.LoginRequest;
import com.pvz.network.NetworkMessage;
import com.pvz.network.PlayerRole;
import com.pvz.utils.PasswordUtils;
import com.pvz.utils.SaveManager;

/**
 * Handles one connected client for its whole lifetime (one thread per client
 * — fine at this scale; a course project won't need a thread pool).
 *
 * All reads/writes to "users/username.json" go through {@link #ACCOUNTS_LOCK}
 * so two clients registering/saving at the same instant can't corrupt it —
 * SaveManager itself does plain unsynchronized file I/O.
 *
 * Phase 2: once a client logs in, this handler registers itself in
 * {@link MatchmakingRegistry} under that username, so other clients' invites
 * and the random queue can reach it — including from a *different* handler's
 * thread, hence {@link #out} is now an instance field guarded by
 * {@link #push}, not a local variable.
 */
public class ClientHandler implements Runnable {

    /** Guards read-modify-write access to the shared username index and user files. */
    private static final Object ACCOUNTS_LOCK = new Object();

    private final Socket socket;
    private final Gson gson = new Gson();

    /** Set once LOGIN succeeds. Read from other handlers' threads too — see class doc. */
    private volatile String username;
    private volatile PrintWriter out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
        ) {
            out = new PrintWriter(
                    new java.io.OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            String line;
            while ((line = in.readLine()) != null) {
                NetworkMessage request = gson.fromJson(line, NetworkMessage.class);
                // A null type means the enum name on the wire doesn't exist in this server's
                // MessageType (e.g. an old server process running against a newer client).
                // Gson silently maps unknown enum names to null instead of throwing, so guard
                // here with a readable error instead of an NPE in the switch below.
                if (request.type == null) {
                    System.err.println("[ClientHandler] Received message with null type: " + line);
                    out.println(gson.toJson(NetworkMessage.error(request,
                            "Unknown or missing message type. "
                            + "Make sure client and server are running the same build.")));
                    continue;
                }
                NetworkMessage response = handle(request);
                out.println(gson.toJson(response));
            }
        } catch (IOException e) {
            System.out.println("[ClientHandler] Client disconnected: " + socket.getRemoteSocketAddress());
        } finally {
            MatchmakingRegistry.markOffline(username, this);
            try { socket.close(); } catch (IOException ignored) { }
        }
    }

    /** Writes a message to this client without it being a response to any request —
     *  used to notify a client about something another client (or the server) did. */
    public synchronized void push(MessageType type, String payloadJson) {
        if (out == null) return;
        NetworkMessage msg = new NetworkMessage();
        msg.type = type;
        msg.requestId = null;
        msg.success = true;
        msg.payload = payloadJson;
        out.println(gson.toJson(msg));
    }

    private NetworkMessage handle(NetworkMessage request) {
        try {
            switch (request.type) {
                case REGISTER:
                    return handleRegister(request);
                case LOGIN:
                    return handleLogin(request);
                case SAVE_USER:
                    return handleSaveUser(request);
                case GET_USER:
                    return handleGetUser(request);
                case GET_LEADERBOARD:
                    return handleGetLeaderboard(request);
                case CHECK_USERNAME:
                    return handleCheckUsername(request);
                case FORGOT_PASSWORD:
                    return handleForgotPassword(request);
                case RESET_PASSWORD:
                    return handleResetPassword(request);
                case UPDATE_PROFILE:
                    return handleUpdateProfile(request);
                case INVITE_SEND:
                    return handleInviteSend(request);
                case INVITE_RESPONSE:
                    return handleInviteResponse(request);
                case QUEUE_JOIN:
                    return handleQueueJoin(request);
                case QUEUE_CANCEL:
                    return handleQueueCancel(request);
                case MATCH_MESSAGE:
                    return handleMatchMessage(request);
                default:
                    return NetworkMessage.error(request, "Unknown message type: " + request.type);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return NetworkMessage.error(request, "Server error: " + e.getMessage());
        }
    }

    // ---- Accounts (Phase 1, unchanged) --------------------------------------------------

    @SuppressWarnings("unchecked")
    private NetworkMessage handleRegister(NetworkMessage request) {
        User incoming = gson.fromJson(request.payload, User.class);

        synchronized (ACCOUNTS_LOCK) {
            HashMap<String, String> usernames = SaveManager.getInstance().load("users/username.json", HashMap.class);
            if (usernames == null) {
                usernames = new HashMap<>();
            }
            if (usernames.containsKey(incoming.getUsername())) {
                return NetworkMessage.error(request, "Username is already taken!");
            }

            String id = UUID.randomUUID().toString();
            incoming.setId(id);

            usernames.put(incoming.getUsername(), id);
            SaveManager.getInstance().save(usernames, "users/username.json");
            SaveManager.getInstance().save(incoming, "users/" + id + ".json");
        }

        return NetworkMessage.ok(request, gson.toJson(incoming));
    }

    @SuppressWarnings("unchecked")
    private NetworkMessage handleLogin(NetworkMessage request) {
        LoginRequest req = gson.fromJson(request.payload, LoginRequest.class);

        HashMap<String, String> usernames = SaveManager.getInstance().load("users/username.json", HashMap.class);
        if (usernames == null || !usernames.containsKey(req.username)) {
            return NetworkMessage.error(request, "Username is incorrect!");
        }
        String id = usernames.get(req.username);

        User user = SaveManager.getInstance().load("users/" + id + ".json", User.class);
        if (user == null) {
            return NetworkMessage.error(request, "User data not found.");
        }
        if (!PasswordUtils.verifyPassword(req.password, user.getPasswordHash())) {
            return NetworkMessage.error(request, "Password is incorrect!");
        }

        user.refreshQuestLog();
        synchronized (ACCOUNTS_LOCK) {
            SaveManager.getInstance().save(user, "users/" + id + ".json");
        }

        String oldUsername = this.username;
        this.username = user.getUsername();
        if (oldUsername != null && !oldUsername.equals(this.username)) {
            MatchmakingRegistry.markOffline(oldUsername, this);
        }
        MatchmakingRegistry.markOnline(this.username, this);

        return NetworkMessage.ok(request, gson.toJson(user));
    }

    private NetworkMessage handleSaveUser(NetworkMessage request) {
        User updated = gson.fromJson(request.payload, User.class);
        if (updated.getId() == null || updated.getId().isEmpty()) {
            return NetworkMessage.error(request, "Cannot save a user with no id.");
        }

        synchronized (ACCOUNTS_LOCK) {
            User stored = SaveManager.getInstance().load("users/" + updated.getId() + ".json", User.class);
            String oldUsername = stored != null ? stored.getUsername() : null;
            String newUsername = updated.getUsername();

            if (oldUsername != null && !oldUsername.equals(newUsername)) {
                // Username changed: keep the login index in sync so the NEW name
                // is what works at login (otherwise the old name keeps working and
                // the new one gets "Username is incorrect!").
                HashMap<String, String> usernames = SaveManager.getInstance()
                        .load("users/username.json", HashMap.class);
                if (usernames == null) {
                    usernames = new HashMap<>();
                }
                if (usernames.containsKey(newUsername)
                        && !usernames.get(newUsername).equals(updated.getId())) {
                    return NetworkMessage.error(request, "Username is already taken!");
                }
                if (updated.getId().equals(usernames.get(oldUsername))) {
                    usernames.remove(oldUsername);
                }
                usernames.put(newUsername, updated.getId());
                SaveManager.getInstance().save(usernames, "users/username.json");
            }

            SaveManager.getInstance().save(updated, "users/" + updated.getId() + ".json");
        }

        // If this connection is logged in as the old name, follow the rename so
        // invites/matchmaking keep addressing this player by the current username.
        if (this.username != null && !this.username.equals(updated.getUsername())) {
            String old = this.username;
            this.username = updated.getUsername();
            MatchmakingRegistry.markOffline(old, this);
            MatchmakingRegistry.markOnline(this.username, this);
        }

        return NetworkMessage.ok(request, null);
    }

    @SuppressWarnings("unchecked")
    private NetworkMessage handleGetUser(NetworkMessage request) {
        String username = gson.fromJson(request.payload, String.class);

        HashMap<String, String> usernames = SaveManager.getInstance().load("users/username.json", HashMap.class);
        if (usernames == null || !usernames.containsKey(username)) {
            return NetworkMessage.error(request, "No such user.");
        }
        String id = usernames.get(username);
        User user = SaveManager.getInstance().load("users/" + id + ".json", User.class);
        if (user == null) {
            return NetworkMessage.error(request, "User data not found.");
        }

        return NetworkMessage.ok(request, gson.toJson(user));
    }

    private NetworkMessage handleGetLeaderboard(NetworkMessage request) {
        List<LeaderBoardEntry> entries = Leaderboard.loadAll();
        return NetworkMessage.ok(request, gson.toJson(entries));
    }

    @SuppressWarnings("unchecked")
    private NetworkMessage handleCheckUsername(NetworkMessage request) {
        MatchDTOs.CheckUsernameRequest req = gson.fromJson(request.payload, MatchDTOs.CheckUsernameRequest.class);
        synchronized (ACCOUNTS_LOCK) {
            HashMap<String, String> usernames = SaveManager.getInstance().load("users/username.json", HashMap.class);
            boolean taken = usernames != null && usernames.containsKey(req.username);
            return NetworkMessage.ok(request, gson.toJson(taken));
        }
    }

    @SuppressWarnings("unchecked")
    private NetworkMessage handleForgotPassword(NetworkMessage request) {
        MatchDTOs.ResetPasswordRequest req = gson.fromJson(request.payload, MatchDTOs.ResetPasswordRequest.class);

        HashMap<String, String> usernames = SaveManager.getInstance().load("users/username.json", HashMap.class);
        if (usernames == null || !usernames.containsKey(req.username)) {
            return NetworkMessage.error(request, "Username not found.");
        }
        String id = usernames.get(req.username);
        User user = SaveManager.getInstance().load("users/" + id + ".json", User.class);
        if (user == null) {
            return NetworkMessage.error(request, "User data not found.");
        }
        if (!user.getEmail().equals(req.email)) {
            return NetworkMessage.error(request, "Email is incorrect!");
        }

        MatchDTOs.ForgotPasswordPayload payload = new MatchDTOs.ForgotPasswordPayload();
        payload.securityQuestion = user.getSecurityQuestion();
        return NetworkMessage.ok(request, gson.toJson(payload));
    }

    @SuppressWarnings("unchecked")
    private NetworkMessage handleResetPassword(NetworkMessage request) {
        MatchDTOs.ResetPasswordRequest req = gson.fromJson(request.payload, MatchDTOs.ResetPasswordRequest.class);

        HashMap<String, String> usernames = SaveManager.getInstance().load("users/username.json", HashMap.class);
        if (usernames == null || !usernames.containsKey(req.username)) {
            return NetworkMessage.error(request, "Username not found.");
        }
        String id = usernames.get(req.username);

        synchronized (ACCOUNTS_LOCK) {
            User user = SaveManager.getInstance().load("users/" + id + ".json", User.class);
            if (user == null) {
                return NetworkMessage.error(request, "User data not found.");
            }
            user.setPasswordHash(PasswordUtils.hashPassword(req.newPassword));
            SaveManager.getInstance().save(user, "users/" + id + ".json");
        }

        return NetworkMessage.ok(request, null);
    }

    @SuppressWarnings("unchecked")
    private NetworkMessage handleUpdateProfile(NetworkMessage request) {
        MatchDTOs.UpdateProfileRequest req = gson.fromJson(request.payload, MatchDTOs.UpdateProfileRequest.class);

        synchronized (ACCOUNTS_LOCK) {
            HashMap<String, String> usernames = SaveManager.getInstance().load("users/username.json", HashMap.class);
            if (usernames == null) {
                usernames = new HashMap<>();
            }

            User user = SaveManager.getInstance().load("users/" + req.userId + ".json", User.class);
            if (user == null) {
                return NetworkMessage.error(request, "User not found.");
            }

            if (req.newUsername != null && !req.newUsername.isEmpty()
                    && !req.newUsername.equals(user.getUsername())) {
                // The new name must not belong to a *different* account.
                String existingId = usernames.get(req.newUsername);
                if (existingId != null && !existingId.equals(req.userId)) {
                    return NetworkMessage.error(request, "Username is already taken!");
                }
                // Remove EVERY mapping that points at this user id (old username —
                // possibly stale/mismatched from the client), then record the new name.
                // Keyed by userId, not by the client-supplied oldUsername, so a rename
                // can never leave the old key behind in username.json.
                usernames.entrySet().removeIf(e -> e.getValue().equals(req.userId));
                usernames.put(req.newUsername, req.userId);
                SaveManager.getInstance().save(usernames, "users/username.json");
            }

            if (req.newUsername != null) user.setUsername(req.newUsername);
            if (req.newNickname != null) user.setNickName(req.newNickname);
            if (req.newEmail != null) user.setEmail(req.newEmail);
            SaveManager.getInstance().save(user, "users/" + req.userId + ".json");

            return NetworkMessage.ok(request, gson.toJson(user));
        }
    }

    // ---- I,Zombie matchmaking (Phase 2) -----------------------------------------------

    private NetworkMessage handleInviteSend(NetworkMessage request) {
        MatchDTOs.InviteSendRequest req = gson.fromJson(request.payload, MatchDTOs.InviteSendRequest.class);

        ClientHandler target = MatchmakingRegistry.findOnline(req.targetUsername);
        if (target == null) {
            return NetworkMessage.error(request, "That user is offline or doesn't exist.");
        }
        if (this.username.equals(req.targetUsername)) {
            return NetworkMessage.error(request, "You can't invite yourself.");
        }

        MatchmakingRegistry.PendingInvite invite =
            MatchmakingRegistry.createInvite(this, target, req.requestedRole);

        MatchDTOs.InviteIncomingPayload pushPayload = new MatchDTOs.InviteIncomingPayload();
        pushPayload.matchId = invite.matchId;
        pushPayload.fromUsername = this.username;
        pushPayload.yourRole = req.requestedRole.opposite();
        target.push(MessageType.INVITE_INCOMING, gson.toJson(pushPayload));

        return NetworkMessage.ok(request, gson.toJson(invite.matchId));
    }

    private NetworkMessage handleInviteResponse(NetworkMessage request) {
        MatchDTOs.InviteResponseRequest req = gson.fromJson(request.payload, MatchDTOs.InviteResponseRequest.class);

        MatchmakingRegistry.PendingInvite invite = MatchmakingRegistry.consumeInvite(req.matchId);
        if (invite == null) {
            return NetworkMessage.error(request, "This invite is no longer valid.");
        }

        if (!req.accepted) {
            MatchDTOs.InviteRejectedPayload rejected = new MatchDTOs.InviteRejectedPayload();
            rejected.matchId = invite.matchId;
            rejected.byUsername = this.username;
            invite.from.push(MessageType.INVITE_REJECTED, gson.toJson(rejected));
            return NetworkMessage.ok(request, null);
        }

        MatchmakingRegistry.registerMatch(invite.matchId, invite.from, invite.to);

        MatchDTOs.MatchFoundPayload toInviter = new MatchDTOs.MatchFoundPayload();
        toInviter.matchId = invite.matchId;
        toInviter.opponentUsername = invite.to.username;
        toInviter.yourRole = invite.fromRole;
        invite.from.push(MessageType.MATCH_FOUND, gson.toJson(toInviter));

        MatchDTOs.MatchFoundPayload toInvitee = new MatchDTOs.MatchFoundPayload();
        toInvitee.matchId = invite.matchId;
        toInvitee.opponentUsername = invite.from.username;
        toInvitee.yourRole = invite.fromRole.opposite();
        invite.to.push(MessageType.MATCH_FOUND, gson.toJson(toInvitee));

        return NetworkMessage.ok(request, null);
    }

    private NetworkMessage handleQueueJoin(NetworkMessage request) {
        ClientHandler opponent = MatchmakingRegistry.joinRandomQueue(this);

        if (opponent != null) {
            String matchId = UUID.randomUUID().toString();
            MatchmakingRegistry.registerMatch(matchId, opponent, this);

            // Whoever queued first defends (arbitrary but deterministic — no real reason
            // it couldn't be a coin flip instead).
            MatchDTOs.MatchFoundPayload toOpponent = new MatchDTOs.MatchFoundPayload();
            toOpponent.matchId = matchId;
            toOpponent.opponentUsername = this.username;
            toOpponent.yourRole = PlayerRole.PLANT;
            opponent.push(MessageType.MATCH_FOUND, gson.toJson(toOpponent));

            MatchDTOs.MatchFoundPayload toSelf = new MatchDTOs.MatchFoundPayload();
            toSelf.matchId = matchId;
            toSelf.opponentUsername = opponent.username;
            toSelf.yourRole = PlayerRole.ZOMBIE;
            this.push(MessageType.MATCH_FOUND, gson.toJson(toSelf));
        }
        // If opponent == null, `this` is now waiting in the queue — the eventual
        // MATCH_FOUND arrives later as a push, whenever someone else joins.

        return NetworkMessage.ok(request, null);
    }

    private NetworkMessage handleQueueCancel(NetworkMessage request) {
        MatchmakingRegistry.leaveRandomQueue(this);
        return NetworkMessage.ok(request, null);
    }

    private NetworkMessage handleMatchMessage(NetworkMessage request) {
        MatchDTOs.MatchMessageEnvelope envelope = gson.fromJson(request.payload, MatchDTOs.MatchMessageEnvelope.class);

        ClientHandler other = MatchmakingRegistry.otherParticipant(envelope.matchId, this);
        if (other == null) {
            return NetworkMessage.error(request, "Match not found or already ended.");
        }

        // Blind relay: forward the same envelope untouched, server never looks inside it.
        other.push(MessageType.MATCH_MESSAGE, request.payload);
        return NetworkMessage.ok(request, null);
    }
}
