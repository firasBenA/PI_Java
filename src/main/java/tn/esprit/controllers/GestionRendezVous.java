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
import tn.esprit.models.User;
import tn.esprit.services.AuthService;
import tn.esprit.services.ServiceAddRdv;
import tn.esprit.services.ServiceUser;
import tn.esprit.utils.SceneManager;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class GestionRendezVous implements Initializable {

    @FXML private DatePicker datePicker;
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

    private final ServiceAddRdv serviceAddRdv = new ServiceAddRdv();
    private Map<String, Integer> doctorIdMap = new HashMap<>();
    private Consumer<String> notificationListener;
    private final List<String> notificationHistory = new ArrayList<>();
    private AuthService authService;
    private SceneManager sceneManager;
    private User currentUser;

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("Current user set in GestionRendezVous: " + (user != null ? user.getNom() : "null"));
    }

    public void setNotificationListener(Consumer<String> listener) {
        this.notificationListener = listener;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialisation des types de rendez-vous
        type_rdv.setItems(FXCollections.observableArrayList("consultation", "suivi", "urgence"));

        // Chargement des médecins
        loadMedecins();

        // Effacer les messages d'erreur lorsqu'on modifie les champs
        type_rdv.valueProperty().addListener((obs, oldVal, newVal) -> typeError.setText(""));
        medecin.valueProperty().addListener((obs, oldVal, newVal) -> medecinError.setText(""));
        cause.textProperty().addListener((obs, oldVal, newVal) -> causeError.setText(""));
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> dateError.setText(""));

        System.out.println("GestionRendezVous initialized (currentUser may be null)");
        initUserData();
    }

    private void initUserData() {
        if (currentUser != null) {
            System.out.println("Initializing user data for " + currentUser.getNom());
        }
    }

    private void loadMedecins() {
        ObservableList<String> medecinsList = FXCollections.observableArrayList();
        try {
            List<User> medecins = new ServiceUser().findMedecinsBySpecialite(null); // Fetch all doctors
            for (User medecin : medecins) {
                String nomComplet = medecin.getPrenom() + " " + medecin.getNom();
                medecinsList.add(nomComplet);
                doctorIdMap.put(nomComplet, medecin.getId());
            }
            medecin.setItems(medecinsList);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger la liste des médecins");
            e.printStackTrace();
        }
    }

    private String getDoctorName(int medecinId) {
        ServiceUser serviceUser = new ServiceUser();
        User doctor = serviceUser.getUserById(medecinId);
        if (doctor != null) {
            return doctor.getPrenom() + " " + doctor.getNom();
        }
        return "Inconnu";
    }

    @FXML
    void save(ActionEvent event) {
        if (validerFormulaire()) {
            try {
                // Vérifier si l'utilisateur est connecté
                if (currentUser == null) {
                    showAlert("Erreur", "Aucun utilisateur connecté");
                    return;
                }

                // Récupérer l'ID du médecin sélectionné
                String selectedDoctorName = medecin.getValue();
                Integer doctorId = doctorIdMap.get(selectedDoctorName);
                if (doctorId == null) {
                    medecinError.setText("Médecin invalide");
                    return;
                }

                // Création et sauvegarde du rendez-vous
                RendeVous rdv = new RendeVous();
                rdv.setDate(datePicker.getValue());
                rdv.setType(type_rdv.getValue());
                rdv.setCause(cause.getText());
                rdv.setIdMedecin(doctorId);
                rdv.setIdPatient(currentUser.getId());
                rdv.setStatut("en_attente");

                // Utilisation de ServiceAddRdv pour ajouter le rendez-vous
                serviceAddRdv.add(rdv, currentUser.getId());

                // Afficher la notification
                showNotification("Rendez-vous enregistré avec succès !");

                // Envoyer la notification à l'historique
                String doctorName = getDoctorName(doctorId);
                String notificationMessage = String.format("Rendez-vous avec Dr %s ajouté pour le %s. Veuillez attendre la réponse du médecin.",
                        doctorName, rdv.getDate());
                addNotification(notificationMessage);
                if (notificationListener != null) {
                    notificationListener.accept(notificationMessage);
                }

                // Réinitialiser les champs
                clearFields();

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

        // Validation de la date
        if (datePicker.getValue() == null) {
            dateError.setText("La date est obligatoire");
            isValid = false;
        } else if (datePicker.getValue().isBefore(LocalDate.now())) {
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
        datePicker.setValue(null);
        type_rdv.getSelectionModel().clearSelection();
        medecin.getSelectionModel().clearSelection();
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