package com.keyy.app;

import javafx.animation.*;
import javafx.fxml.FXML;
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
    @FXML private VBox resultPane;
    @FXML private TextFlow textFlow;
    @FXML private Label timerLabel;
    @FXML private Label wpmLiveLabel;
    @FXML private Label statusLabel;
    @FXML private VBox opponentBox;
    @FXML private Label resultHeadingLabel;
    @FXML private Label resultSubLabel;
    @FXML private Label wpmResultLabel;
    @FXML private Label accResultLabel;
    @FXML private VBox opponentResultBox;
    @FXML private Button dashboardBtn;

    private String username;
    private GameClient client;
    private boolean isHost;

    private int currentCharIndex = 0;
    private int wordsCompleted = 0;
    private final Map<Integer, Boolean> hadError = new HashMap<>();
    private int GAME_SECONDS = 60;
    private int timeLeft = GAME_SECONDS;

    private Timeline countdown;
    private Timeline liveWpmTimer;
    private final Random rand = new Random();

    private String[] wordBank;
    private final List<String> allWords = new ArrayList<>();

    private final Map<String, Integer> opponentProgress = new HashMap<>();
    private final Map<String, Double> opponentFinalWpm = new HashMap<>();

    @FXML
    public void initialize() {
        resultPane.setVisible(false);
        resultPane.setManaged(false);
        dashboardBtn.setOnAction(e -> goToDashboard());
    }

    public void setup(String username, String words, int seconds, GameClient client, boolean isHost) {
        this.username     = username;
        this.client       = client;
        this.isHost       = isHost;
        this.GAME_SECONDS = seconds;
        this.timeLeft     = seconds;
        timerLabel.setText(String.valueOf(seconds));

        wordBank = words.split(" ");

        if (client != null) {
            client.setOnProgressUpdate(this::handleOpponentProgress);
            client.setOnResult(this::handleOpponentResult);
            client.setOnDisconnect(() -> statusLabel.setText("Opponent disconnected."));
        }

        buildTextFlow();
        startCountdown();
        startLiveWpm();

        javafx.application.Platform.runLater(() -> {
            if (rootVBox.getScene() != null)
                rootVBox.getScene().setOnKeyTyped(this::handleKeyPress);
        });
    }

    private void buildTextFlow() {
        textFlow.getChildren().clear();
        allWords.clear();
        currentCharIndex = 0;
        appendWords(40);
        highlightCursor();
    }

    private void appendWords(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (!allWords.isEmpty() || sb.length() > 0) sb.append(" ");
            String word = wordBank[rand.nextInt(wordBank.length)];
            allWords.add(word);
            sb.append(word);
        }
        String added = allWords.isEmpty() ? sb.toString() : sb.toString();
        for (char c : added.toCharArray()) {
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
            if (elapsed > 0)
                wpmLiveLabel.setText(String.format("%.0f WPM", wordsCompleted / (elapsed / 60.0)));
        }));
        liveWpmTimer.setCycleCount(Timeline.INDEFINITE);
        liveWpmTimer.play();
    }

    private void handleKeyPress(KeyEvent event) {
        if (timeLeft <= 0) return;
        String typed = event.getCharacter();
        if (typed.equals("\b")) { handleBackspace(); return; }
        if (typed.isEmpty() || typed.charAt(0) < 32) return;

        String full = fullText();

        if (currentCharIndex >= full.length() - 20) {
            appendWords(20);
        }

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
            if (client != null) client.sendProgress(wordsCompleted);
        }

        highlightCursor();
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
        int words = Integer.parseInt(parts[0]);
        String name = parts[1];
        opponentProgress.put(name, words);
        updateOpponentBox();
    }

    private void handleOpponentResult(String data) {
        String[] parts = data.split("\\|");
        if (parts.length < 3) return;
        double wpm = Double.parseDouble(parts[0]);
        String acc = parts[1];
        String name = parts[2];
        opponentFinalWpm.put(name, wpm);

        VBox row = new VBox(4);
        row.setStyle("-fx-background-color: transparent;");
        Label nameLbl = new Label(name);
        nameLbl.getStyleClass().add("history-rank");
        Label resultLbl = new Label(String.format("%.0f WPM  •  %s%% Accuracy", wpm, acc));
        resultLbl.getStyleClass().add("history-wpm");
        row.getChildren().addAll(nameLbl, resultLbl);
        opponentResultBox.getChildren().add(row);
    }

    private void updateOpponentBox() {
        opponentBox.getChildren().clear();
        for (Map.Entry<String, Integer> entry : opponentProgress.entrySet()) {
            Label lbl = new Label(entry.getKey() + ": " + entry.getValue() + " words");
            lbl.getStyleClass().add("history-detail");
            opponentBox.getChildren().add(lbl);
        }
    }

    private void showResult() {
        if (countdown != null) countdown.stop();
        if (liveWpmTimer != null) liveWpmTimer.stop();

        long clean = 0;
        for (int i = 0; i < currentCharIndex; i++)
            if (!hadError.containsKey(i)) clean++;

        double accuracy = currentCharIndex > 0 ? (clean * 100.0) / currentCharIndex : 100;
        double myWpm    = wordsCompleted / (GAME_SECONDS / 60.0);

        if (client != null) client.sendResult(myWpm, accuracy);
        UserManager.saveResult(username, myWpm, accuracy, GAME_SECONDS);

        wpmResultLabel.setText(String.format("%.0f WPM", myWpm));
        accResultLabel.setText(String.format("%.1f%% Accuracy", accuracy));

        determineOutcome(myWpm);

        typingPane.setVisible(false);
        typingPane.setManaged(false);
        resultPane.setVisible(true);
        resultPane.setManaged(true);
    }

    private void determineOutcome(double myWpm) {
        if (opponentFinalWpm.isEmpty()) {
            resultHeadingLabel.setText("Time's Up!");
            resultSubLabel.setText("Waiting for opponents to finish...");
            resultHeadingLabel.setStyle("-fx-text-fill: -fx-text-1;");
            return;
        }

        long higherCount = opponentFinalWpm.values().stream()
                .filter(w -> w > myWpm).count();

        if (higherCount == 0) {
            resultHeadingLabel.setText("You Won!");
            resultSubLabel.setText("First place! Great typing!");
            resultHeadingLabel.setStyle("-fx-font-size:42px; -fx-font-weight:900; -fx-text-fill:#16A34A;");
        } else if (higherCount == 1 && opponentFinalWpm.size() >= 2) {
            resultHeadingLabel.setText("2nd Place");
            resultSubLabel.setText("So close! Keep it up.");
            resultHeadingLabel.setStyle("-fx-font-size:42px; -fx-font-weight:900; -fx-text-fill:#D97706;");
        } else {
            resultHeadingLabel.setText("Better Luck Next Time");
            resultSubLabel.setText("Practice makes perfect!");
            resultHeadingLabel.setStyle("-fx-font-size:32px; -fx-font-weight:900; -fx-text-fill:#DC2626;");
        }
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