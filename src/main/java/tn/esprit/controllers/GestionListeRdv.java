package tn.esprit.controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.models.RendeVous;
import tn.esprit.services.ServiceAddRdv;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class GestionListeRdv implements Initializable {

    // FXML Components
    @FXML private VBox rdvContainer;
    @FXML private StackPane toastContainer;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label pageInfo;
    @FXML private Button addButton;
    @FXML private Button notificationButton;

    // Services and Data
    private final ServiceAddRdv serviceRendezVous = new ServiceAddRdv();
    private final int patientId = 1;
    private final List<String> notificationHistory = new ArrayList<>();
    private Connection connection;

    // Pagination
    private int currentPage = 1;
    private final int itemsPerPage = 10;
    private List<RendeVous> allRdvs = new ArrayList<>();
    private List<RendeVous> filteredRdvs = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connectDB();
        setupStatusFilter();
        loadAllRdvs();
        setupPagination();
        addNotification("Application démarrée à " + LocalDateTime.now());
    }

    private void connectDB() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ehealth_database", "root", "");
        } catch (SQLException e) {
            showAlert("Erreur", "Connexion à la base de données échouée", e.getMessage());
        }
    }

    private void setupStatusFilter() {
        statusFilter.getItems().addAll("Tous", "Approuvé", "En attente", "Refusé");
        statusFilter.getSelectionModel().selectFirst();
        statusFilter.setOnAction(e -> filterRdvs());
    }

    private void loadAllRdvs() {
        allRdvs = serviceRendezVous.findByPatientId(patientId);
        filterRdvs();
    }

    private void filterRdvs() {
        String selectedStatus = statusFilter.getSelectionModel().getSelectedItem();

        filteredRdvs = allRdvs.stream()
                .filter(rdv -> selectedStatus.equals("Tous") || rdv.getStatut().equalsIgnoreCase(selectedStatus))
                .sorted(Comparator.comparing(RendeVous::getDate).reversed())
                .collect(Collectors.toList());

        currentPage = 1;
        updatePagination();
    }

    private void setupPagination() {
        updatePagination();
    }

    private void updatePagination() {
        int totalPages = (int) Math.ceil((double) filteredRdvs.size() / itemsPerPage);

        // Update page info
        pageInfo.setText(String.format("Page %d/%d", currentPage, totalPages == 0 ? 1 : totalPages));

        // Clear current cards
        rdvContainer.getChildren().clear();

        // Calculate start and end indices
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredRdvs.size());

        // Add cards for current page
        for (int i = startIndex; i < endIndex; i++) {
            RendeVous rdv = filteredRdvs.get(i);
            StackPane card = createRdvCard(rdv);
            rdvContainer.getChildren().add(card);
        }
    }

    private StackPane createRdvCard(RendeVous rdv) {
        StackPane card = new StackPane();
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8; -fx-padding: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); " +
                "-fx-border-radius: 8; -fx-border-width: 2;");

        // Status-based border color
        String statusColor = switch(rdv.getStatut().toLowerCase()) {
            case "approuvé" -> "#2ecc71"; // Green
            case "en attente" -> "#f39c12"; // Orange
            case "refusé" -> "#e74c3c"; // Red
            default -> "#3498db"; // Blue
        };
        card.setStyle(card.getStyle() + "-fx-border-color: " + statusColor + ";");

        // Card content
        String doctorName = getDoctorName(rdv.getIdMedecin());
        Label infoLabel = new Label(String.format(
                "Date: %s\nMédecin: Dr %s\nType: %s\nStatut: %s\nCause: %s",
                rdv.getDate(),
                doctorName,
                rdv.getType(),
                rdv.getStatut(),
                rdv.getCause()
        ));
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
        infoLabel.setWrapText(true);

        // Action buttons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        Button editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        editButton.setOnAction(event -> editRDV(rdv));

        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        deleteButton.setOnAction(event -> deleteRDV(rdv));

        buttonsBox.getChildren().addAll(editButton, deleteButton);

        VBox cardContent = new VBox(10, infoLabel, buttonsBox);
        card.getChildren().add(cardContent);

        // Hover effects
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace("0.1", "0.2")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("0.2", "0.1")));

        return card;
    }

    @FXML
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
        }
    }

    @FXML
    private void nextPage() {
        int totalPages = (int) Math.ceil((double) filteredRdvs.size() / itemsPerPage);
        if (currentPage < totalPages) {
            currentPage++;
            updatePagination();
        }
    }

    private void editRDV(RendeVous rdv) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifierRendezVous.fxml"));
            Parent root = loader.load();

            ModifierRendezVousController controller = loader.getController();
            controller.setRendezVous(rdv);
            controller.setNotificationListener(this::addNotification);

            Stage stage = new Stage();
            stage.setTitle("Modifier Rendez-vous");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (controller.isModificationValidee()) {
                loadAllRdvs();
                showToast("Rendez-vous modifié avec succès", "#2ecc71");
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur", e.getMessage());
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
            String doctorName = getDoctorName(rdv.getIdMedecin());
            String notificationMessage = String.format("Rendez-vous avec Dr %s du %s supprimé.",
                    doctorName, rdv.getDate());

            // Ajouter à l'historique ET afficher le toast
            addNotification(notificationMessage);
            loadAllRdvs();
        }
    }

    @FXML
    private void addNewRDV() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addrendezvous.fxml"));
            Parent root = loader.load();

            GestionRendezVous controller = loader.getController();
            controller.setNotificationListener(this::addNotification);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Nouveau Rendez-vous");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadAllRdvs();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout", e.getMessage());
        }
    }

    @FXML
    private void showNotificationHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/notificationHistorique.fxml"));
            Parent root = loader.load();

            NotificationHistoryController controller = loader.getController();
            controller.setNotifications(notificationHistory);

            Stage stage = new Stage();
            stage.setTitle("Historique des Notifications");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'historique", e.getMessage());
        }
    }

    private void showToast(String message, String color) {
        Label toast = new Label(message);
        toast.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 10 20; " +
                "-fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 1);");

        toastContainer.getChildren().add(toast);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> toastContainer.getChildren().remove(toast));
        delay.play();
    }

    private void addNotification(String message) {
        String fullMessage = LocalDateTime.now().toString() + " - " + message;
        notificationHistory.add(fullMessage);
        showToast(message, "#3498db");
    }

    private String getDoctorName(int medecinId) {
        try {
            String query = "SELECT prenom, nom FROM user WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, medecinId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getString("prenom") + " " + rs.getString("nom");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Inconnu";
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}