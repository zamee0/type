package com.keyy.app;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class TypingController {
    @FXML private VBox rootVBox;
    @FXML private TextFlow textFlow;
    @FXML private Label timerLabel;
    
    private String[] sentences = {
            "the quick brown fox jumps over the lazy dog",
            "practice makes perfect in everything you do",
            "typing fast requires patience and dedication",
            "always strive to improve your skills daily",
            "good communication is key to success"
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
        rootVBox.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyTyped(this::handleKeyPress);
            }
        });
    }
    
    public void setUsername(String username) {
        this.username = username;
        startTimer();
    }
    
    private void loadSentence() {
        Random rand = new Random();
        currentSentence = sentences[rand.nextInt(sentences.length)];
        
        textFlow.getChildren().clear();
        for (char c : currentSentence.toCharArray()) {
            Text t = new Text(String.valueOf(c));
            t.setStyle("-fx-font-size: 24px; -fx-fill: #333;");
            textFlow.getChildren().add(t);
        }
        
        if (!textFlow.getChildren().isEmpty()) {
            Text first = (Text) textFlow.getChildren().get(0);
            first.setStyle("-fx-font-size: 24px; -fx-fill: #333; -fx-underline: true;");
        }
    }
    
    private void startTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeInSeconds++;
            timerLabel.setText(String.valueOf(timeInSeconds));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    private void handleKeyPress(KeyEvent event) {
        String typed = event.getCharacter();
        
        if (typed.equals("\b")) { // Backspace
            if (currentIndex > 0) {
                currentIndex--;
                Text t = (Text) textFlow.getChildren().get(currentIndex);
                t.setStyle("-fx-font-size: 24px; -fx-fill: #333; -fx-underline: true;");
            }
            return;
        }
        
        if (currentIndex >= currentSentence.length()) return;
        
        totalChars++;
        Text t = (Text) textFlow.getChildren().get(currentIndex);
        
        if (typed.charAt(0) == currentSentence.charAt(currentIndex)) {
            t.setStyle("-fx-font-size: 24px; -fx-fill: #999;");
            correctChars++;
        } else {
            t.setStyle("-fx-font-size: 24px; -fx-fill: red;");
        }
        
        currentIndex++;
        
        if (currentIndex < currentSentence.length()) {
            Text next = (Text) textFlow.getChildren().get(currentIndex);
            next.setStyle("-fx-font-size: 24px; -fx-fill: #333; -fx-underline: true;");
        } else {
            showResult();
        }
    }

    private void showResult() {
        if (timeline != null) timeline.stop();

        double minutes = timeInSeconds / 60.0;
        double wpm = minutes > 0 ? (correctChars / 5.0) / minutes : 0;
        double accuracy = totalChars > 0 ? (correctChars * 100.0) / totalChars : 100;

        rootVBox.getChildren().clear();

        VBox resultBox = new VBox(20);
        resultBox.setAlignment(Pos.CENTER);

        Label wpmLabel = new Label(String.format("%.0f WPM", wpm));
        wpmLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold;");

        Label accLabel = new Label(String.format("Accuracy: %.1f%%", accuracy));
        accLabel.setStyle("-fx-font-size: 24px;");

        Label timeLabel = new Label("Time: " + timeInSeconds + " seconds");
        timeLabel.setStyle("-fx-font-size: 18px;");

        Button retryBtn = new Button("Try Again");
        retryBtn.setOnAction(e -> {
            currentIndex = 0;
            correctChars = 0;
            totalChars = 0;
            timeInSeconds = 0;

            rootVBox.getChildren().clear();
            rootVBox.getChildren().addAll(timerLabel, textFlow);
            loadSentence();
            startTimer();
        });

        Button backBtn = new Button("Back to Dashboard");
        backBtn.setOnAction(e -> backToDashboard());
        resultBox.getChildren().addAll(
                wpmLabel,
                accLabel,
                timeLabel,
                retryBtn,
                backBtn
        );

        rootVBox.getChildren().add(resultBox);
    }

    private void backToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard-view.fxml"));
            Scene scene = new Scene(loader.load(), 600, 500);

            dashboardcontrol controller = loader.getController();
            controller.setUsername(username);

            Stage stage = (Stage) rootVBox.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Scene scene = new Scene(loader.load(), 500, 400);
            
            Stage stage = (Stage) rootVBox.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Typing Speed Test");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
