package com.pvz.server;

import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.pvz.network.PlayerRole;

/**
 * All server-side matchmaking state, shared across every {@link ClientHandler}
 * thread. Everything here is static since there's exactly one of these per
 * server process (unlike ClientHandler, which is one-per-connection).
 */
public class MatchmakingRegistry {

    /** Guards the random queue (a plain LinkedList isn't thread-safe on its own). */
    private static final Object QUEUE_LOCK = new Object();

    /** username -> the handler currently serving that logged-in user. */
    private static final Map<String, ClientHandler> ONLINE = new ConcurrentHashMap<>();

    /** Players waiting for a random opponent, FIFO. */
    private static final LinkedList<ClientHandler> RANDOM_QUEUE = new LinkedList<>();

    /** matchId -> an invite waiting for the invitee's accept/reject. */
    private static final Map<String, PendingInvite> PENDING_INVITES = new ConcurrentHashMap<>();

    /** matchId -> the two participants of an accepted/established match (for MATCH_MESSAGE relay). */
    private static final Map<String, ClientHandler[]> ACTIVE_MATCHES = new ConcurrentHashMap<>();

    private MatchmakingRegistry() { }

    // ---- Online presence ----------------------------------------------------------

    public static void markOnline(String username, ClientHandler handler) {
        ONLINE.put(username, handler);
    }

    public static void markOffline(String username, ClientHandler handler) {
        if (username != null) {
            ONLINE.remove(username, handler);
        }
        synchronized (QUEUE_LOCK) {
            RANDOM_QUEUE.remove(handler);
        }

        // Notify opponents in active matches and clean up.
        var ended = new java.util.ArrayList<String>();
        for (var entry : ACTIVE_MATCHES.entrySet()) {
            ClientHandler[] pair = entry.getValue();
            if (pair[0] == handler || pair[1] == handler) {
                ClientHandler other = pair[0] == handler ? pair[1] : pair[0];
                if (other != null) {
                    other.push(com.pvz.network.MessageType.OPPONENT_DISCONNECTED, null);
                }
                ended.add(entry.getKey());
            }
        }
        for (String matchId : ended) {
            ACTIVE_MATCHES.remove(matchId);
        }
    }

    public static ClientHandler findOnline(String username) {
        return ONLINE.get(username);
    }

    // ---- Specific-opponent invites --------------------------------------------------

    public static class PendingInvite {
        public final String matchId;
        public final ClientHandler from;
        public final ClientHandler to;
        public final PlayerRole fromRole;

        public PendingInvite(String matchId, ClientHandler from, ClientHandler to, PlayerRole fromRole) {
            this.matchId = matchId;
            this.from = from;
            this.to = to;
            this.fromRole = fromRole;
        }
    }

    public static PendingInvite createInvite(ClientHandler from, ClientHandler to, PlayerRole fromRole) {
        String matchId = UUID.randomUUID().toString();
        PendingInvite invite = new PendingInvite(matchId, from, to, fromRole);
        PENDING_INVITES.put(matchId, invite);
        return invite;
    }

    /** Removes and returns the invite so it can only be answered once. Null if already handled/expired. */
    public static PendingInvite consumeInvite(String matchId) {
        return PENDING_INVITES.remove(matchId);
    }

    // ---- Random queue ----------------------------------------------------------------

    /**
     * Tries to pair {@code handler} with whoever's waiting. Returns the opponent to
     * pair with, or null if there was nobody waiting (in which case {@code handler}
     * itself is now the one waiting in the queue).
     */
    public static ClientHandler joinRandomQueue(ClientHandler handler) {
        synchronized (QUEUE_LOCK) {
            ClientHandler waiting = RANDOM_QUEUE.poll();
            if (waiting == null || waiting == handler) {
                if (!RANDOM_QUEUE.contains(handler)) {
                    RANDOM_QUEUE.add(handler);
                }
                return null;
            }
            return waiting;
        }
    }

    public static void leaveRandomQueue(ClientHandler handler) {
        synchronized (QUEUE_LOCK) {
            RANDOM_QUEUE.remove(handler);
        }
    }

    // ---- Active matches (for MATCH_MESSAGE relay, Phase 3) ----------------------------

    public static void registerMatch(String matchId, ClientHandler a, ClientHandler b) {
        ACTIVE_MATCHES.put(matchId, new ClientHandler[]{a, b});
    }

    public static ClientHandler otherParticipant(String matchId, ClientHandler self) {
        ClientHandler[] pair = ACTIVE_MATCHES.get(matchId);
        if (pair == null) return null;
        return pair[0] == self ? pair[1] : pair[0];
    }

    public static void endMatch(String matchId) {
        ACTIVE_MATCHES.remove(matchId);
    }
}
