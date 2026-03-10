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
        Scene scene = new Scene(loader.load(), 1100, 700);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        ThemeManager.register(scene);
        stage.setTitle("KEYY");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }
    public static void main(String[] args) { launch(args); }
}