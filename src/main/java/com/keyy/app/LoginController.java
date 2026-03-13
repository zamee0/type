package com.keyy.app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Button loginBtn;
    @FXML private Button registerBtn;
    @FXML private Button themeBtn;

    @FXML
    public void initialize() {
        loginBtn.setOnAction(e -> handleLogin());
        registerBtn.setOnAction(e -> handleRegister());
        passwordField.setOnAction(e -> handleLogin());
        usernameField.setOnAction(e -> passwordField.requestFocus());

        updateThemeIcon();
        themeBtn.setOnAction(e -> {
            ThemeManager.setDarkMode(!ThemeManager.isDarkMode());
            updateThemeIcon();
        });
    }

    private void updateThemeIcon() {
        themeBtn.setText(ThemeManager.isDarkMode() ? "☀" : "☽");
    }

    private void handleLogin() {
        String u = usernameField.getText().trim();
        String p = passwordField.getText().trim();
        if (u.isEmpty() || p.isEmpty()) { messageLabel.setText("Please fill in all fields."); return; }
        if (UserManager.loginUser(u, p)) {
            try {
                Stage stage = (Stage) loginBtn.getScene().getWindow();
                dashboardcontrol ctrl = SceneHelper.loadScene(stage, "dashboard-view.fxml", "KEYY");
                ctrl.setUsername(u);
            } catch (Exception ex) { ex.printStackTrace(); }
        } else { messageLabel.setText("Invalid username or password."); }
    }

    private void handleRegister() {
        String u = usernameField.getText().trim();
        String p = passwordField.getText().trim();
        if (u.isEmpty() || p.isEmpty()) { messageLabel.setText("Please fill in all fields."); return; }
        if (UserManager.registerUser(u, p)) {
            messageLabel.setText("Account created! You can now log in.");
            messageLabel.getStyleClass().removeAll("msg-error");
            messageLabel.getStyleClass().add("msg-success");
        } else {
            messageLabel.setText("Username already taken.");
            messageLabel.getStyleClass().add("msg-error");
        }
    }
}