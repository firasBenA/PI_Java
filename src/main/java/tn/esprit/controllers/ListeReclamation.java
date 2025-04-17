package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import tn.esprit.models.Reclamation;
import tn.esprit.services.ServiceReclamation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class ListeReclamation {

    @FXML
    private HBox cardPane;

    @FXML
    private TextField TFsujet, TFdescription;

    private final ServiceReclamation service = new ServiceReclamation();
    private ObservableList<Reclamation> observableReclamations;
    private Reclamation selectedReclamation;

    // Validation constants
    private static final int MIN_SUJET_LENGTH = 5;
    private static final int MAX_SUJET_LENGTH = 100;
    private static final int MIN_DESCRIPTION_LENGTH = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final Pattern TEXT_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s.,!?éèêëàâäîïôöûüç-]+$");

    @FXML
    public void initialize() {
        loadReclamations();

        // Real-time validation for TFsujet
        TFsujet.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > MAX_SUJET_LENGTH) {
                TFsujet.setText(oldValue);
                showAlert("Avertissement", "Le sujet ne peut pas dépasser " + MAX_SUJET_LENGTH + " caractères !");
            }
        });

        // Real-time validation for TFdescription
        TFdescription.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > MAX_DESCRIPTION_LENGTH) {
                TFdescription.setText(oldValue);
                showAlert("Avertissement", "La description ne peut pas dépasser " + MAX_DESCRIPTION_LENGTH + " caractères !");
            }
        });
    }

    private void loadReclamations() {
        List<Reclamation> reclamationList = service.getAll();
        observableReclamations = FXCollections.observableArrayList(reclamationList);
        cardPane.getChildren().clear();

        for (Reclamation reclamation : observableReclamations) {
            VBox card = createReclamationCard(reclamation);
            cardPane.getChildren().add(card);

            // Add fade-in animation
            FadeTransition fade = new FadeTransition(Duration.millis(500), card);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();
        }
    }

    private VBox createReclamationCard(Reclamation reclamation) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(220);
        card.setPrefHeight(220);
        card.setMinHeight(220);

        // Circular placeholder for an image/icon
        Region imagePlaceholder = new Region();
        imagePlaceholder.getStyleClass().add("image-placeholder");
        imagePlaceholder.setPrefHeight(70);
        imagePlaceholder.setPrefWidth(70);

        // Title (Sujet)
        Text sujetTitle = new Text(reclamation.getSujet());
        sujetTitle.getStyleClass().add("card-title");
        sujetTitle.setWrappingWidth(200);

        // Subtitle (Description)
        Text descriptionText = new Text(reclamation.getDescription());
        descriptionText.getStyleClass().add("card-subtitle");
        descriptionText.setWrappingWidth(200);

        // Details (Date only, ID removed)
        Text dateLabel = new Text("Date: ");
        dateLabel.getStyleClass().add("card-label");
        Text dateText = new Text(reclamation.getDateDebut().toString());
        dateText.getStyleClass().add("card-text");

        // État badge
        Text etatText = new Text(reclamation.getEtat());
        etatText.getStyleClass().add("etat-badge");

        HBox dateBox = new HBox(5, dateLabel, dateText);
        HBox etatBox = new HBox(etatText);
        etatBox.getStyleClass().add("etat-container");

        VBox detailsBox = new VBox(3, dateBox, etatBox);
        detailsBox.getStyleClass().add("card-details");

        card.getChildren().addAll(imagePlaceholder, sujetTitle, descriptionText, detailsBox);

        card.setOnMouseClicked(event -> {
            if (selectedReclamation != null) {
                cardPane.getChildren().stream()
                        .filter(node -> node instanceof VBox)
                        .map(node -> (VBox) node)
                        .forEach(vbox -> vbox.getStyleClass().remove("selected"));
            }
            selectedReclamation = reclamation;
            card.getStyleClass().add("selected");

            TFsujet.setText(reclamation.getSujet());
            TFdescription.setText(reclamation.getDescription());
        });

        return card;
    }

    @FXML
    public void Save(ActionEvent actionEvent) {
        try {
            String sujet = TFsujet.getText().trim();
            String description = TFdescription.getText().trim();
            LocalDate date = LocalDate.now();

            // Validate fields
            if (sujet.isEmpty() || description.isEmpty()) {
                showAlert("Erreur", "Les champs Sujet et Description doivent être remplis !");
                return;
            }

            // Validate sujet length
            if (sujet.length() < MIN_SUJET_LENGTH) {
                showAlert("Erreur", "Le sujet doit contenir au moins " + MIN_SUJET_LENGTH + " caractères !");
                return;
            }
            if (sujet.length() > MAX_SUJET_LENGTH) {
                showAlert("Erreur", "Le sujet ne peut pas dépasser " + MAX_SUJET_LENGTH + " caractères !");
                return;
            }

            // Validate description length
            if (description.length() < MIN_DESCRIPTION_LENGTH) {
                showAlert("Erreur", "La description doit contenir au moins " + MIN_DESCRIPTION_LENGTH + " caractères !");
                return;
            }
            if (description.length() > MAX_DESCRIPTION_LENGTH) {
                showAlert("Erreur", "La description ne peut pas dépasser " + MAX_DESCRIPTION_LENGTH + " caractères !");
                return;
            }

            // Validate allowed characters
            if (!TEXT_PATTERN.matcher(sujet).matches()) {
                showAlert("Erreur", "Le sujet ne peut contenir que des lettres, chiffres, espaces et ponctuations de base (.,!?éèêëàâäîïôöûüç-) !");
                return;
            }
            if (!TEXT_PATTERN.matcher(description).matches()) {
                showAlert("Erreur", "La description ne peut contenir que des lettres, chiffres, espaces et ponctuations de base (.,!?éèêëàâäîïôöûüç-) !");
                return;
            }

            // Optional: Check for unique sujet
            if (isSujetDuplicate(sujet, -1)) {
                showAlert("Erreur", "Ce sujet existe déjà ! Veuillez choisir un sujet unique.");
                return;
            }

            // Log the date to confirm it's not null
            System.out.println("Date before creating Reclamation: " + date);

            // etat will be set to "EnAttente" in the Reclamation constructor
            Reclamation r = new Reclamation(sujet, description, date, null, 1); // 1 = user_id fictif

            // Log the dateDebut and etat to confirm they're set
            System.out.println("Reclamation dateDebut after creation: " + r.getDateDebut());
            System.out.println("Reclamation etat after creation: " + r.getEtat());

            // Show confirmation dialog
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirmation");
            confirmationAlert.setHeaderText("Ajouter une réclamation");
            confirmationAlert.setContentText("Voulez-vous vraiment ajouter cette réclamation ?\n\n" +
                    "Sujet : " + sujet + "\n" +
                    "Description : " + description + "\n" +
                    "Date : " + date + "\n" +
                    "État : EnAttente");

            // Style the dialog
            DialogPane dialogPane = confirmationAlert.getDialogPane();
            System.out.println("Loading stylesheet for confirmation dialog...");
            java.net.URL cssUrl = getClass().getResource("/tn/esprit/styles/styles.css");
            if (cssUrl == null) {
                System.out.println("Error: CSS file not found at /tn/esprit/styles/styles.css");
            } else {
                System.out.println("CSS file found: " + cssUrl.toExternalForm());
                dialogPane.getStylesheets().add(cssUrl.toExternalForm());
                dialogPane.getStyleClass().add("sweet-alert");
            }

            System.out.println("Showing confirmation dialog...");
            Optional<ButtonType> result = confirmationAlert.showAndWait();
            System.out.println("Confirmation dialog result: " + result);

            if (result.isPresent() && result.get() == ButtonType.OK) {
                // User confirmed, proceed with adding the reclamation
                System.out.println("User confirmed, adding reclamation...");
                service.add(r);
                System.out.println("Reclamation added, showing success alert...");
                showAlert("Succès", "Réclamation ajoutée avec la date : " + date.toString() + " et l'état : " + r.getEtat() + " !");
                clearFields();
                loadReclamations();
            } else {
                // User canceled
                System.out.println("Ajout de la réclamation annulé.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Problème : " + e.getMessage());
        }
    }

    @FXML
    public void Update(ActionEvent actionEvent) {
        try {
            if (selectedReclamation == null) {
                showAlert("Erreur", "Veuillez sélectionner une réclamation à modifier !");
                return;
            }

            int id = selectedReclamation.getId();
            String sujet = TFsujet.getText().trim();
            String description = TFdescription.getText().trim();
            Reclamation existingReclamation = service.getById(id);
            if (existingReclamation == null) {
                showAlert("Erreur", "Réclamation non trouvée !");
                return;
            }

            // Validate fields
            if (sujet.isEmpty() || description.isEmpty()) {
                showAlert("Erreur", "Les champs Sujet et Description doivent être remplis !");
                return;
            }

            // Validate sujet length
            if (sujet.length() < MIN_SUJET_LENGTH) {
                showAlert("Erreur", "Le sujet doit contenir au moins " + MIN_SUJET_LENGTH + " caractères !");
                return;
            }
            if (sujet.length() > MAX_SUJET_LENGTH) {
                showAlert("Erreur", "Le sujet ne peut pas dépasser " + MAX_SUJET_LENGTH + " caractères !");
                return;
            }

            // Validate description length
            if (description.length() < MIN_DESCRIPTION_LENGTH) {
                showAlert("Erreur", "La description doit contenir au moins " + MIN_DESCRIPTION_LENGTH + " caractères !");
                return;
            }
            if (description.length() > MAX_DESCRIPTION_LENGTH) {
                showAlert("Erreur", "La description ne peut pas dépasser " + MAX_DESCRIPTION_LENGTH + " caractères !");
                return;
            }

            // Validate allowed characters
            if (!TEXT_PATTERN.matcher(sujet).matches()) {
                showAlert("Erreur", "Le sujet ne peut contenir que des lettres, chiffres, espaces et ponctuations de base (.,!?éèêëàâäîïôöûüç-) !");
                return;
            }
            if (!TEXT_PATTERN.matcher(description).matches()) {
                showAlert("Erreur", "La description ne peut contenir que des lettres, chiffres, espaces et ponctuations de base (.,!?éèêëàâäîïôöûüç-) !");
                return;
            }

            // Optional: Check for unique sujet (excluding the current reclamation)
            if (isSujetDuplicate(sujet, id)) {
                showAlert("Erreur", "Ce sujet existe déjà ! Veuillez choisir un sujet unique.");
                return;
            }

            LocalDate date = existingReclamation.getDateDebut();
            String etat = existingReclamation.getEtat();

            Reclamation r = new Reclamation(id, sujet, description, date, etat, 1);
            service.update(r);
            showAlert("Succès", "Réclamation modifiée !");
            clearFields();
            loadReclamations();
        } catch (Exception e) {
            showAlert("Erreur", "Problème : " + e.getMessage());
        }
    }

    @FXML
    public void Delete(ActionEvent actionEvent) {
        try {
            if (selectedReclamation == null) {
                showAlert("Erreur", "Veuillez sélectionner une réclamation à supprimer !");
                return;
            }

            int id = selectedReclamation.getId();
            Reclamation r = new Reclamation();
            r.setId(id);
            service.delete(r);
            showAlert("Succès", "Réclamation supprimée !");
            clearFields();
            loadReclamations();
        } catch (Exception e) {
            showAlert("Erreur", "Problème : " + e.getMessage());
        }
    }

    private boolean isSujetDuplicate(String sujet, int currentId) {
        for (Reclamation reclamation : observableReclamations) {
            if (reclamation.getSujet().equalsIgnoreCase(sujet) && reclamation.getId() != currentId) {
                return true;
            }
        }
        return false;
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);

        DialogPane dialogPane = alert.getDialogPane();
        System.out.println("Loading stylesheet for alert...");
        java.net.URL cssUrl = getClass().getResource("/tn/esprit/styles/styles.css");
        if (cssUrl == null) {
            System.out.println("Error: CSS file not found at /tn/esprit/styles/styles.css");
        } else {
            System.out.println("CSS file found: " + cssUrl.toExternalForm());
            dialogPane.getStylesheets().add(cssUrl.toExternalForm());
            dialogPane.getStyleClass().add("sweet-alert");
        }

        alert.showAndWait();
    }

    private void clearFields() {
        TFsujet.clear();
        TFdescription.clear();
        if (selectedReclamation != null) {
            cardPane.getChildren().stream()
                    .filter(node -> node instanceof VBox)
                    .map(node -> (VBox) node)
                    .forEach(vbox -> vbox.getStyleClass().remove("selected"));
            selectedReclamation = null;
        }
    }
}