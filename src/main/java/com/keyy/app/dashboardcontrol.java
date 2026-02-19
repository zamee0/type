package com.keyy.app;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class dashboardcontrol {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button startTestBtn;

    @FXML
    private Button progressBtn;

    @FXML
    private Button logoutBtn;

    private String username;

    @FXML
    public void initialize()
    {
        startTestBtn.setOnAction(e -> startTest());
        progressBtn.setOnAction(e -> showProgress());
        logoutBtn.setOnAction(e -> logout());

    }


    public void setUsername(String username)
    {
        this.username = username;
        welcomeLabel.setText("Welcome, " + username + "!");
    }



    private void startTest() {
        try {
            // Load typing test screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("typing-view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 500);

            // Pass username to typing controller
            TypingController controller = loader.getController();
            controller.setUsername(username);

            // Switch scene
            Stage stage = (Stage) startTestBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Typing Test");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showProgress() {
        System.out.println("");
    }



    private void logout() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Scene scene = new Scene(loader.load(), 500, 400);

            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}