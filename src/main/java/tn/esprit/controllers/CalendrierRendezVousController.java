package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import tn.esprit.models.RendeVous;
import tn.esprit.services.ServiceConsultation;
import tn.esprit.services.ServiceAddRdv;

import java.time.LocalDate;
import java.util.HashMap;
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
        // Exemples : noms + id associés
        medecinMap.put("Dr. Aissaoui", 1);
        medecinMap.put("Dr. Ben Amor", 2);

        medecinComboBox.getItems().addAll(medecinMap.keySet());
        typeComboBox.getItems().addAll("CONSULTATION", "URGENCE", "SUIVI");

        medecinComboBox.setOnAction(e -> {
            String nom = medecinComboBox.getValue();
            if (nom != null && medecinMap.containsKey(nom)) {
                int idMedecin = medecinMap.get(nom);
                setupCalendar(idMedecin);
            }
        });
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

        int currentUserId = getCurrentPatientId(); // à adapter
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
        return 1; // à remplacer par ton système d’authentification
    }
}
