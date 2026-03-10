package com.keyy.app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;

public class LeaderboardController {
    @FXML private VBox leaderboardList;
    @FXML private Label emptyLabel;
    @FXML private Button backBtn;
    private String username;

    @FXML
    public void initialize() {
        backBtn.setOnAction(e -> goBack());
        loadLeaderboard();
    }

    public void setUsername(String username) { this.username = username; }

    private void loadLeaderboard() {
        List<String[]> top = UserManager.getLeaderboard();
        leaderboardList.getChildren().clear();
        if (top.isEmpty()) { emptyLabel.setVisible(true); emptyLabel.setManaged(true); return; }
        emptyLabel.setVisible(false); emptyLabel.setManaged(false);

        String[] medals = {"🥇", "🥈", "🥉"};
        for (int i = 0; i < top.size(); i++) {
            String[] entry = top.get(i);
            HBox row = new HBox(16);
            row.getStyleClass().add("lb-row");
            if (i == 0) row.getStyleClass().add("lb-gold");
            else if (i == 1) row.getStyleClass().add("lb-silver");
            else if (i == 2) row.getStyleClass().add("lb-bronze");

            Label rankLbl = new Label(i < 3 ? medals[i] : "#" + (i + 1));
            rankLbl.getStyleClass().add("lb-rank");
            rankLbl.setMinWidth(48);

            Label nameLbl = new Label(entry[0]);
            nameLbl.getStyleClass().add("lb-name");
            HBox.setHgrow(nameLbl, Priority.ALWAYS);

            Label wpmLbl = new Label(entry[1] + " WPM");
            wpmLbl.getStyleClass().add("lb-wpm");

            row.getChildren().addAll(rankLbl, nameLbl, wpmLbl);
            leaderboardList.getChildren().add(row);
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