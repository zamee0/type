package com.keyy.app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class SettingsController {

    // ── Game Settings (static, accessible from anywhere) ──────────────────────
    private static boolean backspaceEnabled = true;
    public static boolean isBackspaceEnabled() { return backspaceEnabled; }
    public static void setBackspaceEnabled(boolean v) { backspaceEnabled = v; }
    // ──────────────────────────────────────────────────────────────────────────

    @FXML private Button backBtn;
    @FXML private ToggleButton darkModeToggle;
    @FXML private Label darkModeLabel;
    @FXML private ToggleButton backspaceToggle;
    @FXML private Label backspaceLabel;

    private String username;

    @FXML
    public void initialize() {
        // Reflect current state
        darkModeToggle.setSelected(ThemeManager.isDarkMode());
        backspaceToggle.setSelected(backspaceEnabled);
        updateDarkLabel();
        updateBackspaceLabel();

        darkModeToggle.setOnAction(e -> {
            ThemeManager.setDarkMode(darkModeToggle.isSelected());
            updateDarkLabel();
        });

        backspaceToggle.setOnAction(e -> {
            backspaceEnabled = backspaceToggle.isSelected();
            updateBackspaceLabel();
        });

        backBtn.setOnAction(e -> goBack());
    }

    private void updateDarkLabel() {
        if (ThemeManager.isDarkMode()) {
            darkModeLabel.setText("Dark Mode is ON");
            darkModeToggle.setText("Switch to Light");
        } else {
            darkModeLabel.setText("Light Mode is ON");
            darkModeToggle.setText("Switch to Dark");
        }
    }

    private void updateBackspaceLabel() {
        if (backspaceEnabled) {
            backspaceLabel.setText("Backspace is ON — you can correct mistakes in-game.");
            backspaceToggle.setText("Disable");
        } else {
            backspaceLabel.setText("Backspace is OFF — no corrections allowed in-game.");
            backspaceToggle.setText("Enable");
        }
    }

    public void setUsername(String username) { this.username = username; }

    private void goBack() {
        try {
            Stage stage = (Stage) backBtn.getScene().getWindow();
            dashboardcontrol ctrl = SceneHelper.loadScene(stage, "dashboard-view.fxml", "KEYY");
            ctrl.setUsername(username);
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}