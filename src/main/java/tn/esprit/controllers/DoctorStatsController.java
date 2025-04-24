package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DoctorStatsController {
    @FXML private ComboBox<String> specialiteCombo;
    @FXML private ComboBox<String> medecinCombo;
    @FXML private VBox statsSection;
    @FXML private VBox statsDetails;
    @FXML private Label specialiteLabel;
    @FXML private Label nomMedecinLabel;
    @FXML private Label statsTitleLabel;
    @FXML private Label approvedLabel;
    @FXML private Label canceledLabel;
    @FXML private PieChart genderChart;
    @FXML private BarChart<String, Number> ageChart;
    @FXML private BarChart<String, Number> dayChart;

    private Connection connection;
    private Map<String, String> medecinsMap = new HashMap<>();

    @FXML
    public void initialize() {
        this.connection = MyDataBase.getInstance().getCnx();

        if (connection != null) {
            loadSpecialites();
            setupListeners();
        } else {
            showAlert("Erreur de connexion", "Impossible de se connecter à la base de données");
        }
    }

    private void loadSpecialites() {
        try {
            String query = "SELECT DISTINCT specialite FROM user WHERE roles LIKE '%MEDECIN%' AND specialite IS NOT NULL AND specialite <> ''";
            ;
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            ObservableList<String> specialites = FXCollections.observableArrayList();
            while (rs.next()) {
                specialites.add(rs.getString("specialite"));
            }

            specialiteCombo.setItems(specialites);

            if (specialites.isEmpty()) {
                showAlert("Aucune spécialité", "Aucune spécialité trouvée dans la base de données");
            }
        } catch (SQLException e) {
            showAlert("Erreur SQL", "Erreur lors du chargement des spécialités: " + e.getMessage());
        }
    }

    private void setupListeners() {
        specialiteCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadMedecinsBySpecialite(newVal);
                medecinCombo.setDisable(false);
            } else {
                medecinCombo.setDisable(true);
                medecinCombo.getSelectionModel().clearSelection();
            }
        });

        medecinCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String doctorPhone = medecinsMap.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(newVal))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);

                if (doctorPhone != null) {
                    showStatsForDoctor(doctorPhone, specialiteCombo.getValue());
                }
            } else {
                statsSection.setVisible(false);
                statsDetails.setVisible(false);
            }
        });
    }

    private void loadMedecinsBySpecialite(String specialite) {
        try {
            String query = "SELECT telephone, prenom, nom FROM user WHERE roles LIKE '%MEDECIN%' AND specialite = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, specialite);
            ResultSet rs = stmt.executeQuery();

            ObservableList<String> medecins = FXCollections.observableArrayList();
            medecinsMap.clear();

            while (rs.next()) {
                String phone = rs.getString("telephone");
                String nomComplet = rs.getString("prenom") + " " + rs.getString("nom");
                medecins.add(nomComplet);
                medecinsMap.put(phone, nomComplet);
            }

            medecinCombo.setItems(medecins);
            medecinCombo.getSelectionModel().clearSelection();

            if (medecins.isEmpty()) {
                showAlert("Aucun médecin", "Aucun médecin trouvé pour la spécialité: " + specialite);
            }
        } catch (SQLException e) {
            showAlert("Erreur SQL", "Erreur lors du chargement des médecins: " + e.getMessage());
        }
    }

    private void showStatsForDoctor(String doctorPhone, String specialite) {
        statsSection.setVisible(true);
        statsDetails.setVisible(true);
        specialiteLabel.setText("- " + specialite);
        loadDoctorName(doctorPhone);
        loadAppointmentStats(doctorPhone);
        loadGenderStats(doctorPhone);
        loadAgeStats(doctorPhone);
        loadDayStats(doctorPhone);
    }

    private void loadDoctorName(String doctorPhone) {
        try {
            String query = "SELECT prenom, nom FROM user WHERE telephone = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, doctorPhone);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String nomComplet = rs.getString("prenom") + " " + rs.getString("nom");
                nomMedecinLabel.setText("  - " + nomComplet);
                statsTitleLabel.setText("Statistiques du médecin " + nomComplet);
            }
        } catch (SQLException e) {
            showAlert("Erreur SQL", "Erreur lors du chargement du médecin: " + e.getMessage());
        }
    }

    private void loadAppointmentStats(String doctorPhone) {
        try {
            // Requête SQL corrigée avec des virgules manquantes
            String query = "SELECT "
                    + "SUM(CASE WHEN statut = 'Approuvé' THEN 1 ELSE 0 END) AS approved, "
                    + "SUM(CASE WHEN statut = 'Refusé' THEN 1 ELSE 0 END) AS canceled, "
                    + "COUNT(*) AS total "
                    + "FROM rendez_vous WHERE medecin_id = ?";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, doctorPhone);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total");
                if (total > 0) {
                    int approved = rs.getInt("approved");
                    int canceled = rs.getInt("canceled");

                    // Calcul des pourcentages
                    double approvedPercent = (approved * 100.0) / total;
                    double canceledPercent = (canceled * 100.0) / total;

                    // Mise à jour des labels avec les pourcentages calculés
                    approvedLabel.setText(String.format("- Rendez-vous approuvés : %.2f%%", approvedPercent));
                    canceledLabel.setText(String.format("- Rendez-vous annulés : %.2f%%", canceledPercent));
                } else {
                    // Si aucun rendez-vous n'est trouvé
                    approvedLabel.setText("- Rendez-vous approuvés : 0%");
                    canceledLabel.setText("- Rendez-vous annulés : 0%");
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur SQL", "Erreur lors du chargement des stats de RDV: " + e.getMessage());
        }
    }

    private void loadGenderStats(String doctorPhone) {
        try {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            String query = "SELECT p.sexe, COUNT(*) as count "
                    + "FROM rendez_vous r JOIN user p ON r.patient_id = p.telephone "
                    + "WHERE r.medecin_id = ? GROUP BY p.sexe";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, doctorPhone);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String sexe = "homme".equalsIgnoreCase(rs.getString("sexe")) ? "Hommes" : "Femmes";
                pieData.add(new PieChart.Data(sexe, rs.getInt("count")));
            }

            genderChart.setData(pieData);
        } catch (SQLException e) {
            showAlert("Erreur SQL", "Erreur lors du chargement des stats par sexe: " + e.getMessage());
        }
    }

    private void loadAgeStats(String doctorPhone) {
        try {
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

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, doctorPhone);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total");
                if (total > 0) {
                    series.getData().add(new XYChart.Data<>("0-18", (rs.getInt("age0_18") * 100.0 / total)));
                    series.getData().add(new XYChart.Data<>("19-35", (rs.getInt("age19_35") * 100.0 / total)));
                    series.getData().add(new XYChart.Data<>("36-50", (rs.getInt("age36_50") * 100.0 / total)));
                    series.getData().add(new XYChart.Data<>("51-70", (rs.getInt("age51_70") * 100.0 / total)));
                    series.getData().add(new XYChart.Data<>("71+", (rs.getInt("age71plus") * 100.0 / total)));
                }
            }

            ageChart.getData().clear();
            ageChart.getData().add(series);
        } catch (SQLException e) {
            showAlert("Erreur SQL", "Erreur lors du chargement des stats par âge: " + e.getMessage());
        }
    }

    private void loadDayStats(String doctorPhone) {
        try {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            String query = "SELECT DAYNAME(date) as day, COUNT(*) as count "
                    + "FROM rendez_vous WHERE medecin_id = ? "
                    + "GROUP BY DAYNAME(date_rdv) "
                    + "ORDER BY DAYOFWEEK(date_rdv)";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, doctorPhone);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String day = rs.getString("day");
                series.getData().add(new XYChart.Data<>(day.substring(0, 3), rs.getInt("count")));
            }

            dayChart.getData().clear();
            dayChart.getData().add(series);
        } catch (SQLException e) {
            showAlert("Erreur SQL", "Erreur lors du chargement des stats par jour: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}