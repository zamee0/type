package com.keyy.app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

public class ProfileController {

    @FXML private Label usernameLabel;
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
        usernameLabel.setText("Profile: " + username);
        loadHistory();
    }

    private void loadHistory() {
        List<String[]> history = UserManager.getUserHistory(username);
        historyList.getChildren().clear();

        if (history.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            return;
        }

        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        int num = 1;
        for (String[] entry : history) {
            // entry: [wpm, accuracy, timeSeconds, timestamp]
            Label row = new Label(String.format(
                    "#%d  |  %s WPM  |  %s Accuracy  |  Time: %ss  |  %s",
                    num, entry[0], entry[1], entry[2], entry[3]
            ));
            row.setStyle("-fx-font-size: 14px; -fx-padding: 8 0 8 0;");
            historyList.getChildren().add(row);

            // Divider
            javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
            historyList.getChildren().add(sep);
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