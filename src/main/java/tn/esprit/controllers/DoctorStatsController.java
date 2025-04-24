package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DoctorStatsController {
    // Composants FXML
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
    private Map<String, String> medecinsMap = new HashMap<>(); // telephone -> nom complet

    public void initialize(Connection connection) {
        this.connection = connection;
        loadSpecialites();
        setupListeners();
    }

    private void loadSpecialites() {
        try {
            String query = "SELECT DISTINCT specialite FROM user WHERE role = 'MEDECIN' AND specialite IS NOT NULL";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            ObservableList<String> specialites = FXCollections.observableArrayList();
            while (rs.next()) {
                specialites.add(rs.getString("specialite"));
            }

            specialiteCombo.setItems(specialites);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        // Quand une spécialité est sélectionnée
        specialiteCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadMedecinsBySpecialite(newVal);
                medecinCombo.setDisable(false);
            } else {
                medecinCombo.setDisable(true);
                medecinCombo.getSelectionModel().clearSelection();
            }
        });

        // Quand un médecin est sélectionné
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
            String query = "SELECT telephone, prenom, nom FROM user WHERE role = 'MEDECIN' AND specialite = ?";
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showStatsForDoctor(String doctorPhone, String specialite) {
        // Afficher les infos de base
        statsSection.setVisible(true);
        statsDetails.setVisible(true);
        specialiteLabel.setText("- " + specialite);

        // Charger les données du médecin
        loadDoctorName(doctorPhone);

        // Charger les statistiques
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
            e.printStackTrace();
        }
    }

    private void loadAppointmentStats(String doctorPhone) {
        // Même implémentation que précédemment
        // ...
    }

    private void loadGenderStats(String doctorPhone) {
        // Même implémentation que précédemment
        // ...
    }

    private void loadAgeStats(String doctorPhone) {
        // Même implémentation que précédemment
        // ...
    }

    private void loadDayStats(String doctorPhone) {
        // Même implémentation que précédemment
        // ...
    }
}