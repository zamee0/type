package com.keyy.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class KeyyApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        UserManager.initialize();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
        Scene scene = new Scene(loader.load(), 500, 400);
        
        stage.setTitle("Typing Speed Test");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
