package com.keyy.app;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill all fields");
            return;
        }

        if (UserManager.loginUser(username, password)) {
            loadDashboard(username); // ← Dashboard load koro
        } else {
            messageLabel.setText("Invalid credentials");
        }
    }
    
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill all fields");
            return;
        }
        
        if (UserManager.registerUser(username, password)) {
            messageLabel.setText("registration is successful.Now you can login.");
            messageLabel.setStyle("-fx-text-fill: green;");
        } else {
            messageLabel.setText("Username already exists");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void loadDashboard(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard-view.fxml"));
            Scene scene = new Scene(loader.load(), 600, 500);


            dashboardcontrol controller = loader.getController();
            controller.setUsername(username);

            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Dashboard - " + username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
