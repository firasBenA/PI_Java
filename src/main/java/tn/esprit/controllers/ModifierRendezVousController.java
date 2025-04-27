package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.RendeVous;
import tn.esprit.services.ServiceAddRdv;

import java.time.LocalDate;

public class ModifierRendezVousController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextArea causeTextArea;
    @FXML private Button btnValider;
    @FXML private Button btnAnnuler;

    // Ajoutez ces annotations pour les labels d'erreur
    @FXML private Label dateError;
    @FXML private Label typeError;
    @FXML private Label causeError;

    private RendeVous rendezVous;
    private final ServiceAddRdv serviceRdv = new ServiceAddRdv();
    private boolean modificationValidee = false;

    @FXML
    public void initialize() {
        // Initialiser les types de rendez-vous disponibles
        typeComboBox.getItems().addAll(
                "Consultation",
                "Urgence",
                "Suivi"
        );

        // Effacer les messages d'erreur lorsqu'on modifie les champs
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> dateError.setText(""));
        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> typeError.setText(""));
        causeTextArea.textProperty().addListener((obs, oldVal, newVal) -> causeError.setText(""));
    }

    public void setRendezVous(RendeVous rdv) {
        this.rendezVous = rdv;
        chargerDonneesRendezVous();
    }

    private void chargerDonneesRendezVous() {
        if (rendezVous != null) {
            datePicker.setValue(rendezVous.getDate());
            typeComboBox.setValue(rendezVous.getType());
            causeTextArea.setText(rendezVous.getCause());
        }
    }

    @FXML
    private void handleValider() {
        if (validerFormulaire()) {
            mettreAJourRendezVous();
            modificationValidee = true;
            fermerFenetre();
        }
    }

    private boolean validerFormulaire() {
        boolean isValid = true;

        // Réinitialiser les messages d'erreur
        dateError.setText("");
        typeError.setText("");
        causeError.setText("");

        // Validation de la date
        if (datePicker.getValue() == null) {
            dateError.setText("La date est obligatoire");
            isValid = false;
        } else if (datePicker.getValue().isBefore(LocalDate.now())) {
            dateError.setText("La date ne peut pas être dans le passé");
            isValid = false;
        }

        // Validation du type
        if (typeComboBox.getValue() == null || typeComboBox.getValue().isEmpty()) {
            typeError.setText("Le type est obligatoire");
            isValid = false;
        }

        // Validation de la cause
        if (causeTextArea.getText() == null || causeTextArea.getText().trim().isEmpty()) {
            causeError.setText("La cause est obligatoire");
            isValid = false;
        }

        return isValid;
    }

    private void mettreAJourRendezVous() {
        rendezVous.setDate(datePicker.getValue());
        rendezVous.setType(typeComboBox.getValue());
        rendezVous.setCause(causeTextArea.getText());

        serviceRdv.update(rendezVous);
    }

    @FXML
    private void handleAnnuler() {
        modificationValidee = false;
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) btnValider.getScene().getWindow();
        stage.close();
    }

    public boolean isModificationValidee() {
        return modificationValidee;
    }
}