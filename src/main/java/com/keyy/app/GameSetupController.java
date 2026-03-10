package com.keyy.app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class GameSetupController {

    @FXML private Button backBtn;
    @FXML private Button startBtn;

    // Difficulty buttons
    @FXML private Button easyBtn;
    @FXML private Button normalBtn;
    @FXML private Button hardBtn;

    // Time buttons
    @FXML private Button time15;
    @FXML private Button time30;
    @FXML private Button time60;
    @FXML private Button time120;

    private String username;

    // Track selection manually — no ToggleGroup needed
    private TypingController.Difficulty selectedDifficulty = TypingController.Difficulty.NORMAL;
    private int selectedSeconds = 60;

    @FXML
    public void initialize() {
        // Set defaults visually
        selectDifficulty(normalBtn, TypingController.Difficulty.NORMAL);
        selectTime(time60, 60);

        // Difficulty button actions
        easyBtn.setOnAction(e -> selectDifficulty(easyBtn, TypingController.Difficulty.EASY));
        normalBtn.setOnAction(e -> selectDifficulty(normalBtn, TypingController.Difficulty.NORMAL));
        hardBtn.setOnAction(e -> selectDifficulty(hardBtn, TypingController.Difficulty.HARD));

        // Time button actions
        time15.setOnAction(e -> selectTime(time15, 15));
        time30.setOnAction(e -> selectTime(time30, 30));
        time60.setOnAction(e -> selectTime(time60, 60));
        time120.setOnAction(e -> selectTime(time120, 120));

        backBtn.setOnAction(e -> goBack());
        startBtn.setOnAction(e -> startGame());
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private void selectDifficulty(Button selected, TypingController.Difficulty diff) {
        selectedDifficulty = diff;
        // Reset all
        easyBtn.getStyleClass().removeAll("setup-btn-active");
        normalBtn.getStyleClass().removeAll("setup-btn-active");
        hardBtn.getStyleClass().removeAll("setup-btn-active");
        // Highlight selected
        selected.getStyleClass().add("setup-btn-active");
    }

    private void selectTime(Button selected, int seconds) {
        selectedSeconds = seconds;
        // Reset all
        time15.getStyleClass().removeAll("setup-btn-active");
        time30.getStyleClass().removeAll("setup-btn-active");
        time60.getStyleClass().removeAll("setup-btn-active");
        time120.getStyleClass().removeAll("setup-btn-active");
        // Highlight selected
        selected.getStyleClass().add("setup-btn-active");
    }

    private void startGame() {
        try {
            Stage stage = (Stage) startBtn.getScene().getWindow();
            TypingController ctrl = SceneHelper.loadScene(
                    stage, "typing-view.fxml", "KEYY — Typing");
            ctrl.setup(username, selectedDifficulty, selectedSeconds);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void goBack() {
        try {
            Stage stage = (Stage) backBtn.getScene().getWindow();
            dashboardcontrol ctrl = SceneHelper.loadScene(
                    stage, "dashboard-view.fxml", "KEYY");
            ctrl.setUsername(username);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}