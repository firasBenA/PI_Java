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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class GestionListeRdv implements Initializable {

    @FXML private ListView<RendeVous> rdvListView;
    @FXML private Button addButton;
    @FXML private Button notificationButton;

    private final ServiceAddRdv serviceRendezVous = new ServiceAddRdv();
    private final int patientId = 1; // ID du patient fixé à 1
    private final List<String> notificationHistory = new ArrayList<>(); // Historique des notifications
    private Connection connection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connectDB();
        configureListView();
        loadPatientRDVs();
        // Ajouter une notification initiale pour tester
        addNotification("Application démarrée à " + LocalDateTime.now());
    }

    private void connectDB() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ehealth_database", "root", "");
            System.out.println("Connexion à la base de données réussie !");
        } catch (SQLException e) {
            showAlert("Erreur", "Connexion à la base de données échouée", e.getMessage());
            e.printStackTrace();
        }
    }

    private String getDoctorName(int medecinId) {
        String doctorName = "Inconnu";
        try {
            String query = "SELECT prenom, nom FROM user WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, medecinId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String prenom = rs.getString("prenom");
                    String nom = rs.getString("nom");
                    doctorName = prenom + " " + nom;
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de récupérer le nom du médecin", e.getMessage());
            e.printStackTrace();
        }
        return doctorName;
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
                    String doctorName = getDoctorName(rdv.getIdMedecin());
                    Label infoLabel = new Label(String.format(
                            "Date: %s | Médecin: Dr %s | Type: %s | Statut: %s | Cause: %s",
                            rdv.getDate(),
                            doctorName,
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
            // Debug: Vérifier si le fichier FXML est trouvé
            URL fxmlLocation = getClass().getResource("/modifierRendezVous.fxml");
            if (fxmlLocation == null) {
                throw new IOException("Cannot find /tn/esprit/views/modifierRendezVous.fxml");
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            ModifierRendezVousController controller = loader.getController();
            controller.setRendezVous(rdv);
            // Passer le listener de notifications
            controller.setNotificationListener(message -> addNotification(message));

            Stage stage = new Stage();
            stage.setTitle("Modifier Rendez-vous");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (controller.isModificationValidee()) {
                loadPatientRDVs();
                String doctorName = getDoctorName(rdv.getIdMedecin());
                addNotification(String.format("Rendez-vous avec Dr %s modifié pour le %s. Veuillez attendre la réponse du médecin.",
                        doctorName, rdv.getDate()));
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur",
                    "Vérifiez que le fichier modifierRendezVous.fxml est dans src/main/resources/tn/esprit/views/: " + e.getMessage());
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
            String doctorName = getDoctorName(rdv.getIdMedecin());
            addNotification(String.format("Rendez-vous avec Dr %s du %s supprimé.",
                    doctorName, rdv.getDate()));
            showAlert("Succès", "Rendez-vous supprimé", "Le rendez-vous a été supprimé avec succès.");
        }
    }

    @FXML
    private void addNewRDV() {
        try {
            // Debug: Vérifier si le fichier FXML est trouvé
            URL fxmlLocation = getClass().getResource("/addrendezvous.fxml");
            if (fxmlLocation == null) {
                throw new IOException("Cannot find /tn/esprit/views/addrendezvous.fxml");
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Récupérer le contrôleur pour écouter l'ajout
            GestionRendezVous controller = loader.getController();
            controller.setNotificationListener(message -> addNotification(message));

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Nouveau Rendez-vous");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadPatientRDVs();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout",
                    "Vérifiez que le fichier addrendezvous.fxml est dans src/main/resources/tn/esprit/views/: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showNotificationHistory() {
        try {
            // Debug: Vérifier si le fichier FXML est trouvé
            URL fxmlLocation = getClass().getResource("/notificationHistorique.fxml");
            if (fxmlLocation == null) {
                throw new IOException("Cannot find /notificationHistorique.fxml");
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            NotificationHistoryController controller = loader.getController();
            controller.setNotifications(notificationHistory);

            Stage stage = new Stage();
            stage.setTitle("Historique des Notifications");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'historique des notifications",
                    "Vérifiez que le fichier notificationHistorique.fxml est dans src/main/resources/tn/esprit/views/: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addNotification(String message) {
        notificationHistory.add(message);
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}