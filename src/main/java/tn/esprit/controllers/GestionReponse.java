package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.models.Reclamation;
import tn.esprit.models.Reponse;
import tn.esprit.services.ServiceReclamation;
import tn.esprit.services.ServiceReponse;
import tn.esprit.utils.MyDataBase;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class GestionReponse {

    @FXML
    private ListView<Reclamation> listViewReclamations;

    @FXML
    private TextField TFReclamationId, TFReclamationSujet;

    @FXML
    private TextArea TAContenu;

    private final ServiceReclamation serviceReclamation = new ServiceReclamation();
    private final ServiceReponse serviceReponse = new ServiceReponse();
    private ObservableList<Reclamation> observableReclamations;

    @FXML
    public void initialize() {
        loadReclamations();

        // Custom cell factory for ListView
        listViewReclamations.setCellFactory(listView -> new ListCell<Reclamation>() {
            @Override
            protected void updateItem(Reclamation reclamation, boolean empty) {
                super.updateItem(reclamation, empty);
                if (empty || reclamation == null) {
                    setText(null);
                } else {
                    setText("ID: " + reclamation.getId() + " | Sujet: " + reclamation.getSujet() + " | Date: " + reclamation.getDateDebut() + " | État: " + reclamation.getEtat());
                }
            }
        });

        // Update form fields when a reclamation is selected
        listViewReclamations.setOnMouseClicked(event -> {
            Reclamation selected = listViewReclamations.getSelectionModel().getSelectedItem();
            if (selected != null) {
                TFReclamationId.setText(String.valueOf(selected.getId()));
                TFReclamationSujet.setText(selected.getSujet());
            }
        });
    }

    private void loadReclamations() {
        List<Reclamation> reclamationList = serviceReclamation.getAll();
        observableReclamations = FXCollections.observableArrayList(reclamationList);
        listViewReclamations.setItems(observableReclamations);
    }

    @FXML
    public void Save(ActionEvent actionEvent) {
        try {
            String reclamationIdText = TFReclamationId.getText();
            String contenu = TAContenu.getText();
            LocalDate date = LocalDate.now();

            if (reclamationIdText.isEmpty()) {
                showAlert("Erreur", "Veuillez sélectionner une réclamation !");
                return;
            }
            if (contenu.isEmpty()) {
                showAlert("Erreur", "Le contenu de la réponse ne peut pas être vide !");
                return;
            }

            int reclamationId = Integer.parseInt(reclamationIdText);
            Reclamation selectedReclamation = serviceReclamation.getById(reclamationId);
            if (selectedReclamation == null) {
                showAlert("Erreur", "Réclamation non trouvée !");
                return;
            }

            System.out.println("Date before creating Reponse: " + date);
            Reponse reponse = new Reponse(contenu, date, reclamationId);
            System.out.println("Reponse dateReponse after creation: " + reponse.getDateReponse());
            System.out.println("Reponse reclamationId after creation: " + reponse.getReclamationId());

            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirmation");
            confirmationAlert.setHeaderText("Ajouter une réponse");
            confirmationAlert.setContentText("Voulez-vous vraiment ajouter cette réponse ?\n\n" +
                    "Réclamation ID : " + reclamationId + "\n" +
                    "Sujet de la réclamation : " + selectedReclamation.getSujet() + "\n" +
                    "Contenu : " + contenu + "\n" +
                    "Date : " + date);

            DialogPane dialogPane = confirmationAlert.getDialogPane();
            System.out.println("Loading stylesheet for confirmation dialog...");
            java.net.URL cssUrl = getClass().getResource("/tn/esprit/styles/styles.css");
            if (cssUrl == null) {
                System.out.println("Error: CSS file not found at /tn/esprit/styles/styles.css");
            } else {
                System.out.println("CSS file found: " + cssUrl.toExternalForm());
                dialogPane.getStylesheets().add(cssUrl.toExternalForm());
                dialogPane.getStyleClass().add("sweet-alert");
            }

            System.out.println("Showing confirmation dialog...");
            Optional<ButtonType> result = confirmationAlert.showAndWait();
            System.out.println("Confirmation dialog result: " + result);

            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Start a transaction
                Connection cnx = MyDataBase.getInstance().getCnx();
                cnx.setAutoCommit(false); // Disable auto-commit
                try {
                    System.out.println("User confirmed, adding response...");
                    serviceReponse.add(reponse);
                    System.out.println("Response added, updating reclamation status...");

                    selectedReclamation.setEtat("Traité");
                    serviceReclamation.update(selectedReclamation);
                    System.out.println("Reclamation status updated to Traité");

                    cnx.commit(); // Commit the transaction
                    System.out.println("Transaction committed successfully");

                    System.out.println("Response added, showing success alert...");
                    showAlert("Succès", "Réponse ajoutée avec la date : " + date.toString() + " ! La réclamation est maintenant marquée comme Traité.");
                    clearFields();
                    loadReclamations();
                } catch (Exception e) {
                    cnx.rollback(); // Roll back on error
                    System.out.println("Transaction rolled back due to error: " + e.getMessage());
                    throw e; // Re-throw to be caught by the outer catch block
                } finally {
                    cnx.setAutoCommit(true); // Restore auto-commit
                }
            } else {
                System.out.println("Ajout de la réponse annulé.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Problème : " + e.getMessage());
        }
    }
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);

        // Style the success/error alert
        DialogPane dialogPane = alert.getDialogPane();
        System.out.println("Loading stylesheet for alert...");
        java.net.URL cssUrl = getClass().getResource("src/main/resources/tn/esprit/styles/styles.css");
        if (cssUrl == null) {
            System.out.println("Error: CSS file not found at /tn/esprit/styles/styles.css");
        } else {
            System.out.println("CSS file found: " + cssUrl.toExternalForm());
            dialogPane.getStylesheets().add(cssUrl.toExternalForm());
            dialogPane.getStyleClass().add("sweet-alert");
        }

        alert.showAndWait();
    }

    private void clearFields() {
        TFReclamationId.clear();
        TFReclamationSujet.clear();
        TAContenu.clear();
        listViewReclamations.getSelectionModel().clearSelection();
    }
}