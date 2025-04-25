package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.models.Evenement;
import tn.esprit.services.ServiceEvenement;

import java.time.LocalDate;
import java.util.List;

public class EvenementController {

    @FXML
    private TextField nomField;
    @FXML
    private TextArea contenueField;
    @FXML
    private TextField typeField;
    @FXML
    private TextField statutField;
    @FXML
    private TextField lieuxField;
    @FXML
    private DatePicker dateField;
    @FXML
    private Label errorLabel;
    @FXML
    private TableView<Evenement> evenementTable;
    @FXML
    private TableColumn<Evenement, Integer> idColumn;
    @FXML
    private TableColumn<Evenement, String> nomColumn;
    @FXML
    private TableColumn<Evenement, String> contenueColumn;
    @FXML
    private TableColumn<Evenement, String> typeColumn;
    @FXML
    private TableColumn<Evenement, String> statutColumn;
    @FXML
    private TableColumn<Evenement, String> lieuxColumn;
    @FXML
    private TableColumn<Evenement, LocalDate> dateColumn;

    private ServiceEvenement serviceEvenement;
    private Evenement selectedEvenement;

    @FXML
    public void initialize() {
        serviceEvenement = new ServiceEvenement();

        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        contenueColumn.setCellValueFactory(new PropertyValueFactory<>("contenue"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        lieuxColumn.setCellValueFactory(new PropertyValueFactory<>("lieuxEvent"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateEvent"));

        // Load events into the table
        loadEvenements();

        // Add listener to table selection
        evenementTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedEvenement = newSelection;
                nomField.setText(selectedEvenement.getNom());
                contenueField.setText(selectedEvenement.getContenue());
                typeField.setText(selectedEvenement.getType());
                statutField.setText(selectedEvenement.getStatut());
                lieuxField.setText(selectedEvenement.getLieuxEvent());
                dateField.setValue(selectedEvenement.getDateEvent());
            }
        });
    }

    private void loadEvenements() {
        List<Evenement> evenements = serviceEvenement.getAll();
        evenementTable.getItems().clear();
        evenementTable.getItems().addAll(evenements);
    }

    @FXML
    private void addEvenement() {
        if (!validateInputs()) {
            return;
        }

        Evenement evenement = new Evenement();
        evenement.setNom(nomField.getText());
        evenement.setContenue(contenueField.getText());
        evenement.setType(typeField.getText());
        evenement.setStatut(statutField.getText());
        evenement.setLieuxEvent(lieuxField.getText());
        evenement.setDateEvent(dateField.getValue());

        serviceEvenement.add(evenement);
        loadEvenements();
        clearFields();
        showSuccess("Événement ajouté avec succès !");
    }

    @FXML
    private void updateEvenement() {
        if (selectedEvenement == null) {
            showError("Veuillez sélectionner un événement à modifier.");
            return;
        }

        if (!validateInputs()) {
            return;
        }

        selectedEvenement.setNom(nomField.getText());
        selectedEvenement.setContenue(contenueField.getText());
        selectedEvenement.setType(typeField.getText());
        selectedEvenement.setStatut(statutField.getText());
        selectedEvenement.setLieuxEvent(lieuxField.getText());
        selectedEvenement.setDateEvent(dateField.getValue());

        serviceEvenement.update(selectedEvenement);
        loadEvenements();
        clearFields();
        showSuccess("Événement modifié avec succès !");
    }

    @FXML
    private void deleteEvenement() {
        if (selectedEvenement == null) {
            showError("Veuillez sélectionner un événement à supprimer.");
            return;
        }

        serviceEvenement.delete(selectedEvenement);
        loadEvenements();
        clearFields();
        showSuccess("Événement supprimé avec succès !");
    }

    @FXML
    private void clearFields() {
        nomField.clear();
        contenueField.clear();
        typeField.clear();
        statutField.clear();
        lieuxField.clear();
        dateField.setValue(null);
        selectedEvenement = null;
        errorLabel.setText("");
    }

    private boolean validateInputs() {
        if (nomField.getText().isEmpty()) {
            showError("Le nom ne peut pas être vide.");
            return false;
        }
        if (contenueField.getText().isEmpty()) {
            showError("Le contenu ne peut pas être vide.");
            return false;
        }
        if (typeField.getText().isEmpty()) {
            showError("Le type ne peut pas être vide.");
            return false;
        }
        if (statutField.getText().isEmpty()) {
            showError("Le statut ne peut pas être vide.");
            return false;
        }
        if (lieuxField.getText().isEmpty()) {
            showError("Le lieu ne peut pas être vide.");
            return false;
        }
        if (dateField.getValue() == null) {
            showError("La date ne peut pas être vide.");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: green;");
    }
}