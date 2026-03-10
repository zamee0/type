package com.keyy.app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class StatsController {

    @FXML private Button backBtn;

    private String username;

    @FXML
    public void initialize() {
        backBtn.setOnAction(e -> {
            try {
                Stage stage = (Stage) backBtn.getScene().getWindow();
                dashboardcontrol ctrl = SceneHelper.loadScene(stage, "dashboard-view.fxml", "KEYY");
                ctrl.setUsername(username);
            } catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
