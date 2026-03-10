package com.keyy.app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
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
        double avgAcc = totalAcc / history.size();
        double avgWpm = totalWpm / history.size();

        totalGamesLabel.setText(String.valueOf(history.size()));
        bestWpmLabel.setText(String.format("%.0f", bestWpm));
        avgAccLabel.setText(String.format("%.1f%%", avgAcc));
        avgWpmLabel.setText(String.format("%.0f", avgWpm));

        // Render cards
        int num = 1;
        for (String[] entry : history) {
            HBox card = new HBox(16);
            card.setAlignment(Pos.CENTER_LEFT);
            card.getStyleClass().add("history-card");

            Label rankLbl = new Label("#" + num);
            rankLbl.getStyleClass().add("history-rank");
            rankLbl.setMinWidth(36);

            VBox info = new VBox(3);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label wpmLbl = new Label(entry[0] + " WPM");
            wpmLbl.getStyleClass().add("history-wpm");
            Label detailLbl = new Label("Accuracy: " + entry[1] + "%   •   Time: " + entry[2] + "s");
            detailLbl.getStyleClass().add("history-detail");
            info.getChildren().addAll(wpmLbl, detailLbl);

            Label tsLbl = new Label(entry[3]);
            tsLbl.getStyleClass().add("history-timestamp");

            card.getChildren().addAll(rankLbl, info, tsLbl);
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