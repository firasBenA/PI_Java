package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import tn.esprit.models.Statistiques;
import tn.esprit.services.ServiceStatistiques;


public class StatistiquesController {

    @FXML
    private PieChart pieChart;

    @FXML
    private BarChart<String, Number> barChart;

    private final ServiceStatistiques serviceStatistiques = new ServiceStatistiques();

    @FXML
    public void initialize() {
        updateCharts();
    }

    @FXML
    public void refreshCharts() {
        updateCharts();
    }

    private void updateCharts() {
        Statistiques stats = serviceStatistiques.getReclamationStatistics();
        int total = stats.getTotalCount();
        int enAttente = stats.getEnAttenteCount();
        int traite = stats.getTraiteCount();

        // Update PieChart
        double enAttentePercent = total > 0 ? (enAttente * 100.0 / total) : 0;
        double traitePercent = total > 0 ? (traite * 100.0 / total) : 0;

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data(String.format("En Attente: %d (%.1f%%)", enAttente, enAttentePercent), enAttente),
                new PieChart.Data(String.format("Traité: %d (%.1f%%)", traite, traitePercent), traite)
        );
        pieChart.setData(pieChartData);
        pieChart.setTitle("Répartition par État");

        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre de Réclamations");
        series.getData().add(new XYChart.Data<>("Tous", total));
        series.getData().add(new XYChart.Data<>("En Attente", enAttente));
        series.getData().add(new XYChart.Data<>("Traité", traite));

        barChart.getData().clear();
        barChart.getData().add(series);
        barChart.setTitle("Nombre par État");
    }
}