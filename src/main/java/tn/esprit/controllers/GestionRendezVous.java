package tn.esprit.controllers;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.models.RendeVous;
import tn.esprit.services.ServiceAddRdv;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class GestionRendezVous implements Initializable {

    @FXML private CalendarView calendarView;
    @FXML private ComboBox<String> type_rdv;
    @FXML private ComboBox<String> medecin;
    @FXML private TextArea cause;
    @FXML private Label notificationLabel;
    @FXML private Button notificationButton;

    // Labels pour les messages d'erreur
    @FXML private Label dateError;
    @FXML private Label typeError;
    @FXML private Label medecinError;
    @FXML private Label causeError;

    private Connection connection;
    private final ServiceAddRdv serviceAddRdv = new ServiceAddRdv();
    private Map<LocalDate, Integer> rdvCountByDate = new HashMap<>();
    private int currentMedecinId = -1;
    private Consumer<String> notificationListener;
    private final List<String> notificationHistory = new ArrayList<>();
    private LocalDate selectedDate;

    public void setNotificationListener(Consumer<String> listener) {
        this.notificationListener = listener;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialisation des types de rendez-vous
        type_rdv.setItems(FXCollections.observableArrayList("consultation", "suivi", "urgence"));

        // Connexion à la base de données
        connectDB();

        // Chargement des médecins
        loadMedecins();

        // Configurer le CalendarView
        configureCalendarView();

        // Effacer les messages d'erreur lorsqu'on modifie les champs
        type_rdv.valueProperty().addListener((obs, oldVal, newVal) -> typeError.setText(""));
        medecin.valueProperty().addListener((obs, oldVal, newVal) -> {
            medecinError.setText("");
            if (newVal != null && !newVal.isEmpty()) {
                updateMedecinSelection();
            }
        });
        cause.textProperty().addListener((obs, oldVal, newVal) -> causeError.setText(""));
    }

    private void configureCalendarView() {
        calendarView.setOnDateSelected(date -> {
            this.selectedDate = date;
            dateError.setText("");
            updateCalendarAppearance();
        });
    }

    private void updateCalendarAppearance() {
        if (currentMedecinId != -1) {
            calendarView.getCalendarDays().forEach(day -> {
                LocalDate date = day.getDate();
                int count = rdvCountByDate.getOrDefault(date, 0);

                if (date.equals(selectedDate)) {
                    day.setSelected(true);
                }

                if (count >= 3) {
                    day.setStyle("-fx-background-color: #ffcccc;");
                } else if (count == 2) {
                    day.setStyle("-fx-background-color: #ffebcc;");
                } else if (count <= 1 && !date.isBefore(LocalDate.now())) {
                    day.setStyle("-fx-background-color: #ccffcc;");
                }

                if (date.isBefore(LocalDate.now())) {
                    day.setDisable(true);
                }
            });
        }
    }

    private void updateMedecinSelection() {
        String[] nomPrenom = medecin.getValue().split(" ");
        if (nomPrenom.length < 2) {
            medecinError.setText("Format du médecin invalide");
            return;
        }

        String prenom = nomPrenom[0];
        String nom = nomPrenom[1];

        try {
            String medecinQuery = "SELECT id FROM user WHERE nom = ? AND prenom = ?";
            try (PreparedStatement psMedecin = connection.prepareStatement(medecinQuery)) {
                psMedecin.setString(1, nom);
                psMedecin.setString(2, prenom);
                ResultSet rs = psMedecin.executeQuery();
                if (rs.next()) {
                    currentMedecinId = rs.getInt("id");
                    loadRendezVousForMedecin();
                } else {
                    medecinError.setText("Médecin non trouvé");
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Problème lors de la sélection du médecin");
            e.printStackTrace();
        }
    }

    private void loadRendezVousForMedecin() {
        rdvCountByDate.clear();

        if (currentMedecinId == -1) return;

        String query = "SELECT date, COUNT(*) as count FROM rendez_vous WHERE user_id = ? GROUP BY date";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, currentMedecinId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                LocalDate date = rs.getDate("date").toLocalDate();
                int count = rs.getInt("count");
                rdvCountByDate.put(date, count);
            }

            updateCalendarAppearance();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des rendez-vous");
            e.printStackTrace();
        }
    }

    private void connectDB() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ehealth_database", "root", "");
            System.out.println("Connexion réussie !");
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de se connecter à la base de données");
            e.printStackTrace();
        }
    }

    private void loadMedecins() {
        ObservableList<String> medecinsList = FXCollections.observableArrayList();
        String query = "SELECT nom, prenom FROM user WHERE user_type = 'MEDECIN'";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String nomComplet = rs.getString("prenom") + " " + rs.getString("nom");
                medecinsList.add(nomComplet);
            }
            medecin.setItems(medecinsList);

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger la liste des médecins : " + e.getMessage());
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
            showAlert("Erreur", "Impossible de récupérer le nom du médecin");
            e.printStackTrace();
        }
        return doctorName;
    }

    @FXML
    void save(ActionEvent event) {
        if (validerFormulaire()) {
            try {
                // Vérifier si le médecin est sélectionné
                if (currentMedecinId == -1) {
                    medecinError.setText("Veuillez sélectionner un médecin");
                    return;
                }

                // Création et sauvegarde du rendez-vous
                RendeVous rdv = new RendeVous();
                rdv.setDate(selectedDate);
                rdv.setType(type_rdv.getValue());
                rdv.setCause(cause.getText());
                rdv.setIdMedecin(currentMedecinId);
                rdv.setIdPatient(1); // À remplacer par l'ID du patient connecté
                rdv.setStatut("en_attente"); // Statut par défaut

                // Utilisation de ServiceAddRdv pour ajouter le rendez-vous
                serviceAddRdv.add(rdv);

                // Afficher la notification
                showNotification("Rendez-vous enregistré avec succès !");

                // Envoyer la notification à l'historique
                String doctorName = getDoctorName(currentMedecinId);
                String notificationMessage = String.format("Rendez-vous avec Dr %s ajouté pour le %s. Veuillez attendre la réponse du médecin.",
                        doctorName, rdv.getDate());
                addNotification(notificationMessage);
                if (notificationListener != null) {
                    notificationListener.accept(notificationMessage);
                }

                // Mettre à jour le calendrier après l'ajout
                loadRendezVousForMedecin();

                // Redirection vers listrdv.fxml

            } catch (Exception e) {
                showAlert("Erreur", "Problème lors de l'enregistrement du rendez-vous");
                e.printStackTrace();
            }
        }
    }

    private void showNotification(String message) {
        notificationLabel.setText(message);
        notificationLabel.setVisible(true);
        notificationLabel.setManaged(true);

        // Masquer la notification après 3 secondes
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            notificationLabel.setVisible(false);
            notificationLabel.setManaged(false);
        });
        pause.play();
    }

    private boolean validerFormulaire() {
        boolean isValid = true;

        // Réinitialiser les messages d'erreur
        dateError.setText("");
        typeError.setText("");
        medecinError.setText("");
        causeError.setText("");

        if (selectedDate == null) {
            dateError.setText("La date est obligatoire");
            isValid = false;
        } else if (selectedDate.isBefore(LocalDate.now())) {
            dateError.setText("La date ne peut pas être dans le passé");
            isValid = false;
        }

        // Validation du type
        if (type_rdv.getValue() == null || type_rdv.getValue().isEmpty()) {
            typeError.setText("Le type est obligatoire");
            isValid = false;
        }

        // Validation du médecin
        if (medecin.getValue() == null || medecin.getValue().isEmpty()) {
            medecinError.setText("Le médecin est obligatoire");
            isValid = false;
        }

        // Validation de la cause
        if (cause.getText() == null || cause.getText().trim().isEmpty()) {
            causeError.setText("La cause est obligatoire");
            isValid = false;
        }

        return isValid;
    }

    private void clearFields() {
        calendarView.setSelectedDate(null);
        selectedDate = null;
        type_rdv.getSelectionModel().clearSelection();
        cause.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void showNotificationHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/views/notificationHistorique.fxml"));
            Parent root = loader.load();

            NotificationHistoryController controller = loader.getController();
            controller.setNotifications(notificationHistory);

            Stage stage = new Stage();
            stage.setTitle("Historique des Notifications");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'historique des notifications");
            e.printStackTrace();
        }
    }

    private void addNotification(String message) {
        notificationHistory.add(message);
    }
}