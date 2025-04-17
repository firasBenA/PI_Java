package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import tn.esprit.models.RendeVous;
import tn.esprit.services.ServiceAddRdv;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
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

    private Connection connection;
    private final ServiceAddRdv serviceAddRdv = new ServiceAddRdv();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialisation des types de rendez-vous
        type_rdv.setItems(FXCollections.observableArrayList("consultation", "suivi", "urgence"));

        // Connexion à la base de données
        connectDB();

        // Chargement des médecins
        loadMedecins();

        // Effacer les messages d'erreur lorsqu'on modifie les champs
        date.valueProperty().addListener((obs, oldVal, newVal) -> dateError.setText(""));
        type_rdv.valueProperty().addListener((obs, oldVal, newVal) -> typeError.setText(""));
        medecin.valueProperty().addListener((obs, oldVal, newVal) -> medecinError.setText(""));
        cause.textProperty().addListener((obs, oldVal, newVal) -> causeError.setText(""));
    }

    private void connectDB() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ehealth_database", "root", "");
            System.out.println("Connexion réussie !");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données : " + e.getMessage());
            showAlert("Erreur", "Impossible de se connecter à la base de données");
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
            System.err.println("Erreur lors du chargement des médecins : " + e.getMessage());
            showAlert("Erreur", "Impossible de charger la liste des médecins");
        }
    }

    @FXML
    void save(ActionEvent event) {
        if (validerFormulaire()) {
            try {
                // Extraction de l'ID du médecin
                String[] nomPrenom = medecin.getValue().split(" ");
                if (nomPrenom.length < 2) {
                    medecinError.setText("Format du médecin invalide");
                    return;
                }

                String prenom = nomPrenom[0];
                String nom = nomPrenom[1];
                int idMedecin = -1;

                String medecinQuery = "SELECT id FROM user WHERE nom = ? AND prenom = ?";
                try (PreparedStatement psMedecin = connection.prepareStatement(medecinQuery)) {
                    psMedecin.setString(1, nom);
                    psMedecin.setString(2, prenom);
                    ResultSet rs = psMedecin.executeQuery();
                    if (rs.next()) {
                        idMedecin = rs.getInt("id");
                    } else {
                        medecinError.setText("Médecin non trouvé");
                        return;
                    }
                }

                // Création et sauvegarde du rendez-vous
                RendeVous rdv = new RendeVous();
                rdv.setDate(date.getValue());
                rdv.setType(type_rdv.getValue());
                rdv.setCause(cause.getText());
                rdv.setIdMedecin(idMedecin);
                rdv.setIdPatient(1); // À remplacer par l'ID du patient connecté
                rdv.setStatut("en_attente"); // Statut par défaut

                // Utilisation de ServiceAddRdv pour ajouter le rendez-vous
                serviceAddRdv.add(rdv);

                showAlert("Succès", "Rendez-vous enregistré avec succès");
                clearFields();

            } catch (SQLException e) {
                System.err.println("Erreur lors de l'ajout du rendez-vous : " + e.getMessage());
                showAlert("Erreur", "Problème lors de l'enregistrement du rendez-vous");
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
        cause.clear();
        medecin.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setRendezVousToEdit(RendeVous rdv) {
        try {
            // Récupérer le nom du médecin
            String medecinQuery = "SELECT prenom, nom FROM user WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(medecinQuery)) {
                ps.setInt(1, rdv.getIdMedecin());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String nomMedecin = rs.getString("prenom") + " " + rs.getString("nom");
                    medecin.setValue(nomMedecin);
                }
            }

            date.setValue(rdv.getDate());
            type_rdv.setValue(rdv.getType());
            cause.setText(rdv.getCause());

        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement du médecin : " + e.getMessage());
        }
    }
}