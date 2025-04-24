package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DoctorStatsController {
    @FXML private Label specialiteLabel;
    @FXML private Label nomMedecinLabel;
    @FXML private Label statsTitleLabel;
    @FXML private Label approvedLabel;
    @FXML private Label canceledLabel;
    @FXML private PieChart genderChart;
    @FXML private BarChart<String, Number> ageChart;
    @FXML private BarChart<String, Number> dayChart;

    private Connection connection;
    private String doctorId; // ou autre identifiant du médecin

    public void initialize(Connection connection, String doctorId) {
        this.connection = connection;
        this.doctorId = doctorId;
        loadDoctorData();
        loadStatistics();
    }

    private void loadDoctorData() {
        String query = "SELECT nom, prenom, specialite FROM user WHERE telephone = ? AND role = 'MEDECIN'";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, doctorId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String specialite = rs.getString("specialite");

                nomMedecinLabel.setText("  - " + prenom + " " + nom);
                specialiteLabel.setText("- " + specialite);
                statsTitleLabel.setText("Statistiques du médecin " + prenom + " " + nom);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadStatistics() {
        // 1. Statistiques approvés/annulés
        loadAppointmentStats();

        // 2. Répartition par sexe
        loadGenderStats();

        // 3. Répartition par âge
        loadAgeStats();

        // 4. Répartition par jour
        loadDayStats();
    }

    private void loadAppointmentStats() {
        // Requête pour les stats des rendez-vous
        String query = "SELECT "
                + "SUM(CASE WHEN status = 'approved' THEN 1 ELSE 0 END) as approved, "
                + "SUM(CASE WHEN status = 'canceled' THEN 1 ELSE 0 END) as canceled, "
                + "COUNT(*) as total "
                + "FROM rendez_vous WHERE medecin_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, doctorId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int approved = rs.getInt("approved");
                int canceled = rs.getInt("canceled");
                int total = rs.getInt("total");

                if (total > 0) {
                    double approvedPercent = (approved * 100.0) / total;
                    double canceledPercent = (canceled * 100.0) / total;

                    approvedLabel.setText(String.format("- Rendez-vous approuvés : %.2f%%", approvedPercent));
                    canceledLabel.setText(String.format("- Rendez-vous annulés : %.2f%%", canceledPercent));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadGenderStats() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        String query = "SELECT p.sexe, COUNT(*) as count "
                + "FROM rendez_vous r JOIN user p ON r.patient_id = p.telephone "
                + "WHERE r.medecin_id = ? GROUP BY p.sexe";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, doctorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String sexe = rs.getString("sexe").equals("homme") ? "Homme" : "Femme";
                int count = rs.getInt("count");
                pieChartData.add(new PieChart.Data(sexe, count));
            }

            genderChart.setData(pieChartData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAgeStats() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        String query = "SELECT "
                + "SUM(CASE WHEN p.age BETWEEN 0 AND 18 THEN 1 ELSE 0 END) as age0_18, "
                + "SUM(CASE WHEN p.age BETWEEN 19 AND 35 THEN 1 ELSE 0 END) as age19_35, "
                + "SUM(CASE WHEN p.age BETWEEN 36 AND 50 THEN 1 ELSE 0 END) as age36_50, "
                + "SUM(CASE WHEN p.age BETWEEN 51 AND 70 THEN 1 ELSE 0 END) as age51_70, "
                + "SUM(CASE WHEN p.age > 70 THEN 1 ELSE 0 END) as age71plus, "
                + "COUNT(*) as total "
                + "FROM rendez_vous r JOIN user p ON r.patient_id = p.telephone "
                + "WHERE r.medecin_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, doctorId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total");
                if (total > 0) {
                    addAgeData(series, "0-18", rs.getInt("age0_18"), total);
                    addAgeData(series, "19-35", rs.getInt("age19_35"), total);
                    addAgeData(series, "36-50", rs.getInt("age36_50"), total);
                    addAgeData(series, "51-70", rs.getInt("age51_70"), total);
                    addAgeData(series, "71+", rs.getInt("age71plus"), total);
                }
            }

            ageChart.getData().add(series);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addAgeData(XYChart.Series<String, Number> series, String range, int count, int total) {
        double percent = (count * 100.0) / total;
        series.getData().add(new XYChart.Data<>(range, percent));
    }

    private void loadDayStats() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        String query = "SELECT DAYNAME(date_rdv) as day, COUNT(*) as count "
                + "FROM rendez_vous WHERE medecin_id = ? "
                + "GROUP BY DAYNAME(date_rdv) "
                + "ORDER BY DAYOFWEEK(date_rdv)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, doctorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String day = rs.getString("day");
                int count = rs.getInt("count");
                series.getData().add(new XYChart.Data<>(day.substring(0, 3), count));
            }

            dayChart.getData().add(series);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}