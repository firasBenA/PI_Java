package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.models.Diagnostique;
import tn.esprit.models.Prescription;
import tn.esprit.services.ServicePrescription;

import java.sql.*;
import java.time.LocalDate;


public class AjouterPrescriptionDepuisDiag {

    @FXML
    private TextField titreField;

    @FXML
    private TextField contenueField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Label errorLabel;

    @FXML
    private Label diagnostiqueIdLabel;

    private Diagnostique diagnostique;

    private final ServicePrescription servicePrescription = new ServicePrescription();

    public void setDiagnostique(Diagnostique diagnostique) {
        this.diagnostique = diagnostique;
        diagnostiqueIdLabel.setText("Diagnostique ID: " + diagnostique.getId());
    }


    public void ajouterPrescription(ActionEvent event) {
        String titre = titreField.getText();
        String contenue = contenueField.getText();
        Date date = (datePicker.getValue() != null) ? Date.valueOf(datePicker.getValue()) : null;

        if (titre == null || titre.isEmpty()) {
            errorLabel.setText("Veuillez remplir le champ du titre !");
            return;
        }
        if (contenue == null || contenue.isEmpty()) {
            errorLabel.setText("Veuillez remplir le champ du contenu !");
            return;
        }
        if (date == null) {
            errorLabel.setText("Veuillez choisir une date !");
            return;
        }
        if (date.before(Date.valueOf(LocalDate.now()))) {
            errorLabel.setText("La date ne peut pas être dans le passé !");
            return;
        }


        // Check if the Diagnostique object is valid
        if (diagnostique == null || diagnostique.getId() <= 0) {
            errorLabel.setText("Diagnostique is invalid or not found!");
            return;
        }

        System.out.println("Diagnostique ID: " + diagnostique.getId());  // Debugging output

        // Create a new Prescription
        Prescription p = new Prescription();
        p.setTitre(titre);
        p.setContenue(contenue);
        p.setDate_prescription(date);

        // Set the diagnostique_id to the corresponding Diagnostique ID
        p.setDiagnostique_id(diagnostique.getId());
        p.setPatient_id(diagnostique.getPatientId());
        p.setDossier_medical_id(diagnostique.getDossierMedicalId());

        p.setMedecin_id(3);  // Replace with the actual doctor's ID if necessary

        // Create a connection (assuming you have a valid connection instance)
        Connection connection = null;
        try {
            // Assuming you have a DataSource or DriverManager to get the connection
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ehealth_database", "root", "");

            // Start a transaction (disable auto-commit)
            connection.setAutoCommit(false);

            // Log the prescription data before saving
            System.out.println("Prescription data: Diagnostique ID: " + diagnostique.getId() + ", Patient ID: " + diagnostique.getPatientId());

            // Add the prescription to the database (use prepared statements or your own logic here)
            String sql = "INSERT INTO prescription (diagnostique_id, titre, contenue, date_prescription, medecin_id, patient_id, dossier_medical_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, p.getDiagnostique_id());
                stmt.setString(2, p.getTitre());
                stmt.setString(3, p.getContenue());
                stmt.setDate(4, p.getDate_prescription());
                stmt.setInt(5, p.getMedecin_id());
                stmt.setInt(6, p.getPatient_id());
                stmt.setInt(7, p.getDossier_medical_id());

                stmt.executeUpdate();
                System.out.println("Prescription inserted.");

                // Commit the transaction
                connection.commit();
                System.out.println("Transaction committed successfully!");

                // After committing the prescription, update the status of the diagnostique
                String updateDiagnostiqueStatusSql = "UPDATE diagnostique SET status = 1 WHERE id = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateDiagnostiqueStatusSql)) {
                    updateStmt.setInt(1, diagnostique.getId());
                    int affectedRows = updateStmt.executeUpdate();
                    System.out.println("Rows affected by diagnostique status update: " + affectedRows);

                    if (affectedRows > 0) {
                        System.out.println("Diagnostique status updated to 1.");
                    } else {
                        System.out.println("No rows were updated. Diagnostique ID may not exist or the status was already set to 1.");
                    }

                    // Commit the transaction after status update
                    connection.commit();
                } catch (SQLException ex) {
                    // Handle any error while updating the diagnostique status
                    System.out.println("Error updating diagnostique status: " + ex.getMessage());
                    connection.rollback();  // Rollback if an error occurs while updating the status
                    errorLabel.setText("Error occurred while updating the diagnostique status.");
                }


            } catch (SQLException ex) {
                // If an error occurs, rollback the transaction
                System.out.println("Error inserting prescription: " + ex.getMessage());
                connection.rollback();  // Rollback if an error occurs
                errorLabel.setText("Error occurred while saving the prescription.");
            }

            // Close the connection
            connection.close();

            // Close the window after saving
            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.close();
        } catch (SQLException ex) {
            // Handle any exception related to database connection
            System.out.println("Database connection error: " + ex.getMessage());
            errorLabel.setText("Database connection error.");
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ex) {
                System.out.println("Error closing connection: " + ex.getMessage());
            }
        }
    }

}