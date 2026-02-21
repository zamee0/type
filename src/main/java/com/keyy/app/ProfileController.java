package com.keyy.app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;

public class ProfileController {

    @FXML private Label usernameLabel;
    @FXML private Label totalGamesLabel;
    @FXML private Label bestWpmLabel;
    @FXML private Label avgAccLabel;
    @FXML private VBox historyList;
    @FXML private Label emptyLabel;
    @FXML private Button backBtn;

    private String username;

    @FXML
    public void initialize() {
        backBtn.setOnAction(e -> goBack());
    }

    public void setUsername(String username) {
        this.username = username;
        usernameLabel.setText(username);
        loadHistory();
    }

    private void loadHistory() {
        List<String[]> history = UserManager.getUserHistory(username);
        historyList.getChildren().clear();

        if (history.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            totalGamesLabel.setText("0");
            bestWpmLabel.setText("—");
            avgAccLabel.setText("—");
            return;
        }

        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        // Compute summary stats
        int totalGames = history.size();
        double bestWpm = 0;
        double totalAcc = 0;

        for (String[] entry : history) {
            double wpm = Double.parseDouble(entry[0]);
            double acc = Double.parseDouble(entry[1]);
            if (wpm > bestWpm) bestWpm = wpm;
            totalAcc += acc;
        }

        double avgAcc = totalAcc / totalGames;

        totalGamesLabel.setText(String.valueOf(totalGames));
        bestWpmLabel.setText(String.format("%.0f WPM", bestWpm));
        avgAccLabel.setText(String.format("%.1f%%", avgAcc));

        // Render history cards
        int num = 1;
        for (String[] entry : history) {
            // entry: [wpm, accuracy, timeSeconds, timestamp]
            HBox card = new HBox();
            card.setSpacing(0);
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: #e8e8e8;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 10;" +
                            "-fx-background-radius: 10;" +
                            "-fx-padding: 16 20 16 20;"
            );

            // Rank number
            Label rankLabel = new Label("#" + num);
            rankLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa; -fx-font-weight: bold; -fx-min-width: 36;");

            // Main info
            VBox info = new VBox(4);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label wpmLine = new Label(entry[0] + " WPM");
            wpmLine.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #222222;");
            Label detailLine = new Label("Accuracy: " + entry[1] + "%   •   Time: " + entry[2] + "s");
            detailLine.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");
            info.getChildren().addAll(wpmLine, detailLine);

            // Timestamp
            Label tsLabel = new Label(entry[3]);
            tsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #bbbbbb;");
            tsLabel.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

            card.getChildren().addAll(rankLabel, info, tsLabel);
            historyList.getChildren().add(card);
            num++;
        }
    }

    private void goBack() {
        try {
            Stage stage = (Stage) backBtn.getScene().getWindow();
            dashboardcontrol ctrl = SceneHelper.loadScene(stage, "dashboard-view.fxml", "KEYY - Dashboard");
            ctrl.setUsername(username);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}