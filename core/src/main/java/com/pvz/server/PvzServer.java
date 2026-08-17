package com.pvz.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Phase-1 server: owns the save directory (accounts + leaderboard data) and
 * answers REGISTER / LOGIN / SAVE_USER / GET_USER / GET_LEADERBOARD requests.
 *
 * This class has no libGDX dependency and doesn't need a graphics context —
 * run it as a plain "java -cp ... com.pvz.server.PvzServer" process, separate
 * from the game client. It reuses your existing SaveManager/User/Leaderboard
 * classes as-is, so it must run with the same working directory the client
 * normally uses (Constants.SAVE_PATH is a relative path — start the server
 * from your project root, same place you'd normally launch the desktop
 * client from, e.g. via your Gradle project's root directory).
 *
 * IMPORTANT: this becomes the single source of truth for account data.
 * Once this is wired in, the desktop client should stop writing directly
 * to "users/*.json" itself — see the integration notes for User.saveUser(),
 * LoginMenu, RegisterMenu, and LeaderBoardController.
 */
public class PvzServer {

    public static final int DEFAULT_PORT = 5454;

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[PvzServer] Listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[PvzServer] Client connected: " + clientSocket.getRemoteSocketAddress());

                Thread handlerThread = new Thread(new ClientHandler(clientSocket), "client-" + clientSocket.getPort());
                handlerThread.setDaemon(true);
                handlerThread.start();
            }
        } catch (IOException e) {
            System.err.println("[PvzServer] Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
