package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import tn.esprit.models.Consultation;
import tn.esprit.models.User;
import tn.esprit.services.AuthService;
import tn.esprit.services.ServiceConsultation;
import tn.esprit.services.EmailService;
import tn.esprit.repository.UserRepository; // Adjust based on your actual package

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ListeConsultationsController implements Initializable {

    @FXML
    private FlowPane consultationsContainer;
    @FXML
    private ComboBox<String> typeFilterComboBox;
    @FXML
    private Button prevPageButton;
    @FXML
    private Button nextPageButton;
    @FXML
    private Label pageInfoLabel;

    private final ServiceConsultation serviceConsultation = new ServiceConsultation();
    private final EmailService emailService = new EmailService();
    private final AuthService authService;
    private final int itemsPerPage = 10;
    private int currentPage = 1;
    private int totalPages = 1;
    private List<Consultation> allConsultations;
    private String currentFilter = "Tous";

    public ListeConsultationsController() {
        // Initialize UserRepository (replace with your actual implementation)
        UserRepository userRepository = new UserRepository() {
            @Override
            public User save(User user) {
                return null;
            }

            @Override
            public User findByEmail(String email) {
                return null;
            }

            @Override
            public List<User> findAll() {
                return List.of();
            }

            @Override
            public void delete(int userId) {

            }

            @Override
            public User findById(int userId) {
                return null;
            }

            @Override
            public List<User> getAllUsers() {
                return List.of();
            }

            @Override
            public List<User> searchUsers(String keyword) {
                return List.of();
            }
        }; // Adjust based on your dependency injection
        this.authService = AuthService.getInstance(userRepository);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Vérifier si un utilisateur est connecté et s'il est un médecin
        if (authService.getCurrentUser() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun utilisateur connecté", "Veuillez vous connecter pour accéder à cette page.");
            redirectToLogin();
            return;
        }
        if (!authService.getCurrentUser().getUserType().equals("MEDECIN")) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Accès non autorisé", "Cette page est réservée aux médecins.");
            redirectToLogin();
            return;
        }

        setupFilterComboBox();
        loadAllConsultations();
        setupPaginationControls();
    }

    private void setupFilterComboBox() {
        // Initialize filter options
        ObservableList<String> filterOptions = FXCollections.observableArrayList("Tous", "consultation", "suivi", "urgence");
        typeFilterComboBox.setItems(filterOptions);
        typeFilterComboBox.getSelectionModel().select("Tous");

        typeFilterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            currentFilter = newVal;
            currentPage = 1;
            updateDisplayedConsultations();
        });
    }

    private void loadAllConsultations() {
        // Use current user's ID
        int medecinId = authService.getCurrentUser().getId();
        allConsultations = serviceConsultation.getByMedecinId(medecinId);
        updateDisplayedConsultations();
    }

    private void setupPaginationControls() {
        prevPageButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateDisplayedConsultations();
            }
        });

        nextPageButton.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                updateDisplayedConsultations();
            }
        });
    }

    private void updateDisplayedConsultations() {
        List<Consultation> filteredConsultations = filterConsultations();
        totalPages = (int) Math.ceil((double) filteredConsultations.size() / itemsPerPage);
        totalPages = Math.max(totalPages, 1);

        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, filteredConsultations.size());

        List<Consultation> pageConsultations = filteredConsultations.subList(fromIndex, toIndex);

        consultationsContainer.getChildren().clear();
        pageConsultations.forEach(consultation ->
                consultationsContainer.getChildren().add(createConsultationCard(consultation))
        );

        updatePaginationControls();
    }

    private List<Consultation> filterConsultations() {
        if ("Tous".equals(currentFilter)) {
            return allConsultations;
        }
        return allConsultations.stream()
                .filter(c -> currentFilter.equalsIgnoreCase(c.getType_consultation()))
                .collect(Collectors.toList());
    }

    private void updatePaginationControls() {
        pageInfoLabel.setText(String.format("Page %d/%d", currentPage, totalPages));
        prevPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= totalPages);
    }

    private VBox createConsultationCard(Consultation consultation) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(300);

        // Titre
        Label titleLabel = new Label("Consultation #" + consultation.getId());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Informations
        Label dateLabel = new Label("Date: " + consultation.getDate());
        Label typeLabel = new Label("Type: " + consultation.getType_consultation());
        Label patientLabel = new Label("Patient: " + consultation.getPatientPrenom() + " " + consultation.getPatientNom());

        // Statut
        Label statusLabel = new Label("Statut: " + consultation.getStatut());
        switch (consultation.getStatut()) {
            case "approuvé":
                statusLabel.setTextFill(Color.GREEN);
                break;
            case "refusé":
                statusLabel.setTextFill(Color.RED);
                break;
            default:
                statusLabel.setTextFill(Color.ORANGE);
        }

        // Boutons
        HBox buttonBox = new HBox(10);
        Button btnModifier = new Button("Modifier");
        Button btnApprouver = new Button("Approuver");
        Button btnRefuser = new Button("Refuser");

        // Style des boutons
        btnModifier.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        btnApprouver.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnRefuser.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        // Désactiver les boutons si statut déjà défini
        boolean statutDefini = !consultation.isEnAttente();
        btnModifier.setDisable(statutDefini);
        btnApprouver.setDisable(statutDefini);
        btnRefuser.setDisable(statutDefini);

        // Gestion des événements
        btnModifier.setOnAction(event -> {
            if (consultation.isEnAttente()) {
                modifierDateConsultation(consultation);
            } else {
                showAlert(Alert.AlertType.WARNING, "Attention", null, "Seules les consultations en attente peuvent être modifiées");
            }
        });

        btnApprouver.setOnAction(event -> showApprovalDialog(consultation));

        btnRefuser.setOnAction(event -> {
            serviceConsultation.updateStatutRendezVous(consultation.getRendez_vous_id(), "refusé");
            consultation.setStatut("refusé");
            try {
                String patientEmail = serviceConsultation.getPatientEmailById(consultation.getPatient_id());
                emailService.sendRejectionEmail(patientEmail);
                showAlert(Alert.AlertType.INFORMATION, "Succès", null, "Consultation refusée et email envoyé au patient");
                loadAllConsultations();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", null, "Échec de l'envoi de l'email : " + e.getMessage());
            }
        });

        buttonBox.getChildren().addAll(btnModifier, btnApprouver, btnRefuser);

        card.getChildren().addAll(titleLabel, dateLabel, typeLabel, patientLabel, statusLabel, buttonBox);

        return card;
    }

    private void showApprovalDialog(Consultation consultation) {
        Dialog<ApprovalData> dialog = new Dialog<>();
        dialog.setTitle("Approuver la consultation");
        dialog.setHeaderText("Définir le prix et l'heure de la consultation");

        // Boutons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField priceField = new TextField();
        priceField.setPromptText("Prix (TND)");

        // Champ Heure
        TextField timeField = new TextField();
        timeField.setPromptText("HH:mm");

        timeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() == 2 && oldVal.length() == 1) {
                timeField.setText(newVal + ":");
                timeField.positionCaret(3);
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Prix (TND):"), 0, 0);
        grid.add(priceField, 1, 0);
        grid.add(new Label("Heure (HH:mm):"), 0, 1);
        grid.add(timeField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    double price = Double.parseDouble(priceField.getText());
                    String timeStr = timeField.getText();

                    if (!timeStr.matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", null, "Format d'heure invalide. Utilisez HH:mm");
                        return null;
                    }

                    return new ApprovalData(consultation.getDate(), timeStr, price);
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", null, "Prix invalide");
                    return null;
                }
            }
            return null;
        });

        Optional<ApprovalData> result = dialog.showAndWait();
        result.ifPresent(data -> {
            serviceConsultation.updateStatutRendezVous(consultation.getRendez_vous_id(), "approuvé");
            consultation.setStatut("approuvé");

            try {
                emailService.sendApprovalEmail(
                        serviceConsultation.getPatientEmailById(consultation.getPatient_id()),
                        consultation.getDate().toString(),
                        data.time,
                        data.price
                );
                showAlert(Alert.AlertType.INFORMATION, "Succès", null, "Consultation approuvée");
                loadAllConsultations();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", null, "Erreur d'envoi d'email : " + e.getMessage());
            }
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
            loadAllConsultations();
            showAlert(Alert.AlertType.INFORMATION, "Succès", null, "Date de consultation modifiée avec succès");
        });
    }

    private void showAlert(Alert.AlertType type, String title, String header, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml")); // Adjust path to your login FXML
            Parent root = loader.load();
            Stage stage = (Stage) consultationsContainer.getScene().getWindow(); // Get current stage
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Redirection vers la page de connexion échouée : " + e.getMessage());
        }
    }

    private static class ApprovalData {
        LocalDate date;
        String time;
        double price;

        ApprovalData(LocalDate date, String time, double price) {
            this.date = date;
            this.time = time;
            this.price = price;
        }
    }
}