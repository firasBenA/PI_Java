package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import tn.esprit.models.Statistiques;
import tn.esprit.services.ServiceStatistiques;

public class StatistiquesController {

    @FXML
    private PieChart pieChart;

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private PieChart ratingPieChart; // PieChart for client satisfaction

    @FXML
    private Label ratingTotalLabel; // Label for total ratings in PieChart center

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

        // Update Status PieChart
        double enAttentePercent = total > 0 ? (enAttente * 100.0 / total) : 0;
        double traitePercent = total > 0 ? (traite * 100.0 / total) : 0;

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data(String.format("En Attente: %d (%.1f%%)", enAttente, enAttentePercent), enAttente),
                new PieChart.Data(String.format("Traité: %d (%.1f%%)", traite, traitePercent), traite)
        );
        pieChart.setData(pieChartData);
        pieChart.setTitle("Répartition par État");

        // Update BarChart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre de Réclamations");
        series.getData().add(new XYChart.Data<>("Tous", total));
        series.getData().add(new XYChart.Data<>("En Attente", enAttente));
        series.getData().add(new XYChart.Data<>("Traité", traite));

        barChart.getData().clear();
        barChart.getData().add(series);
        barChart.setTitle("Nombre par État");

        // Update Rating PieChart
        int high = stats.getHighSatisfactionCount();
        int moderate = stats.getModerateSatisfactionCount();
        int low = stats.getLowSatisfactionCount();
        int unrated = stats.getUnratedCount();
        int totalRatings = high + moderate + low + unrated;

        double highPercent = totalRatings > 0 ? (high * 100.0 / totalRatings) : 0;
        double moderatePercent = totalRatings > 0 ? (moderate * 100.0 / totalRatings) : 0;
        double lowPercent = totalRatings > 0 ? (low * 100.0 / totalRatings) : 0;
        double unratedPercent = totalRatings > 0 ? (unrated * 100.0 / totalRatings) : 0;

        ObservableList<PieChart.Data> ratingPieChartData = FXCollections.observableArrayList(
                new PieChart.Data(String.format("Élevé (4-5): %d (%.1f%%)", high, highPercent), high),
                new PieChart.Data(String.format("Modéré (2-3): %d (%.1f%%)", moderate, moderatePercent), moderate),
                new PieChart.Data(String.format("Faible (1): %d (%.1f%%)", low, lowPercent), low),
                new PieChart.Data(String.format("Non noté: %d (%.1f%%)", unrated, unratedPercent), unrated)
        );
        ratingPieChart.setData(ratingPieChartData);
        ratingPieChart.setTitle("Satisfaction Client (Évaluations)");

        // Update total ratings label
        ratingTotalLabel.setText("Total: " + totalRatings);
    }
}