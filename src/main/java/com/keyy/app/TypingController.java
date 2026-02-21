package com.keyy.app;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;

public class TypingController {

    @FXML private VBox rootVBox;
    @FXML private VBox typingPane;
    @FXML private VBox resultPane;
    @FXML private TextFlow textFlow;
    @FXML private Label timerLabel;
    @FXML private Label wpmResultLabel;
    @FXML private Label accResultLabel;
    @FXML private Label timeResultLabel;
    @FXML private Button retryBtn;
    @FXML private Button dashboardBtn;
    @FXML private Button closeBtn;

    private static final String[] SENTENCES = {
            "the quick brown fox jumps over the lazy dog",
            "practice makes perfect in everything you do",
            "typing fast requires patience and dedication",
            "always strive to improve your skills daily",
            "good communication is the key to success",
            "every moment is a fresh beginning for you",
            "success is the sum of small efforts repeated daily",
            "the only way to do great work is to love it",
            "stay focused and never give up on your dreams",
            "hard work beats talent when talent does not work hard"
    };

    private String currentSentence;
    private int currentIndex = 0;

    // hadError[i] = true if user ever typed wrong at position i
    // This is PERMANENT — backspace does not clear it
    private boolean[] hadError;

    // For visual undo on backspace — tracks what was typed at each position
    private ArrayList<Boolean> typedHistory = new ArrayList<>();

    private int timeInSeconds = 0;
    private Timeline timeline;
    private String username;

    @FXML
    public void initialize() {
        loadSentence();

        resultPane.setVisible(false);
        resultPane.setManaged(false);

        rootVBox.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyTyped(this::handleKeyPress);
            }
        });

        retryBtn.setOnAction(e -> resetGame());
        dashboardBtn.setOnAction(e -> goToDashboard());
        closeBtn.setOnAction(e -> ((Stage) rootVBox.getScene().getWindow()).close());
    }

    public void setUsername(String username) {
        this.username = username;
        startTimer();
    }

    private void loadSentence() {
        java.util.Random rand = new java.util.Random();
        currentSentence = SENTENCES[rand.nextInt(SENTENCES.length)];
        currentIndex = 0;
        hadError = new boolean[currentSentence.length()];
        typedHistory.clear();

        textFlow.getChildren().clear();
        for (int i = 0; i < currentSentence.length(); i++) {
            Text t = new Text(String.valueOf(currentSentence.charAt(i)));
            t.setStyle("-fx-font-size: 22px;");
            if (i == 0) t.setStyle("-fx-font-size: 22px; -fx-underline: true;");
            textFlow.getChildren().add(t);
        }
    }

    private void startTimer() {
        if (timeline != null) timeline.stop();
        timeInSeconds = 0;
        timerLabel.setText("0");
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeInSeconds++;
            timerLabel.setText(String.valueOf(timeInSeconds));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void handleKeyPress(KeyEvent event) {
        String typed = event.getCharacter();

        // --- BACKSPACE ---
        if (typed.equals("\b")) {
            if (!SettingsController.isBackspaceEnabled()) return; // disabled in settings

            if (currentIndex > 0) {
                // Remove cursor from current position
                if (currentIndex < currentSentence.length()) {
                    Text curr = (Text) textFlow.getChildren().get(currentIndex);
                    curr.setStyle("-fx-font-size: 22px;");
                }
                currentIndex--;
                typedHistory.remove(typedHistory.size() - 1);

                // Reset char visually to neutral + cursor
                // hadError[currentIndex] is NOT reset — mistake is permanent
                Text prev = (Text) textFlow.getChildren().get(currentIndex);
                prev.setStyle("-fx-font-size: 22px; -fx-underline: true;");
            }
            return;
        }

        if (currentIndex >= currentSentence.length()) return;

        boolean isCorrect = typed.charAt(0) == currentSentence.charAt(currentIndex);

        // If wrong at this position for the first time, mark it permanently
        if (!isCorrect && !hadError[currentIndex]) {
            hadError[currentIndex] = true;
        }

        typedHistory.add(isCorrect);

        Text t = (Text) textFlow.getChildren().get(currentIndex);
        if (isCorrect) {
            if (!hadError[currentIndex]) {
                // Clean correct — green
                t.setStyle("-fx-font-size: 22px; -fx-fill: green;");
            } else {
                // Corrected after a mistake — orange (shows it cost accuracy)
                t.setStyle("-fx-font-size: 22px; -fx-fill: orange;");
            }
        } else {
            t.setStyle("-fx-font-size: 22px; -fx-fill: red;");
        }

        currentIndex++;

        if (currentIndex < currentSentence.length()) {
            Text next = (Text) textFlow.getChildren().get(currentIndex);
            next.setStyle(next.getStyle() + " -fx-underline: true;");
        } else {
            showResult();
        }
    }

    private void showResult() {
        if (timeline != null) timeline.stop();

        // Count positions with zero errors = perfectly typed chars
        int cleanChars = 0;
        for (boolean err : hadError) {
            if (!err) cleanChars++;
        }

        // Accuracy = clean positions / total positions
        double accuracy = (currentSentence.length() > 0)
                ? (cleanChars * 100.0) / currentSentence.length()
                : 100;

        // WPM = based on sentence length (standard: chars/5 = words)
        double minutes = timeInSeconds / 60.0;
        double wpm = minutes > 0 ? (currentSentence.length() / 5.0) / minutes : 0;

        if (username != null) {
            UserManager.saveResult(username, wpm, accuracy, timeInSeconds);
        }

        wpmResultLabel.setText(String.format("%.0f WPM", wpm));
        accResultLabel.setText(String.format("%.1f%% Accuracy", accuracy));
        timeResultLabel.setText("Time: " + timeInSeconds + "s");

        typingPane.setVisible(false);
        typingPane.setManaged(false);
        resultPane.setVisible(true);
        resultPane.setManaged(true);
    }

    private void resetGame() {
        currentIndex = 0;
        typedHistory.clear();

        typingPane.setVisible(true);
        typingPane.setManaged(true);
        resultPane.setVisible(false);
        resultPane.setManaged(false);

        loadSentence();
        startTimer();
    }

    private void goToDashboard() {
        try {
            Stage stage = (Stage) rootVBox.getScene().getWindow();
            dashboardcontrol ctrl = SceneHelper.loadScene(stage, "dashboard-view.fxml", "KEYY - Dashboard");
            ctrl.setUsername(username);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}