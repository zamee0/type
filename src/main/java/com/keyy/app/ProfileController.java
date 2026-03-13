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
    @FXML private Label avgWpmLabel;
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
            avgWpmLabel.setText("—");
            return;
        }

        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        double bestWpm = 0, totalAcc = 0, totalWpm = 0;
        for (String[] e : history) {
            double wpm = Double.parseDouble(e[0]);
            double acc = Double.parseDouble(e[1]);
            if (wpm > bestWpm) bestWpm = wpm;
            totalAcc += acc;
            totalWpm += wpm;
        }

        totalGamesLabel.setText(String.valueOf(history.size()));
        bestWpmLabel.setText(String.format("%.0f", bestWpm));
        avgAccLabel.setText(String.format("%.1f%%", totalAcc / history.size()));
        avgWpmLabel.setText(String.format("%.0f", totalWpm / history.size()));

        int num = 1;
        for (String[] entry : history) {
            VBox card = new VBox(4);
            card.getStyleClass().add("history-card");
            card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #E0DDD8; -fx-border-width: 1; -fx-border-radius: 10; -fx-padding: 14 18 14 18;");
            if (ThemeManager.isDarkMode()) card.setStyle("-fx-background-color: #242424; -fx-background-radius: 10; -fx-border-color: #3A3A3A; -fx-border-width: 1; -fx-border-radius: 10; -fx-padding: 14 18 14 18;");

            Label rankLbl = new Label("#" + num);
            rankLbl.getStyleClass().add("history-rank");

            Label wpmLbl = new Label(entry[0] + " WPM");
            wpmLbl.getStyleClass().add("history-wpm");

            Label detailLbl = new Label("Accuracy: " + entry[1] + "%   •   Time: " + entry[2] + "s");
            detailLbl.getStyleClass().add("history-detail");

            Label tsLbl = new Label(entry[3]);
            tsLbl.getStyleClass().add("history-timestamp");

            card.getChildren().addAll(rankLbl, wpmLbl, detailLbl, tsLbl);
            historyList.getChildren().add(card);
            num++;
        }
    }

    private void goBack() {
        try {
            Stage stage = (Stage) backBtn.getScene().getWindow();
            dashboardcontrol ctrl = SceneHelper.loadScene(stage, "dashboard-view.fxml", "KEYY");
            ctrl.setUsername(username);
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}