package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Evenement;
import tn.esprit.services.ServiceEvenement;

import java.io.IOException;
import java.time.LocalDate;

public class EvenementController {

    @FXML
    private TextField nomField, typeField, statutField, lieuxField;
    @FXML
    private TextArea contenueField;
    @FXML
    private DatePicker dateField;
    @FXML
    private TableView<Evenement> evenementTable;

    private ServiceEvenement serviceEvenement = new ServiceEvenement();
    private ObservableList<Evenement> evenementList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Load evenements from the database
        try {
            evenementList.clear(); // Clear any existing data
            evenementList.addAll(serviceEvenement.getAll()); // Fetch data from the database
            evenementTable.setItems(evenementList); // Set the data in the TableView
            System.out.println("Loaded " + evenementList.size() + " evenements from the database.");
        } catch (Exception e) {
            showAlert("Error", "Failed to load evenements: " + e.getMessage());
        }

        // Select an evenement to populate fields
        evenementTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nomField.setText(newSelection.getNom());
                contenueField.setText(newSelection.getContenue());
                typeField.setText(newSelection.getType());
                statutField.setText(newSelection.getStatut());
                lieuxField.setText(newSelection.getLieuxEvent());
                dateField.setValue(newSelection.getDateEvent());
            }
        });
    }

    @FXML
    public void addEvenement() {
        // Input validation
        if (!validateEvenementInputs()) {
            return;
        }

        Evenement evenement = new Evenement();
        evenement.setNom(nomField.getText());
        evenement.setContenue(contenueField.getText());
        evenement.setType(typeField.getText());
        evenement.setStatut(statutField.getText());
        evenement.setLieuxEvent(lieuxField.getText());
        evenement.setDateEvent(dateField.getValue());

        try {
            serviceEvenement.add(evenement);
            evenementList.clear();
            evenementList.addAll(serviceEvenement.getAll());
            clearFields();
            showAlert("Success", "Evenement added successfully!");
        } catch (Exception e) {
            showAlert("Error", "Failed to add evenement: " + e.getMessage());
        }
    }

    @FXML
    public void updateEvenement() {
        Evenement selectedEvenement = evenementTable.getSelectionModel().getSelectedItem();
        if (selectedEvenement == null) {
            showAlert("Error", "Please select an evenement to update.");
            return;
        }

        // Input validation
        if (!validateEvenementInputs()) {
            return;
        }

        selectedEvenement.setNom(nomField.getText());
        selectedEvenement.setContenue(contenueField.getText());
        selectedEvenement.setType(typeField.getText());
        selectedEvenement.setStatut(statutField.getText());
        selectedEvenement.setLieuxEvent(lieuxField.getText());
        selectedEvenement.setDateEvent(dateField.getValue());

        try {
            serviceEvenement.update(selectedEvenement);
            evenementList.clear();
            evenementList.addAll(serviceEvenement.getAll());
            clearFields();
            showAlert("Success", "Evenement updated successfully!");
        } catch (Exception e) {
            showAlert("Error", "Failed to update evenement: " + e.getMessage());
        }
    }

    @FXML
    public void deleteEvenement() {
        Evenement selectedEvenement = evenementTable.getSelectionModel().getSelectedItem();
        if (selectedEvenement == null) {
            showAlert("Error", "Please select an evenement to delete.");
            return;
        }

        try {
            serviceEvenement.delete(selectedEvenement);
            evenementList.remove(selectedEvenement);
            clearFields();
            showAlert("Success", "Evenement deleted successfully!");
        } catch (Exception e) {
            showAlert("Error", "Failed to delete evenement: " + e.getMessage());
        }
    }

    @FXML
    public void switchToArticle() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/ArticleController.fxml"));
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    private boolean validateEvenementInputs() {
        if (nomField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Nom cannot be empty.");
            return false;
        }
        if (contenueField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Contenue cannot be empty.");
            return false;
        }
        if (typeField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Type cannot be empty.");
            return false;
        }
        if (statutField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Statut cannot be empty.");
            return false;
        }
        if (lieuxField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Lieux Event cannot be empty.");
            return false;
        }
        if (dateField.getValue() == null) {
            showAlert("Validation Error", "Date Event cannot be empty.");
            return false;
        }
        if (dateField.getValue().isBefore(LocalDate.now())) {
            showAlert("Validation Error", "Date Event cannot be in the past.");
            return false;
        }
        return true;
    }

    private void clearFields() {
        nomField.clear();
        contenueField.clear();
        typeField.clear();
        statutField.clear();
        lieuxField.clear();
        dateField.setValue(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}