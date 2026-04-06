package com.keyy.app;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.*;

public class TypingController {

    public enum Difficulty { EASY, NORMAL, HARD }

    private static String[] bankFor(Difficulty d) {
        return switch (d) {
            case EASY -> WordBank.EASY;
            case HARD -> WordBank.HARD;
            default   -> WordBank.NORMAL;
        };
    }


    @FXML private VBox rootVBox;
    @FXML private VBox typingPane;
    @FXML private VBox resultPane;
    @FXML private TextFlow textFlow;
    @FXML private Label timerLabel;
    @FXML private Label difficultyLabel;
    @FXML private Label wpmLiveLabel;
    @FXML private Label wpmResultLabel;
    @FXML private Label accResultLabel;
    @FXML private Label timeResultLabel;
    @FXML private Label wordsResultLabel;
    @FXML private Button retryBtn;
    @FXML private Button dashboardBtn;
    @FXML private Button closeBtn;


    private String username;
    private Difficulty difficulty = Difficulty.NORMAL;
    private int totalSeconds = 60;
    private int timeLeft;

    private final List<String> wordQueue         = new ArrayList<>();
    private int currentCharIndex                 = 0;
    private int wordsCompleted                   = 0;
    private final Map<Integer, Boolean> hadError = new HashMap<>();
    private final List<Boolean> typedHistory     = new ArrayList<>();

    private Timeline countdown;
    private Timeline liveWpmTimer;
    private final Random rand = new Random();


    @FXML
    public void initialize() {
        resultPane.setVisible(false);
        resultPane.setManaged(false);
        retryBtn.setOnAction(e -> restart());
        dashboardBtn.setOnAction(e -> goToDashboard());
        closeBtn.setOnAction(e -> ((Stage) rootVBox.getScene().getWindow()).close());
    }


    public void setup(String username, Difficulty difficulty, int seconds) {
        this.username     = username;
        this.difficulty   = difficulty;
        this.totalSeconds = seconds;
        this.timeLeft     = seconds;

        difficultyLabel.setText(difficulty.name());
        timerLabel.setText(String.valueOf(timeLeft));

        buildWordQueue(50);
        renderTextFlow();
        startCountdown();
        startLiveWpm();


        javafx.application.Platform.runLater(() -> {
            if (rootVBox.getScene() != null)
                rootVBox.getScene().setOnKeyTyped(this::handleKeyPress);
        });
    }


    private void buildWordQueue(int count) {
        wordQueue.clear();
        String[] bank = bankFor(difficulty);
        for (int i = 0; i < count; i++)
            wordQueue.add(bank[rand.nextInt(bank.length)]);
    }

    private void appendWords(int count) {
        String[] bank = bankFor(difficulty);
        for (int i = 0; i < count; i++)
            wordQueue.add(bank[rand.nextInt(bank.length)]);
    }


    private void renderTextFlow() {
        textFlow.getChildren().clear();
        hadError.clear();
        typedHistory.clear();
        currentCharIndex = 0;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wordQueue.size(); i++) {
            sb.append(wordQueue.get(i));
            if (i < wordQueue.size() - 1) sb.append(" ");
        }

        for (int i = 0; i < sb.length(); i++) {
            Text t = new Text(String.valueOf(sb.charAt(i)));
            t.getStyleClass().add(i == 0 ? "typing-cursor" : "typing-char");
            textFlow.getChildren().add(t);
        }
    }

    private String fullText() {
        StringBuilder sb = new StringBuilder();
        textFlow.getChildren().forEach(n -> sb.append(((Text) n).getText()));
        return sb.toString();
    }


    private void startCountdown() {
        if (countdown != null) countdown.stop();
        countdown = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--;
            timerLabel.setText(String.valueOf(timeLeft));
            if (timeLeft <= 10)
                timerLabel.setStyle(
                        "-fx-font-size:52px;-fx-font-weight:900;" +
                                "-fx-text-fill:#FF5E57;"
                );
            if (timeLeft <= 0) showResult();
        }));
        countdown.setCycleCount(Timeline.INDEFINITE);
        countdown.play();
    }

    private void startLiveWpm() {
        if (liveWpmTimer != null) liveWpmTimer.stop();
        liveWpmTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            int elapsed = totalSeconds - timeLeft;
            if (elapsed > 0)
                wpmLiveLabel.setText(
                        String.format("%.0f WPM", wordsCompleted / (elapsed / 60.0))
                );
        }));
        liveWpmTimer.setCycleCount(Timeline.INDEFINITE);
        liveWpmTimer.play();
    }


    private void handleKeyPress(KeyEvent event) {
        if (timeLeft <= 0) return;
        String typed = event.getCharacter();

        if (typed.equals("\b")) {
            if (SettingsController.isBackspaceEnabled()) handleBackspace();
            return;
        }
        if (typed.isEmpty() || typed.charAt(0) < 32) return;

        String full = fullText();
        if (currentCharIndex >= full.length()) return;

        boolean correct = typed.charAt(0) == full.charAt(currentCharIndex);
        if (!correct && !hadError.containsKey(currentCharIndex))
            hadError.put(currentCharIndex, true);

        typedHistory.add(correct);

        Text t = (Text) textFlow.getChildren().get(currentCharIndex);
        t.getStyleClass().clear();
        t.getStyleClass().add(correct
                ? (hadError.containsKey(currentCharIndex) ? "typing-corrected" : "typing-correct")
                : "typing-wrong");

        currentCharIndex++;

        // Word completed when a space is typed correctly
        if (correct && currentCharIndex > 0&& full.charAt(currentCharIndex - 1) == ' ') {
            wordsCompleted++;
            if (full.length() - currentCharIndex < 150)
                appendWordsToFlow(20);
        }

        // Move cursor to next char
        if (currentCharIndex < textFlow.getChildren().size()) {
            Text next = (Text) textFlow.getChildren().get(currentCharIndex);
            next.getStyleClass().clear();
            next.getStyleClass().add("typing-cursor");
        }
    }

    private void handleBackspace() {
        if (currentCharIndex <= 0) return;

        if (currentCharIndex < textFlow.getChildren().size()) {
            Text curr = (Text) textFlow.getChildren().get(currentCharIndex);
            curr.getStyleClass().clear();
            curr.getStyleClass().add("typing-char");
        }
        currentCharIndex--;
        if (!typedHistory.isEmpty()) typedHistory.remove(typedHistory.size() - 1);

        Text prev = (Text) textFlow.getChildren().get(currentCharIndex);
        prev.getStyleClass().clear();
        prev.getStyleClass().add("typing-cursor");

    }

    private void appendWordsToFlow(int count) {
        appendWords(count);
        int start = wordQueue.size() - count;
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < wordQueue.size(); i++)
            sb.append(" ").append(wordQueue.get(i));

        for (char c : sb.toString().toCharArray()) {
            Text t = new Text(String.valueOf(c));
            t.getStyleClass().add("typing-char");
            textFlow.getChildren().add(t);
        }
    }


    private void showResult() {
        if (countdown != null) countdown.stop();
        if (liveWpmTimer != null) liveWpmTimer.stop();

        long clean = 0;
        for (int i = 0; i < currentCharIndex; i++)
            if (!hadError.containsKey(i)) clean++;

        double accuracy = currentCharIndex > 0
                ? (clean * 100.0) / currentCharIndex : 100;
        double wpm = wordsCompleted / (totalSeconds / 60.0);

        if (username != null) UserManager.saveResult(username, wpm, accuracy, totalSeconds);

        wpmResultLabel.setText(String.format("%.0f", wpm));
        accResultLabel.setText(String.format("%.1f%%", accuracy));
        timeResultLabel.setText(totalSeconds + "s");
        wordsResultLabel.setText(String.valueOf(wordsCompleted));

        typingPane.setVisible(false);
        typingPane.setManaged(false);
        resultPane.setVisible(true);
        resultPane.setManaged(true);
    }

    private void restart() {
        wordQueue.clear();
        hadError.clear();
        typedHistory.clear();
        wordsCompleted   = 0;
        currentCharIndex = 0;
        timeLeft         = totalSeconds;
        timerLabel.setStyle("");

        typingPane.setVisible(true);
        typingPane.setManaged(true);
        resultPane.setVisible(false);
        resultPane.setManaged(false);

        buildWordQueue(50);
        renderTextFlow();
        startCountdown();
        startLiveWpm();

        javafx.application.Platform.runLater(() -> {
            if (rootVBox.getScene() != null)
                rootVBox.getScene().setOnKeyTyped(this::handleKeyPress);
        });
    }

    private void goToDashboard() {
        try {
            if (countdown != null) countdown.stop();
            if (liveWpmTimer != null) liveWpmTimer.stop();
            Stage stage = (Stage) rootVBox.getScene().getWindow();
            dashboardcontrol ctrl = SceneHelper.loadScene(
                    stage, "dashboard-view.fxml", "KEYY");
            ctrl.setUsername(username);
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}