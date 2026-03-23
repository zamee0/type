package com.keyy.app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class SettingsController {

    private static boolean backspaceEnabled = true;
    public static boolean isBackspaceEnabled() { return backspaceEnabled; }
    public static void setBackspaceEnabled(boolean v) { backspaceEnabled = v; }

    @FXML private Button backBtn;
    @FXML private ToggleButton darkModeToggle;
    @FXML private Label darkModeLabel;
    @FXML private ToggleButton backspaceToggle;
    @FXML private Label backspaceLabel;
    @FXML private Button clearHistoryBtn;
    @FXML private Label clearHistoryLabel;

    private String username;

    @FXML
    public void initialize() {
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

        clearHistoryBtn.setOnAction(e -> handleClearHistory());
        backBtn.setOnAction(e -> goBack());

        javafx.application.Platform.runLater(() -> {
            if (backBtn.getScene() != null) {
                backBtn.getScene().getAccelerators().put(
                        new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN),
                        () -> {
                            ThemeManager.setDarkMode(!ThemeManager.isDarkMode());
                            darkModeToggle.setSelected(ThemeManager.isDarkMode());
                            updateDarkLabel();
                        }
                );
            }
        });
    }

    private void handleClearHistory() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear History");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will permanently delete all your game history.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                UserManager.clearHistory(username);
                clearHistoryLabel.setText("History cleared.");
            }
        });
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