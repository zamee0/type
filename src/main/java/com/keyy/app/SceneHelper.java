package com.keyy.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneHelper {
    public static <T> T loadScene(Stage stage, String fxmlName, String title) throws Exception {
        double w = stage.getWidth();
        double h = stage.getHeight();

        FXMLLoader loader = new FXMLLoader(KeyyApplication.class.getResource(fxmlName));
        Scene scene = new Scene(loader.load(), w, h);
        scene.getStylesheets().add(KeyyApplication.class.getResource("styles.css").toExternalForm());
        ThemeManager.register(scene);   // apply current dark/light immediately

        stage.setScene(scene);
        stage.setTitle(title);
        stage.setWidth(w);
        stage.setHeight(h);

        return loader.getController();
    }
}