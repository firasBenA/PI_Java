package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import tn.esprit.models.Consultation;
import tn.esprit.services.ServiceConsultation;

import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListeConsultationsController implements Initializable {

    @FXML
    private ListView<Consultation> consultationsListView;

    private final ServiceConsultation serviceConsultation = new ServiceConsultation();
    private final int medecinId = 4;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chargerConsultations();
        configurerListView();
    }

    private void chargerConsultations() {
        ObservableList<Consultation> consultations = FXCollections.observableArrayList(
                serviceConsultation.getByMedecinId(medecinId)
        );
        consultationsListView.setItems(consultations);
    }

    private void configurerListView() {
        consultationsListView.setCellFactory(param -> new javafx.scene.control.ListCell<Consultation>() {
            private final Button btnApprouver = new Button("Approuver");
            private final Button btnRefuser = new Button("Refuser");
            private final Button btnModifier = new Button("Modifier");
            private final HBox buttonBox = new HBox(5, btnModifier, btnApprouver, btnRefuser);
            private final Label statusLabel = new Label();

            {
                // Style des boutons
                btnApprouver.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btnRefuser.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                btnModifier.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

                // Gestion des événements
                btnApprouver.setOnAction(event -> {
                    Consultation consultation = getItem();
                    if (consultation != null) {
                        serviceConsultation.updateStatutRendezVous(consultation.getRendez_vous_id(), "approuvé");
                        consultation.setStatut("approuvé");
                        updateItem(consultation, false);
                        showAlert("Succès", "Consultation approuvée avec succès");
                    }
                });

                btnRefuser.setOnAction(event -> {
                    Consultation consultation = getItem();
                    if (consultation != null) {
                        serviceConsultation.updateStatutRendezVous(consultation.getRendez_vous_id(), "refusé");
                        consultation.setStatut("refusé");
                        updateItem(consultation, false);
                        showAlert("Information", "Consultation refusée");
                    }
                });

                btnModifier.setOnAction(event -> {
                    Consultation consultation = getItem();
                    if (consultation != null && consultation.isEnAttente()) {
                        modifierDateConsultation(consultation);
                    } else if (consultation != null) {
                        showAlert("Attention", "Seules les consultations en attente peuvent être modifiées");
                    }
                });
            }

            @Override
            protected void updateItem(Consultation consultation, boolean empty) {
                super.updateItem(consultation, empty);

                if (empty || consultation == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(String.format(
                            "Date: %s | Type: %s | Patient: %s %s | Statut: ",
                            consultation.getDate(),
                            consultation.getType_consultation(),
                            consultation.getPatientPrenom(),
                            consultation.getPatientNom()
                    ));

                    statusLabel.setText(consultation.getStatut());
                    updateStatusColor(consultation.getStatut());

                    setGraphic(new HBox(10, statusLabel, buttonBox));

                    // Désactiver les boutons selon le statut
                    boolean statutDefini = !consultation.isEnAttente();
                    btnApprouver.setDisable(statutDefini);
                    btnRefuser.setDisable(statutDefini);
                    btnModifier.setDisable(statutDefini);
                }
            }

            private void updateStatusColor(String statut) {
                switch (statut) {
                    case "approuvé":
                        statusLabel.setTextFill(Color.GREEN);
                        break;
                    case "refusé":
                        statusLabel.setTextFill(Color.RED);
                        break;
                    default:
                        statusLabel.setTextFill(Color.ORANGE);
                }
            }
        });
    }

    private void modifierDateConsultation(Consultation consultation) {
        // Créer une boîte de dialogue pour modifier la date
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Modifier la date de consultation");
        dialog.setHeaderText("Nouvelle date pour la consultation");

        // Ajouter les boutons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Créer le DatePicker
        DatePicker datePicker = new DatePicker(consultation.getDate());
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isBefore(LocalDate.now()));
            }
        });

        dialog.getDialogPane().setContent(new HBox(10, new Label("Date:"), datePicker));

        // Convertir le résultat en LocalDate
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return datePicker.getValue();
            }
            return null;
        });

        Optional<LocalDate> result = dialog.showAndWait();
        result.ifPresent(newDate -> {
            // Mettre à jour la date dans la base de données
            serviceConsultation.updateConsultationDate(consultation.getId(), newDate);

            // Mettre à jour l'objet consultation
            consultation.setDate(newDate);

            // Rafraîchir l'affichage
            consultationsListView.refresh();

            showAlert("Succès", "Date de consultation modifiée avec succès");
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}