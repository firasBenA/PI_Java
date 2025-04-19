package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.models.Diagnostique;
import tn.esprit.models.User;
import tn.esprit.services.ServiceUser;

import javax.swing.text.html.ImageView;
import java.io.IOException;

public class DiagnostiqueController {


    @FXML
    private Label dateDiagnostique;

    @FXML
    private Label nomDiagnostique;

    @FXML
    private Label selectedSymptomes;

    @FXML
    private ImageView userImg;

    @FXML
    private Label nom;

    @FXML
    private Label prenom;
    @FXML
    private Label zoneCorps;

    @FXML
    private Button addBtn;
    private Diagnostique diagnostiquePassedToThisCard;

    public void setData(Diagnostique diagnostique){
        this.diagnostiquePassedToThisCard = diagnostique;

        ServiceUser patientService = new ServiceUser();
        User patient = patientService.getUserById(diagnostique.getPatientId());

        selectedSymptomes.setText(diagnostique.getSelectedSymptoms());
        zoneCorps.setText(diagnostique.getZoneCorps());
        nomDiagnostique.setText(diagnostique.getNom());
        dateDiagnostique.setText(diagnostique.getDateDiagnostique().toString());
        nom.setText(patient.getNom());
        prenom.setText(patient.getPrenom());
    }




    private void showAddPrescriptionForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterPrescriptionDepuisDiag.fxml"));
            Parent root = loader.load();

            // Get the controller of AjouterPrescriptionDepuisDiag
            AjouterPrescriptionDepuisDiag controller = loader.getController();

            // Pass the diagnostique object to the form controller
            controller.setDiagnostique(this.diagnostiquePassedToThisCard);

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Ordonnance");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void handleAddPrescription(javafx.event.ActionEvent actionEvent) {
        System.out.println("Add Prescription button clicked");

        showAddPrescriptionForm();
    }
}
