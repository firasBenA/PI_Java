package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.models.Diagnostique;
import tn.esprit.models.Prescription;
import tn.esprit.services.ServiceDiagnostique;
import tn.esprit.services.ServicePrescription;

import java.io.IOException;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

import javafx.scene.layout.HBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MedecinPrescDiag {

    @FXML
    private TableView<Diagnostique> diagnostiqueTable;
    @FXML
    private TableColumn<Diagnostique, Integer> diagIdCol;
    @FXML
    private TableColumn<Diagnostique, String> symptomsCol;
    @FXML
    private TableColumn<Diagnostique, String> zoneCorpsCol;
    @FXML
    private TableColumn<Diagnostique, String> resultCol;
    @FXML
    private TableColumn<Diagnostique, Date> dateDiagCol;

    @FXML
    private TableColumn<Diagnostique, Void> actionsDiagCol;

    @FXML
    private TableView<Prescription> prescriptionTable;
    @FXML
    private TableColumn<Prescription, Integer> prescIdCol;
    @FXML
    private TableColumn<Prescription, String> titreCol;
    @FXML
    private TableColumn<Prescription, String> contenueCol;
    @FXML
    private TableColumn<Prescription, Date> datePrescCol;
    @FXML
    private TableColumn<Prescription, Void> actionsPrescCol;


    private final ServiceDiagnostique serviceDiagnostique = new ServiceDiagnostique();
    private final ServicePrescription servicePrescription = new ServicePrescription();

    @FXML
    public void initialize() {
        // Initialize Diagnostic Table
        diagIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        symptomsCol.setCellValueFactory(new PropertyValueFactory<>("selectedSymptoms"));
        resultCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        zoneCorpsCol.setCellValueFactory(new PropertyValueFactory<>("zoneCorps"));
        dateDiagCol.setCellValueFactory(new PropertyValueFactory<>("dateSymptomes"));

        // Initialize Prescription Table
        prescIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        contenueCol.setCellValueFactory(new PropertyValueFactory<>("contenue"));
        datePrescCol.setCellValueFactory(new PropertyValueFactory<>("date_prescription"));

        loadDiagnostiques();
        loadPrescriptions();

        // Add buttons to diagnostic rows
        addActionButtonsToDiagnostique();
        addActionButtonsToPrescription();

    }

    private void loadDiagnostiques() {
        List<Diagnostique> diagnostics = serviceDiagnostique.getAll()
                .stream()
                .filter(d -> d.getStatus() == 0)
                .toList();

        diagnostiqueTable.getItems().setAll(diagnostics);
    }


    private void loadPrescriptions() {
        List<Prescription> prescriptions = servicePrescription.getAll();
        prescriptionTable.getItems().setAll(prescriptions);
    }

    private void addActionButtonsToDiagnostique() {
        actionsDiagCol.setCellFactory(param -> new TableCell<>() {
            private final Button addBtn = new Button("Ajouter");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox pane = new HBox(5, addBtn, deleteBtn);

            {
                addBtn.setOnAction(event -> {
                    Diagnostique diag = getTableView().getItems().get(getIndex());
                    openAjouterPrescription(diag);  // Pass the Diagnostique object to the next method
                });

                deleteBtn.setOnAction(event -> {
                    Diagnostique diag = getTableView().getItems().get(getIndex());

                    // Create the confirmation dialog
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce diagnostic ?");
                    alert.setContentText("Cette action est irréversible.");

                    // Show the dialog and wait for a response
                    Optional<ButtonType> result = alert.showAndWait();

                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        // If the user clicks "OK", delete the Diagnostique
                        serviceDiagnostique.delete(diag);  // Pass the Diagnostique object directly
                        loadDiagnostiques();  // Reload the table after deletion
                    } else {
                        // If the user clicks "Cancel", do nothing
                        System.out.println("Suppression annulée");
                    }
                });

                addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }


    private void addActionButtonsToPrescription() {
        actionsPrescCol.setCellFactory(param -> new TableCell<>() {
            private final Button modifyBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox pane = new HBox(5, modifyBtn, deleteBtn);

            {
                modifyBtn.setOnAction(event -> {
                    Prescription prescription = getTableView().getItems().get(getIndex());
                    openModifierPrescription(prescription);  // You should define this method
                });

                deleteBtn.setOnAction(event -> {
                    Prescription prescription = getTableView().getItems().get(getIndex());

                    // Create the confirmation dialog
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setHeaderText("Êtes-vous sûr de vouloir supprimer cette prescription ?");
                    alert.setContentText("Cette action est irréversible.");

                    // Show the dialog and wait for a response
                    Optional<ButtonType> result = alert.showAndWait();

                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        // If the user clicks "OK", delete the Prescription
                        servicePrescription.delete(prescription);  // Assuming you have a delete method
                        loadPrescriptions();  // Reload the table after deletion
                    } else {
                        // If the user clicks "Cancel", do nothing
                        System.out.println("Suppression annulée");
                    }
                });

                modifyBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void openAjouterPrescription(Diagnostique diagnostique) {
        try {
            // Assuming you're using FXMLLoader to load the scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterPrescriptionDepuisDiag.fxml"));
            Parent root = loader.load();

            // Get the controller of the AjouterPrescriptionDepuisDiag scene
            AjouterPrescriptionDepuisDiag controller = loader.getController();
            controller.setDiagnostique(diagnostique);  // Pass the selected Diagnostique object

            // Create a new Stage for the prescription window and show it
            Stage stage = new Stage();
            stage.setTitle("Ajouter Prescription");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadPrescriptions();
            loadDiagnostiques();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void openModifierPrescription(Prescription prescription) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierPrescription.fxml"));
            Parent root = loader.load();

            ModifierPrescriptionController controller = loader.getController();
            controller.setPrescription(prescription);

            Stage stage = new Stage();
            stage.setTitle("Modifier Prescription");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadPrescriptions();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
