package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.models.Prescription;
import tn.esprit.models.User;
import tn.esprit.services.ServicePrescription;
import tn.esprit.services.ServiceUser;

import java.io.IOException;

public class PrescriptionController {


    @FXML
    private Label contenue;

    @FXML
    private Label datePrescription;

    @FXML
    private Label nom;

    @FXML
    private Label numTel;

    @FXML
    private Label prenom;

    @FXML
    private Label titre;

    @FXML
    private Button deleteBtn;

    @FXML
    private Button modifyBtn;

    private Prescription currentPrescription;




    public void setData(Prescription prescription){

        this.currentPrescription = prescription;

        ServiceUser patientService = new ServiceUser();
        User patient = patientService.getUserById(prescription.getPatient_id());

        titre.setText(prescription.getTitre());
        contenue.setText(prescription.getContenue());
        numTel.setText(patient.getTelephone());
        datePrescription.setText(prescription.getDate_prescription().toString());
        nom.setText(patient.getNom());
        prenom.setText(patient.getPrenom());
    }

    @FXML
    private void onDeleteClicked() {
        ServicePrescription service = new ServicePrescription();
        boolean result = service.deletePrescriptionById(currentPrescription.getId());

        if (result) {
            System.out.println("Prescription supprimée !");
            deleteBtn.getScene().getWindow().hide();
        } else {
            System.out.println("Erreur lors de la suppression.");
        }
    }

    @FXML
    private void onModifyClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierPrescription.fxml"));
            Parent root = loader.load();

            ModifierPrescriptionController controller = loader.getController();
            controller.setPrescription(currentPrescription);

            Stage stage = new Stage();
            stage.setTitle("Modifier Prescription");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
