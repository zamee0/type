package com.keyy.app;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.*;

public class LocalhostController {

    @FXML private Button backBtn;
    @FXML private Button hostBtn;
    @FXML private Button joinBtn;

    @FXML private VBox hostPane;
    @FXML private VBox joinPane;

    @FXML private Button diffEasy, diffNormal, diffHard;
    @FXML private Button time15, time30, time60, time120;

    @FXML private Label hostIpLabel;
    @FXML private Button copyIpBtn;
    @FXML private Label hostStatusLabel;
    @FXML private VBox playerListBox;
    @FXML private Button startGameBtn;

    @FXML private TextField joinIpField;
    @FXML private Button connectBtn;
    @FXML private Label joinStatusLabel;

    private String username;
    private GameServer server;
    private GameClient client;

    private String selectedDifficulty = "Normal";
    private int selectedSeconds = 60;

    @FXML
    public void initialize() {
        hostPane.setVisible(false);
        hostPane.setManaged(false);
        joinPane.setVisible(false);
        joinPane.setManaged(false);

        backBtn.setOnAction(e -> goBack());
        hostBtn.setOnAction(e -> showHost());
        joinBtn.setOnAction(e -> showJoin());
        startGameBtn.setOnAction(e -> startGame());
        connectBtn.setOnAction(e -> joinGame());

        setupDiffButtons();
        setupTimeButtons();
    }

    public void setUsername(String u) { this.username = u; }

    private void setupDiffButtons() {
        Button[] btns = {diffEasy, diffNormal, diffHard};
        String[] vals = {"Easy", "Normal", "Hard"};
        for (int i = 0; i < btns.length; i++) {
            final String val = vals[i];
            btns[i].setOnAction(e -> {
                selectedDifficulty = val;
                for (Button b : btns) b.getStyleClass().remove("setup-btn-active");
                ((Button) e.getSource()).getStyleClass().add("setup-btn-active");
            });
        }
        diffNormal.getStyleClass().add("setup-btn-active");
    }

    private void setupTimeButtons() {
        Button[] btns = {time15, time30, time60, time120};
        int[] vals = {15, 30, 60, 120};
        for (int i = 0; i < btns.length; i++) {
            final int val = vals[i];
            btns[i].setOnAction(e -> {
                selectedSeconds = val;
                for (Button b : btns) b.getStyleClass().remove("setup-btn-active");
                ((Button) e.getSource()).getStyleClass().add("setup-btn-active");
            });
        }
        time60.getStyleClass().add("setup-btn-active");
    }

    private void showHost() {
        joinPane.setVisible(false);
        joinPane.setManaged(false);
        hostPane.setVisible(true);
        hostPane.setManaged(true);

        if (server != null) server.stop();

        try {
            server = new GameServer();
            server.setOnLog(msg -> hostStatusLabel.setText(msg));
            server.setOnPlayersUpdated(players -> {
                playerListBox.getChildren().clear();
                Label self = new Label("• " + username + " (You - Host)");
                self.getStyleClass().add("history-detail");
                playerListBox.getChildren().add(self);
                for (String p : players) {
                    Label lbl = new Label("• " + p);
                    lbl.getStyleClass().add("history-detail");
                    playerListBox.getChildren().add(lbl);
                }
                startGameBtn.setDisable(players.isEmpty());
            });

            server.start();
            String ip = GameServer.getLocalIP();
            hostIpLabel.setText("Your IP: " + ip);
            hostStatusLabel.setText("Waiting for players...");
            startGameBtn.setDisable(true);

            Label self = new Label("• " + username + " (You - Host)");
            self.getStyleClass().add("history-detail");
            playerListBox.getChildren().add(self);

            copyIpBtn.setOnAction(ev -> {
                Clipboard cb = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(ip);
                cb.setContent(content);
                copyIpBtn.setText("Copied!");
                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(e2 -> copyIpBtn.setText("Copy"));
                pause.play();
            });

        } catch (Exception ex) {
            hostStatusLabel.setText("Error: " + ex.getMessage());
        }
    }

    private void showJoin() {
        hostPane.setVisible(false);
        hostPane.setManaged(false);
        joinPane.setVisible(true);
        joinPane.setManaged(true);
        joinStatusLabel.setText("Enter host IP and connect.");
    }

    private void joinGame() {
        String ip = joinIpField.getText().trim();
        if (ip.isEmpty()) { joinStatusLabel.setText("Enter an IP address."); return; }

        client = new GameClient(username);
        client.setOnGameStart(data -> {
            String[] parts = data.split("\\|", 2);
            int seconds = Integer.parseInt(parts[0]);
            String words = parts[1];
            try {
                Stage stage = (Stage) backBtn.getScene().getWindow();
                MultiplayerController ctrl = SceneHelper.loadScene(
                        stage, "multiplayer-view.fxml", "KEYY — Multiplayer");
                ctrl.setup(username, words, seconds, client, false);
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        client.setOnDisconnect(() -> joinStatusLabel.setText("Disconnected from host."));

        try {
            client.connect(ip);
            joinStatusLabel.setText("Connected! Waiting for host to start...");
            connectBtn.setDisable(true);
        } catch (Exception ex) {
            joinStatusLabel.setText("Could not connect: " + ex.getMessage());
        }
    }

    private void startGame() {
        if (server == null) return;
        try {
            List<String> words = buildWordList(50);
            String wordStr = String.join(" ", words);
            Stage stage = (Stage) backBtn.getScene().getWindow();
            MultiplayerController ctrl = SceneHelper.loadScene(
                    stage, "multiplayer-view.fxml", "KEYY — Multiplayer");
            ctrl.setup(username, wordStr, selectedSeconds, null, true);
            server.startGame(wordStr, selectedSeconds);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private List<String> buildWordList(int count) {
        Random rand = new Random();
        String[] bank = switch (selectedDifficulty) {
            case "Easy" -> WordBank.EASY;
            case "Hard" -> WordBank.HARD;
            default     -> WordBank.NORMAL;
        };
        List<String> list = new ArrayList<>();
        for (int i = 0; i < count; i++) list.add(bank[rand.nextInt(bank.length)]);
        return list;
    }

    private void goBack() {
        if (server != null) server.stop();
        if (client != null) client.disconnect();
        try {
            Stage stage = (Stage) backBtn.getScene().getWindow();
            dashboardcontrol ctrl = SceneHelper.loadScene(stage, "dashboard-view.fxml", "KEYY");
            ctrl.setUsername(username);
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}