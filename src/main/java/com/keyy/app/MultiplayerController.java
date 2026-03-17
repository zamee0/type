package com.keyy.app;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.*;

public class MultiplayerController {

    @FXML private VBox rootVBox;
    @FXML private VBox typingPane;
    @FXML private ScrollPane resultPane;
    @FXML private ScrollPane typingScroll;
    @FXML private TextFlow textFlow;
    @FXML private Label timerLabel;
    @FXML private Label wpmLiveLabel;
    @FXML private Label opponentSummaryLabel;
    @FXML private Label resultHeadingLabel;
    @FXML private Label resultSubLabel;
    @FXML private Label wpmResultLabel;
    @FXML private Label accResultLabel;
    @FXML private HBox podiumBox;
    @FXML private VBox rankingBox;
    @FXML private javafx.scene.chart.LineChart<String, Number> wpmChart;
    @FXML private Button dashboardBtn;

    private String username;
    private GameClient client;
    private GameServer server;

    private int currentCharIndex = 0;
    private int wordsCompleted = 0;
    private final Map<Integer, Boolean> hadError = new HashMap<>();
    private int GAME_SECONDS = 60;
    private int timeLeft = GAME_SECONDS;

    private Timeline countdown;
    private Timeline liveWpmTimer;

    private String[] wordBank;
    private int wordBankIndex = 0;

    private final Map<String, Integer> liveProgress = new LinkedHashMap<>();
    private final Map<String, Double> allFinalWpm = new LinkedHashMap<>();
    private final Map<String, Double> allFinalAcc = new LinkedHashMap<>();
    private final Map<String, List<Double>> allWpmHistory = new LinkedHashMap<>();
    private final List<Double> myWpmHistory = new ArrayList<>();
    private int tickCount = 0;

    @FXML
    public void initialize() {
        resultPane.setVisible(false);
        resultPane.setManaged(false);
        dashboardBtn.setOnAction(e -> goToDashboard());
    }

    public void setup(String username, String words, int seconds, GameClient client, boolean isHost) {
        this.username     = username;
        this.client       = client;
        this.GAME_SECONDS = seconds;
        this.timeLeft     = seconds;
        timerLabel.setText(String.valueOf(seconds));

        wordBank = words.split(" ");
        wordBankIndex = 0;

        liveProgress.put(username, 0);

        if (client != null) {
            client.setOnProgressUpdate(this::handleOpponentProgress);
            client.setOnResult(this::handleOpponentResult);
        }

        buildTextFlow();
        startCountdown();
        startLiveWpm();

        javafx.application.Platform.runLater(() -> {
            if (rootVBox.getScene() != null)
                rootVBox.getScene().setOnKeyTyped(this::handleKeyPress);
        });
    }

    public void setServer(GameServer server) {
        this.server = server;
        if (server != null) {
            server.setOnPlayersUpdated(players -> {
                for (String p : players) liveProgress.putIfAbsent(p, 0);
                refreshOpponentSummary();
            });
            server.setOnHostResult(data -> {
                String[] parts = data.split("\\|", 4);
                if (parts.length < 3) return;
                String name = parts[2];
                allFinalWpm.put(name, Double.parseDouble(parts[0]));
                allFinalAcc.put(name, Double.parseDouble(parts[1]));
                if (resultPane.isVisible()) buildPodium();
            });
        }
    }

    private void buildTextFlow() {
        textFlow.getChildren().clear();
        currentCharIndex = 0;
        wordBankIndex = 0;
        appendWords(50);
        highlightCursor();
    }

    private void appendWords(int count) {
        boolean needSpace = !textFlow.getChildren().isEmpty();
        StringBuilder sb = new StringBuilder();
        if (needSpace) sb.append(" ");
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(" ");
            if (wordBankIndex < wordBank.length) {
                sb.append(wordBank[wordBankIndex++]);
            } else {
                wordBankIndex = 0;
                sb.append(wordBank[wordBankIndex++]);
            }
        }
        for (char c : sb.toString().toCharArray()) {
            Text t = new Text(String.valueOf(c));
            t.getStyleClass().add("typing-char");
            textFlow.getChildren().add(t);
        }
    }

    private void highlightCursor() {
        if (currentCharIndex < textFlow.getChildren().size()) {
            Text t = (Text) textFlow.getChildren().get(currentCharIndex);
            t.getStyleClass().clear();
            t.getStyleClass().add("typing-cursor");
        }
    }

    private String fullText() {
        StringBuilder sb = new StringBuilder();
        textFlow.getChildren().forEach(n -> sb.append(((Text) n).getText()));
        return sb.toString();
    }

    private void startCountdown() {
        countdown = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--;
            timerLabel.setText(String.valueOf(timeLeft));
            if (timeLeft <= 10)
                timerLabel.setStyle("-fx-font-size:52px;-fx-font-weight:900;-fx-text-fill:#FF5E57;");
            if (timeLeft <= 0) showResult();
        }));
        countdown.setCycleCount(Timeline.INDEFINITE);
        countdown.play();
    }

    private void startLiveWpm() {
        liveWpmTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            int elapsed = GAME_SECONDS - timeLeft;
            if (elapsed > 0) {
                double currentWpm = wordsCompleted / (elapsed / 60.0);
                wpmLiveLabel.setText(String.format("%.0f WPM", currentWpm));
                tickCount++;
                if (tickCount % 5 == 0) myWpmHistory.add(currentWpm);
            }
        }));
        liveWpmTimer.setCycleCount(Timeline.INDEFINITE);
        liveWpmTimer.play();
    }

    private void handleKeyPress(KeyEvent event) {
        if (timeLeft <= 0) return;
        String typed = event.getCharacter();
        if (typed.equals("\b")) { handleBackspace(); return; }
        if (typed.isEmpty() || typed.charAt(0) < 32) return;

        if (currentCharIndex >= fullText().length() - 20) appendWords(20);

        String full = fullText();
        if (currentCharIndex >= full.length()) return;

        boolean correct = typed.charAt(0) == full.charAt(currentCharIndex);
        if (!correct && !hadError.containsKey(currentCharIndex))
            hadError.put(currentCharIndex, true);

        Text t = (Text) textFlow.getChildren().get(currentCharIndex);
        t.getStyleClass().clear();
        t.getStyleClass().add(correct
                ? (hadError.containsKey(currentCharIndex) ? "typing-corrected" : "typing-correct")
                : "typing-wrong");

        currentCharIndex++;

        if (correct && currentCharIndex > 0 && full.charAt(currentCharIndex - 1) == ' ') {
            wordsCompleted++;
            liveProgress.put(username, wordsCompleted);
            refreshOpponentSummary();
            if (client != null) client.sendProgress(wordsCompleted);
        }

        highlightCursor();
        autoScroll();
    }

    private void autoScroll() {
        javafx.application.Platform.runLater(() -> {
            if (currentCharIndex < textFlow.getChildren().size()) {
                Text cursorNode = (Text) textFlow.getChildren().get(currentCharIndex);
                double nodeY = cursorNode.getBoundsInParent().getMinY();
                double flowH = textFlow.getBoundsInLocal().getHeight();
                double viewH = typingScroll.getViewportBounds().getHeight();
                if (flowH > viewH) {
                    double vval = Math.max(0, Math.min(1, (nodeY - viewH / 2) / (flowH - viewH)));
                    typingScroll.setVvalue(vval);
                }
            }
        });
    }

    private void handleBackspace() {
        if (!SettingsController.isBackspaceEnabled() || currentCharIndex <= 0) return;
        if (currentCharIndex < textFlow.getChildren().size()) {
            Text curr = (Text) textFlow.getChildren().get(currentCharIndex);
            curr.getStyleClass().clear();
            curr.getStyleClass().add("typing-char");
        }
        currentCharIndex--;
        Text prev = (Text) textFlow.getChildren().get(currentCharIndex);
        prev.getStyleClass().clear();
        prev.getStyleClass().add("typing-cursor");
    }

    private void handleOpponentProgress(String data) {
        String[] parts = data.split("\\|");
        if (parts.length < 2) return;
        liveProgress.put(parts[1], Integer.parseInt(parts[0]));
        refreshOpponentSummary();
    }

    private void handleOpponentResult(String data) {
        // format: wpm|accuracy|name|history
        String[] parts = data.split("\\|", 4);
        if (parts.length < 3) return;
        String name = parts[2];
        allFinalWpm.put(name, Double.parseDouble(parts[0]));
        allFinalAcc.put(name, Double.parseDouble(parts[1]));
        if (resultPane.isVisible()) buildPodium();
    }

    private void refreshOpponentSummary() {
        if (liveProgress.size() <= 1) {
            opponentSummaryLabel.setText("");
            return;
        }
        StringBuilder sb = new StringBuilder();
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(liveProgress.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());
        for (Map.Entry<String, Integer> e : sorted) {
            if (!e.getKey().equals(username))
                sb.append(e.getKey()).append(": ").append(e.getValue()).append("w  ");
        }
        opponentSummaryLabel.setText(sb.toString().trim());
    }

    private void showResult() {
        if (countdown != null) countdown.stop();
        if (liveWpmTimer != null) liveWpmTimer.stop();

        long clean = 0;
        for (int i = 0; i < currentCharIndex; i++)
            if (!hadError.containsKey(i)) clean++;

        double myAcc = currentCharIndex > 0 ? (clean * 100.0) / currentCharIndex : 100;
        double myWpm = wordsCompleted / (GAME_SECONDS / 60.0);

        allFinalWpm.put(username, myWpm);
        allFinalAcc.put(username, myAcc);
        allWpmHistory.put(username, new ArrayList<>(myWpmHistory));

        if (client != null) client.sendResult(myWpm, myAcc, myWpmHistory);
        if (server != null) server.broadcastHostResult(
                String.format("%.0f", myWpm),
                String.format("%.1f", myAcc),
                username,
                myWpmHistory);

        UserManager.saveResult(username, myWpm, myAcc, GAME_SECONDS);

        wpmResultLabel.setText(String.format("%.0f", myWpm));
        accResultLabel.setText(String.format("%.1f%%", myAcc));

        typingPane.setVisible(false);
        typingPane.setManaged(false);
        resultPane.setVisible(true);
        resultPane.setManaged(true);

        buildPodium();
    }

    private void buildPodium() {
        List<Map.Entry<String, Double>> ranked = new ArrayList<>(allFinalWpm.entrySet());
        ranked.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        int myRank = 1;
        for (int i = 0; i < ranked.size(); i++) {
            if (ranked.get(i).getKey().equals(username)) { myRank = i + 1; break; }
        }

        switch (myRank) {
            case 1 -> { resultHeadingLabel.setText("You Won!");
                resultSubLabel.setText("First place! Amazing typing!");
                resultHeadingLabel.setStyle("-fx-font-size:44px;-fx-font-weight:900;-fx-text-fill:#16A34A;"); }
            case 2 -> { resultHeadingLabel.setText("2nd Place");
                resultSubLabel.setText("So close! You almost had it.");
                resultHeadingLabel.setStyle("-fx-font-size:44px;-fx-font-weight:900;-fx-text-fill:#D97706;"); }
            case 3 -> { resultHeadingLabel.setText("3rd Place");
                resultSubLabel.setText("Good effort! Keep going.");
                resultHeadingLabel.setStyle("-fx-font-size:44px;-fx-font-weight:900;-fx-text-fill:#B45309;"); }
            default -> { resultHeadingLabel.setText("Better Luck Next Time!");
                resultSubLabel.setText("Practice makes perfect.");
                resultHeadingLabel.setStyle("-fx-font-size:34px;-fx-font-weight:900;-fx-text-fill:#DC2626;"); }
        }

        podiumBox.getChildren().clear();
        rankingBox.getChildren().clear();

        String[] medalColors = {"#F59E0B", "#94A3B8", "#B45309"};
        String[] podiumBg    = {"rgba(245,158,11,0.12)", "rgba(148,163,184,0.10)", "rgba(180,83,9,0.10)"};
        int[] heights        = {160, 120, 100};

        int total = ranked.size();

        if (total == 1) {
            buildPodiumSlot(ranked.get(0), 1, medalColors[0], podiumBg[0], heights[0]);
        } else if (total == 2) {
            buildPodiumSlot(ranked.get(1), 2, medalColors[1], podiumBg[1], heights[1]);
            buildPodiumSlot(ranked.get(0), 1, medalColors[0], podiumBg[0], heights[0]);
        } else {
            buildPodiumSlot(ranked.get(1), 2, medalColors[1], podiumBg[1], heights[1]);
            buildPodiumSlot(ranked.get(0), 1, medalColors[0], podiumBg[0], heights[0]);
            buildPodiumSlot(ranked.get(2), 3, medalColors[2], podiumBg[2], heights[2]);
        }

        for (int i = 3; i < ranked.size(); i++) {
            String name  = ranked.get(i).getKey();
            double wpm   = ranked.get(i).getValue();
            double acc   = allFinalAcc.getOrDefault(name, 0.0);
            boolean isMe = name.equals(username);

            HBox row = new HBox(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(isMe
                    ? "-fx-background-color:rgba(37,99,235,0.08);-fx-background-radius:8;-fx-border-color:#2563EB;-fx-border-width:1.5;-fx-border-radius:8;-fx-padding:10 16 10 16;"
                    : "-fx-background-color:white;-fx-background-radius:8;-fx-border-color:#E0DDD8;-fx-border-width:1;-fx-border-radius:8;-fx-padding:10 16 10 16;");

            Label pos = new Label((i + 1) + "th");
            pos.setStyle("-fx-font-size:13px;-fx-font-weight:700;-fx-text-fill:-fx-text-3;-fx-min-width:32;");
            Label nl = new Label(name + (isMe ? " ★" : ""));
            nl.setStyle("-fx-font-size:13px;-fx-font-weight:" + (isMe ? "700" : "400")
                    + ";-fx-text-fill:" + (isMe ? "#2563EB" : "-fx-text-1") + ";");
            HBox.setHgrow(nl, Priority.ALWAYS);
            Label wl = new Label(String.format("%.0f WPM", wpm));
            wl.setStyle("-fx-font-size:13px;-fx-font-weight:700;");
            Label al = new Label(String.format("%.1f%%", acc));
            al.setStyle("-fx-font-size:11px;-fx-text-fill:-fx-text-2;-fx-min-width:44;");

            row.getChildren().addAll(pos, nl, wl, al);
            rankingBox.getChildren().add(row);
        }

        buildWpmChart();
    }

    private void buildWpmChart() {
        wpmChart.getData().clear();
        if (myWpmHistory.isEmpty()) return;

        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName(username);

        for (int i = 0; i < myWpmHistory.size(); i++) {
            int sec = (i + 1) * 5;
            String label = (sec % 10 == 0) ? sec + "s" : "";
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(label, myWpmHistory.get(i)));
        }

        wpmChart.getData().add(series);

        javafx.application.Platform.runLater(() -> {
            for (javafx.scene.chart.XYChart.Data<String, Number> d : series.getData()) {
                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-background-color: #2563EB, white;"
                            + "-fx-background-radius: 4px; -fx-padding: 4px;");
                }
            }
        });
    }

    private void buildPodiumSlot(Map.Entry<String, Double> entry, int rank,
                                 String mc, String bg, int height) {
        String name  = entry.getKey();
        double wpm   = entry.getValue();
        double acc   = allFinalAcc.getOrDefault(name, 0.0);
        boolean isMe = name.equals(username);

        String rankLabel = rank == 1 ? "1st" : rank == 2 ? "2nd" : "3rd";

        Label rankLbl = new Label(rankLabel);
        rankLbl.setStyle("-fx-font-size:13px;-fx-font-weight:800;-fx-text-fill:" + mc + ";");

        Label nameLbl = new Label(name + (isMe ? " ★" : ""));
        nameLbl.setStyle("-fx-font-size:13px;-fx-font-weight:" + (isMe ? "800" : "600")
                + ";-fx-text-fill:" + (isMe ? "#2563EB" : "-fx-text-1") + ";");
        nameLbl.setWrapText(true);
        nameLbl.setMaxWidth(120);
        nameLbl.setAlignment(Pos.CENTER);

        Label wpmLbl = new Label(String.format("%.0f WPM", wpm));
        wpmLbl.setStyle("-fx-font-size:15px;-fx-font-weight:900;-fx-text-fill:" + mc + ";");

        Label accLbl = new Label(String.format("%.1f%%", acc));
        accLbl.setStyle("-fx-font-size:11px;-fx-text-fill:-fx-text-2;");

        VBox platform = new VBox(8);
        platform.setAlignment(Pos.CENTER);
        platform.setPrefHeight(height);
        platform.setPrefWidth(150);
        platform.setStyle("-fx-background-color:" + bg
                + ";-fx-background-radius:10 10 0 0;"
                + "-fx-border-color:" + mc + ";-fx-border-width:1.5 1.5 0 1.5;"
                + "-fx-border-radius:10 10 0 0;-fx-padding:14;");
        platform.getChildren().addAll(rankLbl, nameLbl, wpmLbl, accLbl);

        podiumBox.getChildren().add(platform);
    }

    private void goToDashboard() {
        if (client != null) client.disconnect();
        try {
            Stage stage = (Stage) rootVBox.getScene().getWindow();
            dashboardcontrol ctrl = SceneHelper.loadScene(stage, "dashboard-view.fxml", "KEYY");
            ctrl.setUsername(username);
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}