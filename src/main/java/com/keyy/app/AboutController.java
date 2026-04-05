package com.keyy.app;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class AboutController {

    @FXML private Button backBtn;
    private String username;

    @FXML
    public void initialize() {
        backBtn.setOnAction(e -> goBack());
    }

    public void setUsername(String u) { this.username = u; }

    private void goBack() {
        try {
            Stage stage = (Stage) backBtn.getScene().getWindow();
            dashboardcontrol ctrl = SceneHelper.loadScene(stage, "dashboard-view.fxml", "KEYY");
            ctrl.setUsername(username);
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
