package com.keyy.app;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.*;

public class StatsController {

    @FXML private Button backBtn;
    @FXML private PieChart accuracyPie;
    @FXML private LineChart<String, Number> wpmLineChart;
    @FXML private LineChart<String, Number> accLineChart;
    @FXML private VBox chartsPane;
    @FXML private Label emptyLabel;

    private String username;

    @FXML
    public void initialize() {
        backBtn.setOnAction(e -> goBack());
    }

    public void setUsername(String username) {
        this.username = username;
        loadCharts();
    }

    private void loadCharts() {
        List<String[]> history = UserManager.getUserHistory(username);

        if (history.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            chartsPane.setVisible(false);
            chartsPane.setManaged(false);
            return;
        }

        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);
        chartsPane.setVisible(true);
        chartsPane.setManaged(true);

        buildAccuracyPie(history);
        buildWpmLine(history);
        buildAccLine(history);
    }

    private void buildAccuracyPie(List<String[]> history) {
        int above90 = 0, above80 = 0, above70 = 0, below70 = 0;
        for (String[] e : history) {
            double acc = Double.parseDouble(e[1]);
            if      (acc >= 90) above90++;
            else if (acc >= 80) above80++;
            else if (acc >= 70) above70++;
            else                below70++;
        }
        accuracyPie.getData().clear();
        if (above90 > 0) accuracyPie.getData().add(new PieChart.Data("90%+ (" + above90 + ")", above90));
        if (above80 > 0) accuracyPie.getData().add(new PieChart.Data("80–90% (" + above80 + ")", above80));
        if (above70 > 0) accuracyPie.getData().add(new PieChart.Data("70–80% (" + above70 + ")", above70));
        if (below70 > 0) accuracyPie.getData().add(new PieChart.Data("< 70% (" + below70 + ")", below70));
    }

    private void buildWpmLine(List<String[]> history) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("WPM");
        List<String[]> ordered = new ArrayList<>(history);
        Collections.reverse(ordered);
        int max = Math.min(ordered.size(), 20);
        for (int i = 0; i < max; i++) {
            double wpm = Double.parseDouble(ordered.get(i)[0]);
            series.getData().add(new XYChart.Data<>(String.valueOf(i + 1), wpm));
        }
        wpmLineChart.getData().clear();
        wpmLineChart.getData().add(series);
        wpmLineChart.setLegendVisible(false);
    }

    private void buildAccLine(List<String[]> history) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Accuracy");
        List<String[]> ordered = new ArrayList<>(history);
        Collections.reverse(ordered);
        int max = Math.min(ordered.size(), 20);
        for (int i = 0; i < max; i++) {
            double acc = Double.parseDouble(ordered.get(i)[1]);
            series.getData().add(new XYChart.Data<>(String.valueOf(i + 1), acc));
        }
        accLineChart.getData().clear();
        accLineChart.getData().add(series);
        accLineChart.setLegendVisible(false);
    }

    private void goBack() {
        try {
            Stage stage = (Stage) backBtn.getScene().getWindow();
            dashboardcontrol ctrl = SceneHelper.loadScene(stage, "dashboard-view.fxml", "KEYY");
            ctrl.setUsername(username);
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}