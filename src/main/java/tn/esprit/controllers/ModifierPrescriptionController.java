package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Prescription;
import tn.esprit.services.ServicePrescription;

import java.sql.Date;

public class ModifierPrescriptionController {

    @FXML private TextField titreField;
    @FXML private TextArea contenueField;
    @FXML private DatePicker datePicker;
    @FXML private Label errorLabel;

    private Prescription prescription;
    private final ServicePrescription servicePrescription = new ServicePrescription();

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;

        // Populate the fields with existing data
        titreField.setText(prescription.getTitre());
        contenueField.setText(prescription.getContenue());
        if (prescription.getDate_prescription() != null) {
            datePicker.setValue(prescription.getDate_prescription().toLocalDate());
        }
    }

    @FXML
    public void modifierPrescription() {
        String titre = titreField.getText();
        String contenue = contenueField.getText();
        Date date = (datePicker.getValue() != null) ? Date.valueOf(datePicker.getValue()) : null;

        if (titre.isEmpty() || contenue.isEmpty() || date == null) {
            errorLabel.setText("Veuillez remplir tous les champs !");
            return;
        }

        // Update the prescription object
        prescription.setTitre(titre);
        prescription.setContenue(contenue);
        prescription.setDate_prescription(date);

        // Call your existing service method
        servicePrescription.update(prescription); // No need to check return value

        // Close the modification window
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();
    }

}
