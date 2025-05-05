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
import javafx.util.Callback;
import javafx.util.Duration;
import tn.esprit.models.RendeVous;
import tn.esprit.services.ServiceAddRdv;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class ModifierRendezVousController implements Initializable {

    @FXML private DatePicker date;
    @FXML private ComboBox<String> type_rdv;
    @FXML private ComboBox<String> medecin;
    @FXML private TextArea cause;
    @FXML private Button saveButton;
    @FXML private Button notificationButton;
    @FXML private Label notificationLabel;

    // Labels pour les messages d'erreur
    @FXML private Label dateError;
    @FXML private Label typeError;
    @FXML private Label medecinError;
    @FXML private Label causeError;

    private Connection connection;
    private final ServiceAddRdv serviceAddRdv = new ServiceAddRdv();
    private Map<LocalDate, Integer> rdvCountByDate = new HashMap<>();
    private int currentMedecinId = -1;
    private RendeVous rendezVous;
    private boolean modificationValidee = false;
    private Consumer<String> notificationListener;
    private final List<String> notificationHistory = new ArrayList<>(); // Historique des notifications

    public void setNotificationListener(Consumer<String> listener) {
        this.notificationListener = listener;
    }

    public void setRendezVous(RendeVous rdv) {
        this.rendezVous = rdv;
        loadRendezVousData();
    }

    public boolean isModificationValidee() {
        return modificationValidee;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialisation des types de rendez-vous
        type_rdv.setItems(FXCollections.observableArrayList("consultation", "suivi", "urgence"));

        // Connexion à la base de données
        connectDB();

        // Chargement des médecins
        loadMedecins();

        // Configurer le DatePicker
        configureDatePicker();

        // Effacer les messages d'erreur lorsqu'on modifie les champs
        date.valueProperty().addListener((obs, oldVal, newVal) -> dateError.setText(""));
        type_rdv.valueProperty().addListener((obs, oldVal, newVal) -> typeError.setText(""));
        medecin.valueProperty().addListener((obs, oldVal, newVal) -> {
            medecinError.setText("");
            if (newVal != null && !newVal.isEmpty()) {
                updateMedecinSelection();
            }
        });
        cause.textProperty().addListener((obs, oldVal, newVal) -> causeError.setText(""));
    }

    private void configureDatePicker() {
        date.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(DatePicker param) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item != null && !empty && currentMedecinId != -1) {
                            int count = rdvCountByDate.getOrDefault(item, 0);

                            if (count >= 3) {
                                setStyle("-fx-background-color: #ffcccc;"); // Rouge
                            } else if (count == 2) {
                                setStyle("-fx-background-color: #ffebcc;"); // Orange
                            } else if (count <= 1) {
                                setStyle("-fx-background-color: #ccffcc;"); // Vert
                            }

                            setTooltip(new Tooltip(count + " consultation(s) ce jour"));

                            // Désactiver les dates passées
                            if (item.isBefore(LocalDate.now())) {
                                setDisable(true);
                                setStyle("-fx-background-color: #eeeeee;");
                            }
                        }
                    }
                };
            }
        });
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

        String query = "SELECT date, COUNT(*) as count FROM rendez_vous WHERE medecin_id = ? GROUP BY date";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, currentMedecinId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                LocalDate date = rs.getDate("date").toLocalDate();
                int count = rs.getInt("count");
                rdvCountByDate.put(date, count);
            }

            // Rafraîchir le DatePicker
            date.setValue(null);
            date.show();
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
        String query = "SELECT nom, prenom FROM user WHERE roles LIKE '%MEDECIN%'";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String nomComplet = rs.getString("prenom") + " " + rs.getString("nom");
                medecinsList.add(nomComplet);
            }
            medecin.setItems(medecinsList);

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger la liste des médecins");
            e.printStackTrace();
        }
    }

    private void loadRendezVousData() {
        try {
            // Récupérer le nom du médecin
            String medecinQuery = "SELECT prenom, nom FROM user WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(medecinQuery)) {
                ps.setInt(1, rendezVous.getIdMedecin());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String nomMedecin = rs.getString("prenom") + " " + rs.getString("nom");
                    medecin.setValue(nomMedecin);
                    currentMedecinId = rendezVous.getIdMedecin();
                    loadRendezVousForMedecin();
                }
            }

            date.setValue(rendezVous.getDate());
            type_rdv.setValue(rendezVous.getType());
            cause.setText(rendezVous.getCause());

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les données du rendez-vous");
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

                // Mettre à jour le rendez-vous
                rendezVous.setDate(date.getValue());
                rendezVous.setType(type_rdv.getValue());
                rendezVous.setCause(cause.getText());
                rendezVous.setIdMedecin(currentMedecinId);
                rendezVous.setStatut("en_attente"); // Statut par défaut

                // Utilisation de ServiceAddRdv pour mettre à jour
                serviceAddRdv.update(rendezVous);

                modificationValidee = true;

                // Afficher le message de succès
                notificationLabel.setVisible(true);
                notificationLabel.setManaged(true);
                PauseTransition pause = new PauseTransition(Duration.seconds(3));
                pause.setOnFinished(e -> {
                    notificationLabel.setVisible(false);
                    notificationLabel.setManaged(false);
                });
                pause.play();

                // Ajouter une notification locale
                String doctorName = getDoctorName(currentMedecinId);
                String notificationMessage = String.format("Rendez-vous avec Dr %s modifié pour le %s. Veuillez attendre la réponse du médecin.",
                        doctorName, rendezVous.getDate());
                addNotification(notificationMessage);

                // Envoyer la notification au listener
                if (notificationListener != null) {
                    notificationListener.accept(notificationMessage);
                }

                // Fermer la fenêtre
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();

            } catch (Exception e) {
                showAlert("Erreur", "Problème lors de la modification du rendez-vous");
                e.printStackTrace();
            }
        }
    }

    private boolean validerFormulaire() {
        boolean isValid = true;

        // Réinitialiser les messages d'erreur
        dateError.setText("");
        typeError.setText("");
        medecinError.setText("");
        causeError.setText("");

        if (date.getValue() == null) {
            dateError.setText("La date est obligatoire");
            isValid = false;
        } else if (date.getValue().isBefore(LocalDate.now())) {
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

    @FXML
    private void showNotificationHistory() {
        try {
            URL fxmlLocation = getClass().getResource("/tn/esprit/views/notificationHistorique.fxml");
            if (fxmlLocation == null) {
                throw new IOException("Cannot find /tn/esprit/views/notificationHistorique.fxml");
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
            showAlert("Erreur", "Impossible d'ouvrir l'historique des notifications"
            );
            e.printStackTrace();
        }
    }

    private void addNotification(String message) {
        notificationHistory.add(message);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}