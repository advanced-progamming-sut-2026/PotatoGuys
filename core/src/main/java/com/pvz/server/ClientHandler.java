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
import com.pvz.network.NetworkClient.LoginRequest;
import com.pvz.network.NetworkMessage;
import com.pvz.utils.PasswordUtils;
import com.pvz.utils.SaveManager;

/**
 * Handles one connected client for its whole lifetime (one thread per client
 * — fine at this scale; a course project won't need a thread pool).
 *
 * All reads/writes to "users/username.json" go through {@link #ACCOUNTS_LOCK}
 * so two clients registering/saving at the same instant can't corrupt it —
 * SaveManager itself does plain unsynchronized file I/O.
 */
public class ClientHandler implements Runnable {

    /** Guards read-modify-write access to the shared username index and user files. */
    private static final Object ACCOUNTS_LOCK = new Object();

    private final Socket socket;
    private final Gson gson = new Gson();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(new java.io.OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                NetworkMessage request = gson.fromJson(line, NetworkMessage.class);
                NetworkMessage response = handle(request);
                out.println(gson.toJson(response));
            }
        } catch (IOException e) {
            System.out.println("[ClientHandler] Client disconnected: " + socket.getRemoteSocketAddress());
        } finally {
            try { socket.close(); } catch (IOException ignored) { }
        }
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
                default:
                    return NetworkMessage.error(request, "Unknown message type: " + request.type);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return NetworkMessage.error(request, "Server error: " + e.getMessage());
        }
    }

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

        return NetworkMessage.ok(request, gson.toJson(user));
    }

    private NetworkMessage handleSaveUser(NetworkMessage request) {
        User updated = gson.fromJson(request.payload, User.class);
        if (updated.getId() == null || updated.getId().isEmpty()) {
            return NetworkMessage.error(request, "Cannot save a user with no id.");
        }

        synchronized (ACCOUNTS_LOCK) {
            SaveManager.getInstance().save(updated, "users/" + updated.getId() + ".json");
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
}
