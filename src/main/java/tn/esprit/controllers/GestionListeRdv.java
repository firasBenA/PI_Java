package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.models.RendeVous;
import tn.esprit.services.ServiceAddRdv;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class GestionListeRdv implements Initializable {

    @FXML
    private ListView<RendeVous> rdvListView;

    @FXML
    private Button addButton;

    private final ServiceAddRdv serviceRendezVous = new ServiceAddRdv();
    private final int patientId = 1; // ID du patient fixé à 1

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureListView();
        loadPatientRDVs();
    }

    private void configureListView() {
        rdvListView.setCellFactory(param -> new ListCell<RendeVous>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox buttonsBox = new HBox(5, editButton, deleteButton);
            private final HBox container = new HBox(10);

            {
                // Style des boutons
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                // Gestion des événements
                editButton.setOnAction(event -> {
                    RendeVous rdv = getItem();
                    if (rdv != null) {
                        editRDV(rdv);
                    }
                });

                deleteButton.setOnAction(event -> {
                    RendeVous rdv = getItem();
                    if (rdv != null) {
                        deleteRDV(rdv);
                    }
                });
            }

            @Override
            protected void updateItem(RendeVous rdv, boolean empty) {
                super.updateItem(rdv, empty);

                if (empty || rdv == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label infoLabel = new Label(String.format(
                            "Date: %s | Médecin: %d | Type: %s | Statut: %s | Cause: %s",
                            rdv.getDate(),
                            rdv.getIdMedecin(),
                            rdv.getType(),
                            rdv.getStatut(),
                            rdv.getCause()
                    ));

                    container.getChildren().setAll(infoLabel, buttonsBox);
                    setGraphic(container);
                }
            }
        });
    }

    private void loadPatientRDVs() {
        List<RendeVous> rdvs = serviceRendezVous.findByPatientId(patientId);
        ObservableList<RendeVous> items = FXCollections.observableArrayList(rdvs);
        rdvListView.setItems(items);
    }

    private void editRDV(RendeVous rdv) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifierRendezVous.fxml"));
            Parent root = loader.load();

            ModifierRendezVousController controller = loader.getController();
            controller.setRendezVous(rdv);

            Stage stage = new Stage();
            stage.setTitle("Modifier Rendez-vous");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (controller.isModificationValidee()) {
                loadPatientRDVs();
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur",
                    "Une erreur s'est produite lors de l'ouverture de l'éditeur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteRDV(RendeVous rdv) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le rendez-vous");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce rendez-vous ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            serviceRendezVous.delete(rdv);
            loadPatientRDVs();
            showAlert("Succès", "Rendez-vous supprimé", "Le rendez-vous a été supprimé avec succès.");
        }
    }

    @FXML
    private void addNewRDV() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addrendezvous.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Nouveau Rendez-vous");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadPatientRDVs();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout", e.getMessage());
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}