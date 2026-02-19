package com.keyy.app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class SettingsController {

    @FXML private Button backBtn;

    private String username;

    @FXML
    public void initialize() {
        backBtn.setOnAction(e -> goBack());
    }

    public void setUsername(String username) {
        this.username = username;
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