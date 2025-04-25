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
import tn.esprit.controllers.EmailService;

import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListeConsultationsController implements Initializable {

    @FXML
    private ListView<Consultation> consultationsListView;

    private final ServiceConsultation serviceConsultation = new ServiceConsultation();
    private final EmailService emailService = new EmailService();
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
        consultationsListView.setCellFactory(param -> new ListCell<Consultation>() {
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
                        showApprovalDialog(consultation);
                    }
                });

                btnRefuser.setOnAction(event -> {
                    Consultation consultation = getItem();
                    if (consultation != null) {
                        serviceConsultation.updateStatutRendezVous(consultation.getRendez_vous_id(), "refusé");
                        consultation.setStatut("refusé");
                        updateItem(consultation, false);
                        try {
                            String patientEmail = serviceConsultation.getPatientEmailById(consultation.getPatient_id());
                            emailService.sendRejectionEmail(patientEmail);
                            showAlert("Succès", "Consultation refusée et email envoyé au patient");
                        } catch (Exception e) {
                            showAlert("Erreur", "Échec de l'envoi de l'email : " + e.getMessage());
                        }
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

    private void showApprovalDialog(Consultation consultation) {
        Dialog<ApprovalData> dialog = new Dialog<>();
        dialog.setTitle("Approuver la consultation");
        dialog.setHeaderText("Entrez la date et le prix de la consultation");

        // Ajouter les boutons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Créer les champs
        DatePicker datePicker = new DatePicker(consultation.getDate());
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isBefore(LocalDate.now()));
            }
        });

        TextField priceField = new TextField();
        priceField.setPromptText("Prix (TND)");
        priceField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                priceField.setText(oldValue);
            }
        });

        dialog.getDialogPane().setContent(new HBox(10,
                new Label("Date:"), datePicker,
                new Label("Prix:"), priceField
        ));

        // Convertir le résultat
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    LocalDate date = datePicker.getValue();
                    double price = Double.parseDouble(priceField.getText());
                    return new ApprovalData(date, price);
                } catch (NumberFormatException e) {
                    showAlert("Erreur", "Veuillez entrer un prix valide (nombre décimal).");
                    return null;
                }
            }
            return null;
        });

        Optional<ApprovalData> result = dialog.showAndWait();
        result.ifPresent(data -> {
            // Mettre à jour la consultation
            serviceConsultation.updateConsultationDateAndPrice(consultation.getId(), data.date, data.price);
            consultation.setDate(data.date);
            consultation.setPrix(data.price);
            consultation.setStatut("approuvé");

            // Envoyer l'email
            try {
                String patientEmail = serviceConsultation.getPatientEmailById(consultation.getPatient_id());
                emailService.sendApprovalEmail(patientEmail, data.date.toString(), data.price);
                showAlert("Succès", "Consultation approuvée et email envoyé au patient");
            } catch (Exception e) {
                showAlert("Erreur", "Échec de l'envoi de l'email : " + e.getMessage());
            }

            // Rafraîchir l'affichage
            consultationsListView.refresh();
        });
    }

    private void modifierDateConsultation(Consultation consultation) {
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Modifier la date de consultation");
        dialog.setHeaderText("Nouvelle date pour la consultation");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        DatePicker datePicker = new DatePicker(consultation.getDate());
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isBefore(LocalDate.now()));
            }
        });

        dialog.getDialogPane().setContent(new HBox(10, new Label("Date:"), datePicker));

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return datePicker.getValue();
            }
            return null;
        });

        Optional<LocalDate> result = dialog.showAndWait();
        result.ifPresent(newDate -> {
            serviceConsultation.updateConsultationDate(consultation.getId(), newDate);
            consultation.setDate(newDate);
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

    // Classe interne pour stocker les données du popup
    private static class ApprovalData {
        LocalDate date;
        double price;

        ApprovalData(LocalDate date, double price) {
            this.date = date;
            this.price = price;
        }
    }
}