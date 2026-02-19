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

    @FXML
    public void initialize() {
        loginBtn.setOnAction(e -> handleLogin());
        registerBtn.setOnAction(e -> handleRegister());
        passwordField.setOnAction(e -> handleLogin());
        usernameField.setOnAction(e -> passwordField.requestFocus());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
            return;
        }

        if (UserManager.loginUser(username, password)) {
            try {
                Stage stage = (Stage) loginBtn.getScene().getWindow();
                dashboardcontrol ctrl = SceneHelper.loadScene(stage, "dashboard-view.fxml", "KEYY - Dashboard");
                ctrl.setUsername(username);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            messageLabel.setText("Invalid username or password.");
        }
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
            return;
        }

        if (UserManager.registerUser(username, password)) {
            messageLabel.setText("Account created! You can now log in.");
        } else {
            messageLabel.setText("Username already taken.");
        }
    }
}