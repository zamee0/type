package com.keyy.app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class dashboardcontrol {
    @FXML private Label welcomeLabel;
    @FXML private Button startGameBtn;
    @FXML private Button startGameBtnCenter;
    @FXML private Button profileBtn;
    @FXML private Button statsBtn;
    @FXML private Button localhostBtn;
    @FXML private Button leaderboardBtn;
    @FXML private Button settingsBtn;
    @FXML private Button logoutBtn;

    private String username;

    @FXML
    public void initialize() {
        startGameBtn.setOnAction(e -> goToGame());
        startGameBtnCenter.setOnAction(e -> goToGame());

        profileBtn.setOnAction(e -> nav("profile-view.fxml", "KEYY — Profile", ctrl -> {
            if (ctrl instanceof ProfileController) ((ProfileController) ctrl).setUsername(username);
        }));
        statsBtn.setOnAction(e -> nav("stats-view.fxml", "KEYY — Stats", ctrl -> {
            if (ctrl instanceof StatsController) ((StatsController) ctrl).setUsername(username);
        }));
        localhostBtn.setOnAction(e -> nav("localhost-view.fxml", "KEYY — Local Host", ctrl -> {
            if (ctrl instanceof LocalhostController) ((LocalhostController) ctrl).setUsername(username);
        }));
        leaderboardBtn.setOnAction(e -> nav("leaderboard-view.fxml", "KEYY — Leaderboard", ctrl -> {
            if (ctrl instanceof LeaderboardController) ((LeaderboardController) ctrl).setUsername(username);
        }));
        settingsBtn.setOnAction(e -> nav("settings-view.fxml", "KEYY — Settings", ctrl -> {
            if (ctrl instanceof SettingsController) ((SettingsController) ctrl).setUsername(username);
        }));
        logoutBtn.setOnAction(e -> {
            try {
                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                SceneHelper.loadScene(stage, "login-view.fxml", "KEYY");
            } catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    private void goToGame() {
        nav("game-setup-view.fxml", "KEYY — Game Setup", ctrl -> {
            if (ctrl instanceof GameSetupController) ((GameSetupController) ctrl).setUsername(username);
        });
    }

    public void setUsername(String username) {
        this.username = username;
        welcomeLabel.setText("Welcome back, " + username);
    }

    private void nav(String fxml, String title, ControllerInit init) {
        try {
            Stage stage = (Stage) startGameBtn.getScene().getWindow();
            Object ctrl = SceneHelper.loadScene(stage, fxml, title);
            init.setup(ctrl);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FunctionalInterface interface ControllerInit { void setup(Object c); }
}