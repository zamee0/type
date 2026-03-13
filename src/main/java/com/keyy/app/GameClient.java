package com.keyy.app;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class GameClient {

    private Socket socket;
    private PrintWriter out;
    private String username;

    private Consumer<String> onGameStart;
    private Consumer<String> onProgressUpdate;
    private Consumer<String> onResult;
    private Consumer<String> onLog;
    private Runnable onDisconnect;

    public GameClient(String username) {
        this.username = username;
    }

    public void setOnGameStart(Consumer<String> cb)      { this.onGameStart = cb; }
    public void setOnProgressUpdate(Consumer<String> cb) { this.onProgressUpdate = cb; }
    public void setOnResult(Consumer<String> cb)         { this.onResult = cb; }
    public void setOnLog(Consumer<String> cb)            { this.onLog = cb; }
    public void setOnDisconnect(Runnable cb)             { this.onDisconnect = cb; }

    public void connect(String host) throws IOException {
        socket = new Socket(host, GameServer.PORT);
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        out.println("JOIN|" + username);

        Thread readThread = new Thread(() -> {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    handleMessage(line);
                }
            } catch (IOException e) {
                if (onDisconnect != null)
                    javafx.application.Platform.runLater(onDisconnect);
            }
        });
        readThread.setDaemon(true);
        readThread.start();
    }

    private void handleMessage(String msg) {
        if (msg.startsWith("START|")) {
            String data = msg.substring(6);
            if (onGameStart != null)
                javafx.application.Platform.runLater(() -> onGameStart.accept(data));
        } else if (msg.startsWith("PROGRESS|")) {
            if (onProgressUpdate != null)
                javafx.application.Platform.runLater(() -> onProgressUpdate.accept(msg.substring(9)));
        } else if (msg.startsWith("RESULT|")) {
            if (onResult != null)
                javafx.application.Platform.runLater(() -> onResult.accept(msg.substring(7)));
        } else if (msg.equals("STOP")) {
            if (onDisconnect != null)
                javafx.application.Platform.runLater(onDisconnect);
        }
    }

    public void sendProgress(int wordsCompleted) {
        if (out != null) out.println("PROGRESS|" + wordsCompleted);
    }

    public void sendResult(double wpm, double accuracy) {
        if (out != null) out.println("RESULT|" + String.format("%.0f|%.1f", wpm, accuracy));
    }

    public void disconnect() {
        try { if (socket != null) socket.close(); } catch (IOException e) { e.printStackTrace(); }
    }
}