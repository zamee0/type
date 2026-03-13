package com.keyy.app;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class GameServer {

    public static final int PORT = 55555;
    private static final int MAX_PLAYERS = 4;

    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private boolean gameStarted = false;

    private Consumer<String> onLog;
    private Consumer<List<String>> onPlayersUpdated;
    private Runnable onGameStart;

    public GameServer() {
    }

    public void setOnLog(Consumer<String> cb)                    { this.onLog = cb; }
    public void setOnPlayersUpdated(Consumer<List<String>> cb)   { this.onPlayersUpdated = cb; }
    public void setOnGameStart(Runnable cb)                      { this.onGameStart = cb; }

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        log("Server started on port " + PORT);

        Thread acceptThread = new Thread(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    if (clients.size() >= MAX_PLAYERS) {
                        socket.close();
                        continue;
                    }
                    ClientHandler handler = new ClientHandler(socket);
                    clients.add(handler);
                    new Thread(handler).start();
                    log("Player connected: " + socket.getInetAddress());
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) e.printStackTrace();
                }
            }
        });
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    public void startGame(String words, int seconds) {
        gameStarted = true;
        broadcast("START|" + seconds + "|" + words);
        if (onGameStart != null) javafx.application.Platform.runLater(onGameStart);
    }

    public void stop() {
        try {
            broadcast("STOP");
            for (ClientHandler c : clients) c.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public List<String> getConnectedPlayers() {
        List<String> names = new ArrayList<>();
        for (ClientHandler c : clients) if (c.username != null) names.add(c.username);
        return names;
    }

    private void broadcast(String msg) {
        for (ClientHandler c : clients) c.send(msg);
    }

    private void log(String msg) {
        if (onLog != null) javafx.application.Platform.runLater(() -> onLog.accept(msg));
    }

    class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        String username;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                String line;
                while ((line = in.readLine()) != null) {
                    handleMessage(line);
                }
            } catch (IOException e) {
                log(username + " disconnected.");
            } finally {
                clients.remove(this);
                notifyPlayerList();
            }
        }

        private void handleMessage(String msg) {
            if (msg.startsWith("JOIN|")) {
                username = msg.substring(5);
                log(username + " joined.");
                notifyPlayerList();
            } else if (msg.startsWith("PROGRESS|")) {
                broadcast(msg + "|" + username);
            } else if (msg.startsWith("RESULT|")) {
                broadcast(msg + "|" + username);
                log(username + " finished: " + msg.substring(7));
            }
        }

        void send(String msg) {
            if (out != null) out.println(msg);
        }

        void close() {
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void notifyPlayerList() {
        if (onPlayersUpdated != null) {
            List<String> names = getConnectedPlayers();
            javafx.application.Platform.runLater(() -> onPlayersUpdated.accept(names));
        }
    }

    public static String getLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "Unknown";
        }
    }
}