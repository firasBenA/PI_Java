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

    @FXML
    private PieChart diagnostiquePieChart; // PieChart for Diagnostique

    @FXML
    private BarChart<String, Number> diagnostiqueBarChart; // BarChart for Diagnostique

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
        // Fetch and update Reclamation statistics
        Statistiques stats = serviceStatistiques.getReclamationStatistics();
        int total = stats.getTotalCount();
        int enAttente = stats.getEnAttenteCount();
        int traite = stats.getTraiteCount();

        // Update PieChart for Reclamation
        double enAttentePercent = total > 0 ? (enAttente * 100.0 / total) : 0;
        double traitePercent = total > 0 ? (traite * 100.0 / total) : 0;

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data(String.format("En Attente: %d (%.1f%%)", enAttente, enAttentePercent), enAttente),
                new PieChart.Data(String.format("Traité: %d (%.1f%%)", traite, traitePercent), traite)
        );
        pieChart.setData(pieChartData);
        pieChart.setTitle("Répartition par État");

        // Update BarChart for Reclamation
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre de Réclamations");
        series.getData().add(new XYChart.Data<>("Tous", total));
        series.getData().add(new XYChart.Data<>("En Attente", enAttente));
        series.getData().add(new XYChart.Data<>("Traité", traite));

        barChart.getData().clear();
        barChart.getData().add(series);
        barChart.setTitle("Nombre par État");

        // Fetch and update Diagnostique statistics based on status (0 = pending, 1 = treated)
        int diagnostiqueTotal = serviceStatistiques.getDiagnostiqueCount(); // Total diagnostiques
        int diagnostiquePending = serviceStatistiques.getDiagnostiqueCountByStatus(0); // Pending (status = 0)
        int diagnostiqueTreated = serviceStatistiques.getDiagnostiqueCountByStatus(1); // Treated (status = 1)

        // Update PieChart for Diagnostique
        double diagnostiquePendingPercent = diagnostiqueTotal > 0 ? (diagnostiquePending * 100.0 / diagnostiqueTotal) : 0;
        double diagnostiqueTreatedPercent = diagnostiqueTotal > 0 ? (diagnostiqueTreated * 100.0 / diagnostiqueTotal) : 0;

        ObservableList<PieChart.Data> diagnostiquePieChartData = FXCollections.observableArrayList(
                new PieChart.Data(String.format("En Attente: %d (%.1f%%)", diagnostiquePending, diagnostiquePendingPercent), diagnostiquePending),
                new PieChart.Data(String.format("Traité: %d (%.1f%%)", diagnostiqueTreated, diagnostiqueTreatedPercent), diagnostiqueTreated)
        );
        diagnostiquePieChart.setData(diagnostiquePieChartData);
        diagnostiquePieChart.setTitle("Répartition Diagnostiques");

        // Update BarChart for Diagnostique
        XYChart.Series<String, Number> diagnostiqueSeries = new XYChart.Series<>();
        diagnostiqueSeries.setName("Nombre de Diagnostiques");
        diagnostiqueSeries.getData().add(new XYChart.Data<>("Tous", diagnostiqueTotal));
        diagnostiqueSeries.getData().add(new XYChart.Data<>("En Attente", diagnostiquePending));
        diagnostiqueSeries.getData().add(new XYChart.Data<>("Traité", diagnostiqueTreated));

        diagnostiqueBarChart.getData().clear();
        diagnostiqueBarChart.getData().add(diagnostiqueSeries);
        diagnostiqueBarChart.setTitle("Nombre par État (Diagnostiques)");
    }
}
