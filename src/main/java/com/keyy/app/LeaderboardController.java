package com.keyy.app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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

    public void setUsername(String username) {
        this.username = username;
    }

    private void loadLeaderboard() {
        List<String[]> top = UserManager.getLeaderboard();
        leaderboardList.getChildren().clear();

        if (top.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            return;
        }

        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        String[] medals = {"#1 🥇", "#2 🥈", "#3 🥉"};

        for (int i = 0; i < top.size(); i++) {
            String[] entry = top.get(i);
            String rank = i < 3 ? medals[i] : "#" + (i + 1);

            Label row = new Label(String.format("%-8s  %-20s  %s WPM", rank, entry[0], entry[1]));
            row.setStyle("-fx-font-size: 15px; -fx-font-family: monospace; -fx-padding: 8 0 8 0;");
            leaderboardList.getChildren().add(row);

            Separator sep = new Separator();
            leaderboardList.getChildren().add(sep);
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