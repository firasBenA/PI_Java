package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.models.Evenement;
import tn.esprit.services.ServiceEvenement;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class EvenementController {

    @FXML
    private TextField nomField, lieuxField;
    @FXML
    private TextArea contenueField;
    @FXML
    private ComboBox<String> typeComboBox, statutComboBox;
    @FXML
    private DatePicker dateField;
    @FXML
    private TableView<Evenement> evenementTable;
    @FXML
    private Label statusLabel;

    private ServiceEvenement serviceEvenement = new ServiceEvenement();
    private ObservableList<Evenement> evenementList = FXCollections.observableArrayList();

    // Define the options for type and statut as they are in the Symfony form
    private final List<String> typeOptions = Arrays.asList("Conférence", "Séminaire", "Workshop", "Formation");
    private final List<String> statutOptions = Arrays.asList("Planifié", "En cours", "Terminé");

    @FXML
    public void initialize() {

        typeComboBox.setItems(FXCollections.observableArrayList(typeOptions));
        statutComboBox.setItems(FXCollections.observableArrayList(statutOptions));

        // Set default values
        dateField.setValue(LocalDate.now());

        // Set CSS style for invalid inputs
        String errorStyle = "-fx-border-color: red; -fx-border-width: 2px;";
        String normalStyle = "";

        // Add listeners to validate input fields as user types
        nomField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                nomField.setStyle(errorStyle);
            } else {
                nomField.setStyle(normalStyle);
            }
        });

        contenueField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                contenueField.setStyle(errorStyle);
            } else {
                contenueField.setStyle(normalStyle);
            }
        });

        lieuxField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                lieuxField.setStyle(errorStyle);
            } else {
                lieuxField.setStyle(normalStyle);
            }
        });

        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                typeComboBox.setStyle(errorStyle);
            } else {
                typeComboBox.setStyle(normalStyle);
            }
        });

        statutComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                statutComboBox.setStyle(errorStyle);
            } else {
                statutComboBox.setStyle(normalStyle);
            }
        });

        dateField.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBefore(LocalDate.now())) {
                dateField.setStyle(errorStyle);
            } else {
                dateField.setStyle(normalStyle);
            }
        });

        loadEvenements();

        evenementTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nomField.setText(newSelection.getNom());
                contenueField.setText(newSelection.getContenue());

                String type = newSelection.getType();
                if (type != null) {
                    switch (type) {
                        case "conference": typeComboBox.setValue("Conférence"); break;
                        case "seminaire": typeComboBox.setValue("Séminaire"); break;
                        case "workshop": typeComboBox.setValue("Workshop"); break;
                        case "formation": typeComboBox.setValue("Formation"); break;
                        default: typeComboBox.setValue(type);
                    }
                } else {
                    typeComboBox.setValue(null);
                }

                String statut = newSelection.getStatut();
                if (statut != null) {
                    // Map the database value to the display value
                    switch (statut) {
                        case "planifie": statutComboBox.setValue("Planifié"); break;
                        case "en_cours": statutComboBox.setValue("En cours"); break;
                        case "termine": statutComboBox.setValue("Terminé"); break;
                        default: statutComboBox.setValue(statut);
                    }
                } else {
                    statutComboBox.setValue(null);
                }

                lieuxField.setText(newSelection.getLieuxEvent());
                dateField.setValue(newSelection.getDateEvent());
            }
        });
    }

    private void loadEvenements() {
        try {
            evenementList.clear(); // Clear any existing data
            evenementList.addAll(serviceEvenement.getAll()); // Fetch data from the database
            evenementTable.setItems(evenementList); // Set the data in the TableView
            System.out.println("Loaded " + evenementList.size() + " evenements from the database.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Echéc" + e.getMessage());
        }
    }

    @FXML
    public void addEvenement() {
        // Input validation
        if (!validateEvenementInputs()) {
            return;
        }

        Evenement evenement = new Evenement();
        evenement.setNom(nomField.getText().trim());
        evenement.setContenue(contenueField.getText().trim());

        // Convert display values to database values
        String typeValue = mapTypeToDatabase(typeComboBox.getValue());
        String statutValue = mapStatutToDatabase(statutComboBox.getValue());

        evenement.setType(typeValue);
        evenement.setStatut(statutValue);
        evenement.setLieuxEvent(lieuxField.getText().trim());
        evenement.setDateEvent(dateField.getValue());

        try {
            serviceEvenement.add(evenement);
            loadEvenements();
            clearFields();
            showStatusMessage("Événement ajouté avec succès!", true);
        } catch (Exception e) {
            showStatusMessage("Échec de l'ajout de l'événement: " + e.getMessage(), false);
        }
    }

    @FXML
    public void updateEvenement() {
        Evenement selectedEvenement = evenementTable.getSelectionModel().getSelectedItem();
        if (selectedEvenement == null) {
            showStatusMessage("Veuillez sélectionner un événement à modifier.", false);
            return;
        }

        // Input validation
        if (!validateEvenementInputs()) {
            return;
        }

        selectedEvenement.setNom(nomField.getText().trim());
        selectedEvenement.setContenue(contenueField.getText().trim());

        // Convert display values to database values
        String typeValue = mapTypeToDatabase(typeComboBox.getValue());
        String statutValue = mapStatutToDatabase(statutComboBox.getValue());

        selectedEvenement.setType(typeValue);
        selectedEvenement.setStatut(statutValue);
        selectedEvenement.setLieuxEvent(lieuxField.getText().trim());
        selectedEvenement.setDateEvent(dateField.getValue());

        try {
            serviceEvenement.update(selectedEvenement);
            loadEvenements();
            clearFields();
            showStatusMessage("Événement mis à jour avec succès!", true);
        } catch (Exception e) {
            showStatusMessage("Échec de la mise à jour de l'événement: " + e.getMessage(), false);
        }
    }

    @FXML
    public void deleteEvenement() {
        Evenement selectedEvenement = evenementTable.getSelectionModel().getSelectedItem();
        if (selectedEvenement == null) {
            showStatusMessage("Veuillez sélectionner un événement à supprimer.", false);
            return;
        }

        // Confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Supprimer l'événement");
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer l'événement \"" + selectedEvenement.getNom() + "\" ?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceEvenement.delete(selectedEvenement);
                evenementList.remove(selectedEvenement);
                clearFields();
                showStatusMessage("Événement supprimé avec succès!", true);
            } catch (Exception e) {
                showStatusMessage("Échec de la suppression de l'événement: " + e.getMessage(), false);
            }
        }
    }

    @FXML
    public void clearFields() {
        nomField.clear();
        contenueField.clear();
        typeComboBox.setValue(null);
        statutComboBox.setValue(null);
        lieuxField.clear();
        dateField.setValue(LocalDate.now());
        evenementTable.getSelectionModel().clearSelection();
    }

    @FXML
    public void switchToArticle() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/ArticleController.fxml"));
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    // Helper method to map display type value to database value
    private String mapTypeToDatabase(String displayValue) {
        if (displayValue == null) return null;

        switch (displayValue) {
            case "Conférence": return "conference";
            case "Séminaire": return "seminaire";
            case "Workshop": return "workshop";
            case "Formation": return "formation";
            default: return displayValue.toLowerCase();
        }
    }

    // Helper method to map display statut value to database value
    private String mapStatutToDatabase(String displayValue) {
        if (displayValue == null) return null;

        switch (displayValue) {
            case "Planifié": return "planifie";
            case "En cours": return "en_cours";
            case "Terminé": return "termine";
            default: return displayValue.toLowerCase().replace(" ", "_");
        }
    }

    private boolean validateEvenementInputs() {
        StringBuilder errorBuilder = new StringBuilder();
        boolean isValid = true;

        // Reset styles
        String errorStyle = "-fx-border-color: red; -fx-border-width: 2px;";
        String normalStyle = "";

        nomField.setStyle(normalStyle);
        contenueField.setStyle(normalStyle);
        typeComboBox.setStyle(normalStyle);
        statutComboBox.setStyle(normalStyle);
        lieuxField.setStyle(normalStyle);
        dateField.setStyle(normalStyle);

        if (nomField.getText().trim().isEmpty()) {
            errorBuilder.append("- Le nom de l'événement est requis.\n");
            nomField.setStyle(errorStyle);
            isValid = false;
        }

        if (contenueField.getText().trim().isEmpty()) {
            errorBuilder.append("- La description de l'événement est requise.\n");
            contenueField.setStyle(errorStyle);
            isValid = false;
        }

        if (typeComboBox.getValue() == null) {
            errorBuilder.append("- Le type d'événement doit être sélectionné.\n");
            typeComboBox.setStyle(errorStyle);
            isValid = false;
        }

        if (statutComboBox.getValue() == null) {
            errorBuilder.append("- Le statut de l'événement doit être sélectionné.\n");
            statutComboBox.setStyle(errorStyle);
            isValid = false;
        }

        if (lieuxField.getText().trim().isEmpty()) {
            errorBuilder.append("- Le lieu de l'événement est requis.\n");
            lieuxField.setStyle(errorStyle);
            isValid = false;
        }

        if (dateField.getValue() == null) {
            errorBuilder.append("- La date de l'événement est requise.\n");
            dateField.setStyle(errorStyle);
            isValid = false;
        } else if (dateField.getValue().isBefore(LocalDate.now())) {
            errorBuilder.append("- La date de l'événement ne peut pas être dans le passé.\n");
            dateField.setStyle(errorStyle);
            isValid = false;
        }

        if (!isValid) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", errorBuilder.toString());
        }

        return isValid;
    }

    private void showStatusMessage(String message, boolean isSuccess) {
        statusLabel.setText(message);

        if (isSuccess) {
            statusLabel.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 5; -fx-background-radius: 5;");
        } else {
            statusLabel.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-padding: 5; -fx-background-radius: 5;");
        }

        // Make the label visible with fade animation
        statusLabel.setOpacity(1);

        // Create a fade out transition
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), statusLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.seconds(2));
        fadeOut.play();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}