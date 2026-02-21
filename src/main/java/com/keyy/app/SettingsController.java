package com.keyy.app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class SettingsController {

    // ─── Game Settings (static so other classes can access) ───────────────────
    private static boolean backspaceEnabled = true;

    public static boolean isBackspaceEnabled() {
        return backspaceEnabled;
    }

    public static void setBackspaceEnabled(boolean enabled) {
        backspaceEnabled = enabled;
    }
    // ──────────────────────────────────────────────────────────────────────────

    @FXML private Button backBtn;
    @FXML private ToggleButton backspaceToggle;
    @FXML private Label backspaceStatusLabel;

    private String username;

    @FXML
    public void initialize() {
        backspaceToggle.setSelected(backspaceEnabled);
        updateLabel();

        backspaceToggle.setOnAction(e -> {
            backspaceEnabled = backspaceToggle.isSelected();
            updateLabel();
        });

        backBtn.setOnAction(e -> goBack());
    }

    private void updateLabel() {
        if (backspaceEnabled) {
            backspaceStatusLabel.setText("Backspace is ON — you can correct mistakes during the game.");
            backspaceToggle.setText("Disable Backspace");
        } else {
            backspaceStatusLabel.setText("Backspace is OFF — no corrections allowed during the game.");
            backspaceToggle.setText("Enable Backspace");
        }
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