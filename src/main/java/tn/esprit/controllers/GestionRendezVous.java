package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;
import tn.esprit.models.RendeVous;
import tn.esprit.services.ServiceAddRdv;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class GestionRendezVous implements Initializable {

    @FXML private DatePicker date;
    @FXML private ComboBox<String> type_rdv;
    @FXML private ComboBox<String> medecin;
    @FXML private TextArea cause;

    // Labels pour les messages d'erreur
    @FXML private Label dateError;
    @FXML private Label typeError;
    @FXML private Label medecinError;
    @FXML private Label causeError;

    private final ServiceAddRdv serviceRendezVous = new ServiceAddRdv();
    private Map<LocalDate, Integer> rdvCountByDate = new HashMap<>();
    private int currentMedecinId = -1;
    private final int patientId = 1; // Replace with actual logged-in patient ID

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialisation des types de rendez-vous avec les valeurs de l'enum
        type_rdv.setItems(FXCollections.observableArrayList(
                RendeVous.TypeRendezVous.CONSULTATION.name(),
                RendeVous.TypeRendezVous.SUIVI.name(),
                RendeVous.TypeRendezVous.URGENCE.name()
        ));

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
            try (Connection connection = serviceRendezVous.getConnection();
                 PreparedStatement psMedecin = connection.prepareStatement(medecinQuery)) {
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
            System.err.println("Erreur lors de la récupération de l'ID du médecin : " + e.getMessage());
            showAlert("Erreur", "Problème lors de la sélection du médecin");
        }
    }

    private void loadRendezVousForMedecin() {
        rdvCountByDate.clear();

        if (currentMedecinId == -1) return;

        String query = "SELECT date, COUNT(*) as count FROM rendez_vous WHERE medecin_id = ? GROUP BY date";

        try (Connection connection = serviceRendezVous.getConnection();
             PreparedStatement pst = connection.prepareStatement(query)) {
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
            System.err.println("Erreur lors du chargement des rendez-vous du médecin : " + e.getMessage());
        }
    }

    private void loadMedecins() {
        ObservableList<String> medecinsList = FXCollections.observableArrayList();
        String query = "SELECT nom, prenom FROM user WHERE roles LIKE '%MEDECIN%'";

        try (Connection connection = serviceRendezVous.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String nomComplet = rs.getString("prenom") + " " + rs.getString("nom");
                medecinsList.add(nomComplet);
            }
            medecin.setItems(medecinsList);

        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des médecins : " + e.getMessage());
            showAlert("Erreur", "Impossible de charger la liste des médecins");
        }
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

                // Vérifier la limite de rendez-vous (3 par jour)
                LocalDate selectedDate = date.getValue();
                int rdvCount = rdvCountByDate.getOrDefault(selectedDate, 0);
                if (rdvCount >= 3) {
                    showAlert("Erreur", "Le médecin a déjà 3 rendez-vous ce jour. Veuillez choisir une autre date.");
                    return;
                }

                // Création et sauvegarde du rendez-vous
                RendeVous rdv = new RendeVous();
                rdv.setDate(selectedDate);
                rdv.setType(type_rdv.getValue());
                rdv.setCause(cause.getText());
                rdv.setIdMedecin(currentMedecinId);
                rdv.setIdPatient(patientId);
                rdv.setStatut(RendeVous.StatutRendezVous.EN_ATTENTE.name());

                // Utilisation de ServiceRendezVous pour ajouter le rendez-vous
                serviceRendezVous.add(rdv);

                showAlert("Succès", "Rendez-vous enregistré avec succès");
                showDesktopNotification("Rendez-vous Ajouté",
                        "Votre rendez-vous a été ajouté.\nDate: " + selectedDate +
                                "\nType: " + type_rdv.getValue() +
                                "\nCause: " + cause.getText());
                clearFields();

                // Mettre à jour le calendrier après l'ajout
                loadRendezVousForMedecin();
            } catch (Exception e) {
                System.err.println("Erreur lors de l'ajout du rendez-vous : " + e.getMessage());
                showAlert("Erreur", "Problème lors de l'enregistrement du rendez-vous : " + e.getMessage());
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

    private void clearFields() {
        date.setValue(null);
        type_rdv.getSelectionModel().clearSelection();
        medecin.getSelectionModel().clearSelection();
        cause.clear();
        currentMedecinId = -1;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showDesktopNotification(String title, String content) {
        Alert notification = new Alert(Alert.AlertType.INFORMATION);
        notification.setTitle(title);
        notification.setHeaderText(null);
        notification.setContentText(content);
        notification.showAndWait();
    }

    public void setRendezVousToEdit(RendeVous rdv) {
        try {
            // Récupérer le nom du médecin
            String medecinQuery = "SELECT prenom, nom FROM user WHERE id = ?";
            try (Connection connection = serviceRendezVous.getConnection();
                 PreparedStatement ps = connection.prepareStatement(medecinQuery)) {
                ps.setInt(1, rdv.getIdMedecin());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String nomMedecin = rs.getString("prenom") + " " + rs.getString("nom");
                    medecin.setValue(nomMedecin);
                    currentMedecinId = rdv.getIdMedecin();
                    loadRendezVousForMedecin();
                }
            }

            date.setValue(rdv.getDate());
            type_rdv.setValue(rdv.getType());
            cause.setText(rdv.getCause());

        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement du médecin : " + e.getMessage());
            showAlert("Erreur", "Problème lors du chargement du rendez-vous à modifier");
        }
    }
}