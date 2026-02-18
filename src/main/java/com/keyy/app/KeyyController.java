package com.keyy.app;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KeyyController {

    @FXML
    private VBox rootVBox;
    @FXML
    private TextFlow textFlow;
    @FXML
    private Label timerLabel;
    @FXML
    private Button darkModeBtn;
    @FXML
    private VBox typingContainer;

    private String[] sentences = {
            "late out house consider order hold or off year new great keep each those present no right for up also late course think thing system in govern move ask face both you other show against of man",
            "the quick brown fox jumps over the lazy dog and runs through the dense forest with incredible speed",
            "programming requires patience practice and dedication to master the art of writing clean efficient code",
            "artificial intelligence is transforming the way we live work and interact with technology every single day",
            "type faster and more accurately to improve your productivity and efficiency in everyday tasks online",
            "good communication skills are essential for success in both personal and professional relationships worldwide"
    };

    private String currentSentence;
    private int currentIndex = 0;
    private int correctChars = 0;
    private int totalKeyPresses = 0;
    private int mistakeCount = 0;
    private int backspaceCount = 0;

    private Timeline timeline;
    private int timeInSeconds = 0;
    private boolean timerStarted = false;
    private boolean isDarkMode = false;

    private javafx.event.EventHandler<KeyEvent> keyEventHandler;
    private Random random = new Random();

    private String currentUsername = null;

    // For WPM tracking over time
    private List<Double> wpmHistory = new ArrayList<>();
    private List<Integer> timeHistory = new ArrayList<>();

    // World average WPM
    private final double WORLD_AVG_WPM = 41.0;

    // Color schemes
    private final String LIGHT_BG = "#f5f7fa";
    private final String LIGHT_TEXT = "#646669";
    private final String LIGHT_UNTYPED = "#4a4a4a";
    private final String LIGHT_TYPED_CORRECT = "#d4d4d8";

    private final String DARK_BG = "#323437";
    private final String DARK_TEXT = "#646669";
    private final String DARK_UNTYPED = "#d1d0c5";
    private final String DARK_TYPED_CORRECT = "#646669";

    @FXML
    public void initialize() {
        keyEventHandler = this::handleKey;

        loadNextSentence();

        // Dark mode button setup
        darkModeBtn.setText("🌙");
        darkModeBtn.setFocusTraversable(false);
        darkModeBtn.setMnemonicParsing(false);
        darkModeBtn.setStyle("-fx-font-size: 18px; -fx-cursor: hand; -fx-background-color: transparent; -fx-border-color: transparent;");
        darkModeBtn.setOnAction(e -> toggleDarkMode());

        // Scene ready → key listener attach
        textFlow.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.removeEventFilter(KeyEvent.KEY_TYPED, keyEventHandler);
            }
            if (newScene != null) {
                newScene.removeEventFilter(KeyEvent.KEY_TYPED, keyEventHandler);
                newScene.addEventFilter(KeyEvent.KEY_TYPED, keyEventHandler);
            }
        });
    }

    public void setCurrentUser(String username) {
        this.currentUsername = username;
    }

    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;

        if (isDarkMode) {
            darkModeBtn.setText("☀️");
            rootVBox.setStyle("-fx-background-color: " + DARK_BG + "; -fx-padding: 40;");
            timerLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #e2b714;");
        } else {
            darkModeBtn.setText("🌙");
            rootVBox.setStyle("-fx-background-color: " + LIGHT_BG + "; -fx-padding: 40;");
            timerLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #e2b714;");
        }

        updateTextColors();
    }

    private void updateTextColors() {
        String untypedColor = isDarkMode ? DARK_UNTYPED : LIGHT_UNTYPED;
        String typedCorrectColor = isDarkMode ? DARK_TYPED_CORRECT : LIGHT_TYPED_CORRECT;

        for (int i = 0; i < textFlow.getChildren().size(); i++) {
            Text txt = (Text) textFlow.getChildren().get(i);
            String style = txt.getStyle();

            if (style.contains("#f87171") || style.contains("red")) {
                // Wrong - keep red
                txt.setStyle("-fx-fill: #f87171; -fx-font-size: 24px;" + (i == currentIndex ? " -fx-underline: true;" : ""));
            } else if (i < currentIndex) {
                // Typed correctly - gray/dim color
                txt.setStyle("-fx-fill: " + typedCorrectColor + "; -fx-font-size: 24px;");
            } else if (i == currentIndex) {
                // Current position - highlighted
                txt.setStyle("-fx-fill: " + untypedColor + "; -fx-font-size: 24px; -fx-underline: true;");
            } else {
                // Not typed yet
                txt.setStyle("-fx-fill: " + untypedColor + "; -fx-font-size: 24px;");
            }
        }
    }

    private void loadNextSentence() {
        if (sentences.length == 0) return;

        currentSentence = sentences[random.nextInt(sentences.length)];
        textFlow.getChildren().clear();

        String untypedColor = isDarkMode ? DARK_UNTYPED : LIGHT_UNTYPED;

        for (char c : currentSentence.toCharArray()) {
            Text t = new Text(String.valueOf(c));
            t.setStyle("-fx-fill: " + untypedColor + "; -fx-font-size: 24px;");
            textFlow.getChildren().add(t);
        }

        currentIndex = 0;
        correctChars = 0;
        totalKeyPresses = 0;
        mistakeCount = 0;
        backspaceCount = 0;
        wpmHistory.clear();
        timeHistory.clear();

        timerInSecondsReset();
        updateCursor();
    }

    private void startTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeInSeconds++;
            timerLabel.setText(String.valueOf(timeInSeconds));

            // Track WPM every second
            double currentWPM = calculateCurrentWPM();
            wpmHistory.add(currentWPM);
            timeHistory.add(timeInSeconds);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        timerStarted = true;
    }

    private void timerInSecondsReset() {
        if (timeline != null) timeline.stop();
        timeInSeconds = 0;
        timerLabel.setText("0");
        timerStarted = false;
    }

    private double calculateCurrentWPM() {
        double minutes = timeInSeconds / 60.0;
        return minutes > 0 ? (correctChars / 5.0) / minutes : 0;
    }

    private void handleKey(KeyEvent event) {
        event.consume();

        if (!timerStarted) startTimer();

        String character = event.getCharacter();

        if (character.equals("\b")) { // Backspace
            if (currentIndex > 0) {
                currentIndex--;
                backspaceCount++;
                Text t = (Text) textFlow.getChildren().get(currentIndex);

                String untypedColor = isDarkMode ? DARK_UNTYPED : LIGHT_UNTYPED;
                t.setStyle("-fx-fill: " + untypedColor + "; -fx-font-size: 24px;");

                updateCursor();
            }
            return;
        }

        if (currentIndex >= currentSentence.length()) return;

        totalKeyPresses++;
        Text t = (Text) textFlow.getChildren().get(currentIndex);

        if (character.charAt(0) == currentSentence.charAt(currentIndex)) {
            String typedCorrectColor = isDarkMode ? DARK_TYPED_CORRECT : LIGHT_TYPED_CORRECT;
            t.setStyle("-fx-fill: " + typedCorrectColor + "; -fx-font-size: 24px;");
            correctChars++;
        } else {
            t.setStyle("-fx-fill: #f87171; -fx-font-size: 24px;");
            mistakeCount++;
        }

        currentIndex++;
        updateCursor();

    }

    private void updateCursor() {
        updateTextColors();
    }


    private double calculatePercentile(double wpm) {
        // Based on typical WPM distribution
        // Average: 41 WPM, Median: ~40 WPM
        // Using approximate normal distribution

        if (wpm >= 120) return 99.9;
        if (wpm >= 100) return 99.0;
        if (wpm >= 90) return 97.0;
        if (wpm >= 80) return 95.0;
        if (wpm >= 70) return 90.0;
        if (wpm >= 60) return 80.0;
        if (wpm >= 50) return 65.0;
        if (wpm >= 41) return 50.0;
        if (wpm >= 35) return 35.0;
        if (wpm >= 30) return 25.0;
        if (wpm >= 25) return 15.0;
        if (wpm >= 20) return 10.0;
        return 5.0;
    }

    private void showDominanceScreen(double wpm, double accuracy, double percentile) {
        rootVBox.getChildren().clear();

        VBox dominanceBox = new VBox(30);
        dominanceBox.setAlignment(javafx.geometry.Pos.CENTER);
        dominanceBox.setStyle("-fx-padding: 40;");

        Label titleLabel = new Label("📊 Your Dominance");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #e2b714;");

        // WPM Progression Graph
        Label wpmGraphLabel = new Label("WPM Over Time");
        wpmGraphLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: " + (isDarkMode ? DARK_UNTYPED : LIGHT_UNTYPED) + "; -fx-font-weight: bold;");

        LineChart<Number, Number> wpmChart = createWPMChart();
        wpmChart.setStyle("-fx-background-color: transparent;");

        // Dominance Stats
        VBox statsContainer = new VBox(20);
        statsContainer.setAlignment(javafx.geometry.Pos.CENTER);

        // Percentile beaten
        VBox percentileBox = new VBox(10);
        percentileBox.setAlignment(javafx.geometry.Pos.CENTER);

        Label percentileValue = new Label(String.format("%.1f%%", percentile));
        percentileValue.setStyle("-fx-font-size: 64px; -fx-font-weight: bold; -fx-text-fill: #4ade80;");

        Label percentileText = new Label("of typists beaten");
        percentileText.setStyle("-fx-font-size: 20px; -fx-text-fill: " + (isDarkMode ? DARK_TEXT : LIGHT_TEXT) + ";");

        ProgressBar percentileBar = new ProgressBar(percentile / 100.0);
        percentileBar.setPrefWidth(400);
        percentileBar.setPrefHeight(20);
        percentileBar.setStyle("-fx-accent: #4ade80;");

        percentileBox.getChildren().addAll(percentileValue, percentileText, percentileBar);

        // Accuracy visualization
        VBox accuracyBox = new VBox(10);
        accuracyBox.setAlignment(javafx.geometry.Pos.CENTER);

        Label accuracyValue = new Label(String.format("%.1f%%", accuracy));
        accuracyValue.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #e2b714;");

        Label accuracyText = new Label("Accuracy");
        accuracyText.setStyle("-fx-font-size: 18px; -fx-text-fill: " + (isDarkMode ? DARK_TEXT : LIGHT_TEXT) + ";");

        ProgressBar accuracyBar = new ProgressBar(accuracy / 100.0);
        accuracyBar.setPrefWidth(400);
        accuracyBar.setPrefHeight(20);
        accuracyBar.setStyle("-fx-accent: #e2b714;");

        accuracyBox.getChildren().addAll(accuracyValue, accuracyText, accuracyBar);

        statsContainer.getChildren().addAll(percentileBox, accuracyBox);

        // Comparison text
        String rankText = getRankText(percentile);
        Label rankLabel = new Label(rankText);
        rankLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: #4ade80; -fx-font-weight: bold;");

        // Back button
        Button backBtn = new Button("← Back to Results");
        backBtn.setFocusTraversable(false);
        backBtn.setStyle("-fx-font-size: 18px; -fx-padding: 12 30; -fx-cursor: hand; -fx-background-color: #646669; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;");

        dominanceBox.getChildren().addAll(
                titleLabel,
                wpmGraphLabel,
                wpmChart,
                statsContainer,
                rankLabel,
                backBtn
        );

        rootVBox.getChildren().add(dominanceBox);
    }

    private LineChart<Number, Number> createWPMChart() {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time (seconds)");
        xAxis.setStyle("-fx-tick-label-fill: " + (isDarkMode ? DARK_TEXT : LIGHT_TEXT) + ";");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("WPM");
        yAxis.setStyle("-fx-tick-label-fill: " + (isDarkMode ? DARK_TEXT : LIGHT_TEXT) + ";");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Your Performance");
        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);
        lineChart.setPrefHeight(300);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("WPM");

        for (int i = 0; i < wpmHistory.size(); i++) {
            series.getData().add(new XYChart.Data<>(timeHistory.get(i), wpmHistory.get(i)));
        }

        lineChart.getData().add(series);

        return lineChart;
    }

    private String getRankText(double percentile) {
        if (percentile >= 99) return "🏆 LEGENDARY! You're in the top 1%!";
        if (percentile >= 95) return "⭐ MASTER! You're in the top 5%!";
        if (percentile >= 90) return "💎 EXPERT! You're in the top 10%!";
        if (percentile >= 80) return "🔥 ADVANCED! Better than most!";
        if (percentile >= 65) return "👍 PROFICIENT! Above average!";
        if (percentile >= 50) return "📈 INTERMEDIATE! Keep improving!";
        if (percentile >= 35) return "💪 DEVELOPING! Practice makes perfect!";
        return "🎯 BEGINNER! Great start, keep going!";
    }

    private void resetToTypingScreen() {
        rootVBox.getChildren().clear();

        // Rebuild UI
        VBox container = new VBox(40);
        container.setAlignment(javafx.geometry.Pos.CENTER);

        HBox topBar = new HBox();
        topBar.setAlignment(javafx.geometry.Pos.CENTER);
        HBox.setMargin(timerLabel, new javafx.geometry.Insets(0, 0, 0, 0));

        timerLabel.setText("0");
        timerLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #e2b714;");

        topBar.getChildren().add(timerLabel);

        // Position dark mode button
        HBox headerBox = new HBox();
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        headerBox.getChildren().add(darkModeBtn);

        VBox mainBox = new VBox(40);
        mainBox.setAlignment(javafx.geometry.Pos.CENTER);
        mainBox.getChildren().addAll(topBar, textFlow);

        rootVBox.getChildren().addAll(headerBox, mainBox);

        if (isDarkMode) {
            rootVBox.setStyle("-fx-background-color: " + DARK_BG + "; -fx-padding: 40;");
        } else {
            rootVBox.setStyle("-fx-background-color: " + LIGHT_BG + "; -fx-padding: 40;");
        }

        loadNextSentence();

        Platform.runLater(() -> rootVBox.requestFocus());
    }

}