package com.keyy.app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class dashboardcontrol {

    @FXML private Label welcomeLabel;
    @FXML private Button startGameBtn;
    @FXML private Button startGameBtnCenter;
    @FXML private Button profileBtn;
    @FXML private Button localhostBtn;
    @FXML private Button leaderboardBtn;
    @FXML private Button settingsBtn;
    @FXML private Button logoutBtn;

    private String username;

    @FXML
    public void initialize() {
        startGameBtn.setOnAction(e -> navigate("typing-view.fxml", "KEYY - Typing Test", ctrl -> {
            if (ctrl instanceof TypingController)
                ((TypingController) ctrl).setUsername(username);
        }));

        startGameBtnCenter.setOnAction(e -> navigate("typing-view.fxml", "KEYY - Typing Test", ctrl -> {
            if (ctrl instanceof TypingController)
                ((TypingController) ctrl).setUsername(username);
        }));

        profileBtn.setOnAction(e -> navigate("profile-view.fxml", "KEYY - My Profile", ctrl -> {
            if (ctrl instanceof ProfileController)
                ((ProfileController) ctrl).setUsername(username);
        }));

        localhostBtn.setOnAction(e -> navigate("localhost-view.fxml", "KEYY - Local Host", ctrl -> {
            if (ctrl instanceof LocalhostController)
                ((LocalhostController) ctrl).setUsername(username);
        }));

        leaderboardBtn.setOnAction(e -> navigate("leaderboard-view.fxml", "KEYY - Leaderboard", ctrl -> {
            if (ctrl instanceof LeaderboardController)
                ((LeaderboardController) ctrl).setUsername(username);
        }));

        settingsBtn.setOnAction(e -> navigate("settings-view.fxml", "KEYY - Settings", ctrl -> {
            if (ctrl instanceof SettingsController)
                ((SettingsController) ctrl).setUsername(username);
        }));

        logoutBtn.setOnAction(e -> logout());
    }

    public void setUsername(String username) {
        this.username = username;
        welcomeLabel.setText("Welcome, " + username + "!");
    }

    private void navigate(String fxml, String title, ControllerInit init) {
        try {
            Stage stage = (Stage) startGameBtn.getScene().getWindow();
            Object ctrl = SceneHelper.loadScene(stage, fxml, title);
            init.setup(ctrl);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void logout() {
        try {
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            SceneHelper.loadScene(stage, "login-view.fxml", "KEYY - Typing Speed Test");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FunctionalInterface
    interface ControllerInit {
        void setup(Object controller);
    }
}