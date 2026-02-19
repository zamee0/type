package com.keyy.app;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

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
    private int correctChars = 0;
    private int totalChars = 0;
    private int timeInSeconds = 0;
    private Timeline timeline;
    private String username;

    @FXML
    public void initialize() {
        loadSentence();

        // Show typing pane, hide result pane
        resultPane.setVisible(false);
        resultPane.setManaged(false);

        // Key listener
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

    // Tracks whether each typed character was correct (true) or wrong (false)
    private java.util.ArrayList<Boolean> typedHistory = new java.util.ArrayList<>();

    private void handleKeyPress(KeyEvent event) {
        String typed = event.getCharacter();

        // Backspace
        if (typed.equals("\b")) {
            if (currentIndex > 0) {
                // Remove cursor from current char (if not at end)
                if (currentIndex < currentSentence.length()) {
                    Text curr = (Text) textFlow.getChildren().get(currentIndex);
                    curr.setStyle("-fx-font-size: 22px;");
                }
                currentIndex--;

                // Undo the last typed char's effect on counts
                boolean wasCorrect = typedHistory.remove(typedHistory.size() - 1);
                totalChars--;
                if (wasCorrect) correctChars--;

                // Reset previous char to neutral with cursor
                Text prev = (Text) textFlow.getChildren().get(currentIndex);
                prev.setStyle("-fx-font-size: 22px; -fx-underline: true;");
            }
            return;
        }

        // Ignore if sentence already done
        if (currentIndex >= currentSentence.length()) return;

        Text t = (Text) textFlow.getChildren().get(currentIndex);
        boolean isCorrect = typed.charAt(0) == currentSentence.charAt(currentIndex);

        totalChars++;
        typedHistory.add(isCorrect);

        if (isCorrect) {
            t.setStyle("-fx-font-size: 22px; -fx-fill: green;");
            correctChars++;
        } else {
            t.setStyle("-fx-font-size: 22px; -fx-fill: red;");
        }

        currentIndex++;

        // Move cursor underline to next char
        if (currentIndex < currentSentence.length()) {
            Text next = (Text) textFlow.getChildren().get(currentIndex);
            next.setStyle(next.getStyle() + " -fx-underline: true;");
        } else {
            showResult();
        }
    }

    private void showResult() {
        if (timeline != null) timeline.stop();

        double minutes = timeInSeconds / 60.0;
        double wpm = minutes > 0 ? (correctChars / 5.0) / minutes : 0;
        double accuracy = totalChars > 0 ? (correctChars * 100.0) / totalChars : 100;

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
        correctChars = 0;
        totalChars = 0;
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