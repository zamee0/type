package com.keyy.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneHelper {

    /**
     * Loads a new FXML scene while preserving the current window size exactly.
     * Returns the controller so the caller can pass data to it.
     */
    public static <T> T loadScene(Stage stage, String fxmlName, String title) throws Exception {
        double w = stage.getWidth();
        double h = stage.getHeight();

        FXMLLoader loader = new FXMLLoader(KeyyApplication.class.getResource(fxmlName));
        Scene scene = new Scene(loader.load(), w, h);

        stage.setScene(scene);
        stage.setTitle(title);
        // Force exact size preservation
        stage.setWidth(w);
        stage.setHeight(h);

        return loader.getController();
    }
}