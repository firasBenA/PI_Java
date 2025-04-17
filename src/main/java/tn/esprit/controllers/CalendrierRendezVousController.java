package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import tn.esprit.models.RendeVous;
import tn.esprit.services.ServiceAddRdv;
import tn.esprit.services.ServiceConsultation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendrierRendezVousController {

    @FXML private ComboBox<String> medecinComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField causeTextField;
    @FXML private Button validerButton;

    private final ServiceConsultation serviceConsultation = new ServiceConsultation();
    private final ServiceAddRdv serviceRdv = new ServiceAddRdv();

    private final Map<String, Integer> medecinMap = new HashMap<>();

    @FXML
    public void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList("Consultation", "Suivi", "Urgence"));
        loadMedecins();

        medecinComboBox.setOnAction(e -> {
            String selected = medecinComboBox.getValue();
            if (selected != null) {
                Integer medecinId = medecinMap.get(selected);
                if (medecinId != null) {
                    setupCalendar(medecinId);
                }
            }
        });
    }

    private void loadMedecins() {
        ObservableList<String> medecinsList = FXCollections.observableArrayList();
        String query = "SELECT nom, prenom FROM user WHERE roles LIKE '%MEDECIN%'";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String nomComplet = rs.getString("prenom") + " " + rs.getString("nom");
                medecinsList.add(nomComplet);
            }
            medecin.setItems(medecinsList);

        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des médecins : " + e.getMessage());
            showAlert("Erreur", "Impossible de charger la liste des médecins");
        }
    }

    private void setupCalendar(int medecinId) {
        datePicker.setValue(null);
        datePicker.setDayCellFactory(new Callback<>() {
            @Override
            public DateCell call(DatePicker picker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        if (empty || date.isBefore(LocalDate.now())) {
                            setDisable(true);
                            return;
                        }

                        int count = serviceConsultation.getConsultationCountForDate(medecinId, date);
                        if (count >= 3) {
                            setStyle("-fx-background-color: #ff4d4d;");
                            setDisable(true);
                        } else if (count == 2) {
                            setStyle("-fx-background-color: #ffa500;");
                        } else {
                            setStyle("-fx-background-color: #90ee90;");
                        }
                    }
                };
            }
        });
    }

    @FXML
    private void validerRendezVous() {
        String medecinNom = medecinComboBox.getValue();
        LocalDate date = datePicker.getValue();
        String type = typeComboBox.getValue();
        String cause = causeTextField.getText();

        if (medecinNom == null || date == null || type == null || cause == null || cause.isEmpty()) {
            showAlert("Veuillez remplir tous les champs.");
            return;
        }

        int idMedecin = medecinMap.get(medecinNom);

        RendeVous rv = new RendeVous();
        rv.setIdMedecin(idMedecin);
        rv.setDate(date);
        rv.setType(type);
        rv.setCause(cause);
        rv.setStatut("en_attente");

        int currentUserId = getCurrentPatientId(); // à adapter selon session
        rv.setIdPatient(currentUserId);

        serviceRdv.add(rv);
        showAlert("Rendez-vous ajouté avec succès !");
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private int getCurrentPatientId() {
        return 1; // à adapter à ton système d'authentification
    }
}
