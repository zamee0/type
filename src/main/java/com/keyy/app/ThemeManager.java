package com.keyy.app;

import javafx.scene.Scene;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    private static boolean darkMode = false;
    private static final List<Scene> scenes = new ArrayList<>();

    public static boolean isDarkMode() { return darkMode; }

    public static void setDarkMode(boolean dark) {
        darkMode = dark;
        applyAll();
    }

    public static void register(Scene scene) {
        if (scene == null) return;
        scenes.removeIf(s -> s == null);
        if (!scenes.contains(scene)) scenes.add(scene);
        apply(scene);
    }

    public static void apply(Scene scene) {
        if (scene == null) return;
        scene.getRoot().getStyleClass().removeAll("dark-mode", "light-mode");
        scene.getRoot().getStyleClass().add(darkMode ? "dark-mode" : "light-mode");
    }

    private static void applyAll() {
        scenes.removeIf(s -> s == null);
        scenes.forEach(ThemeManager::apply);
    }
}