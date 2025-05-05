package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.models.Reclamation;
import tn.esprit.models.Reponse;
import tn.esprit.services.ServiceReclamation;
import tn.esprit.services.ServiceReponse;
import tn.esprit.utils.BadWordsFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class ListeReclamation {

    @FXML
    private HBox cardPane;

    @FXML
    private TextField TFsujet;

    @FXML
    private TextArea TFdescription;

    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    @FXML
    private Label pageLabel;

    @FXML
    private ComboBox<String> filterEtatComboBox;

    private final ServiceReclamation service = new ServiceReclamation();
    private final ServiceReponse serviceReponse = new ServiceReponse();
    private ObservableList<Reclamation> observableReclamations;
    private Reclamation selectedReclamation;

    private static final int ITEMS_PER_PAGE = 3;
    private int currentPage = 1;
    private int totalPages = 1;

    private static final int MIN_SUJET_LENGTH = 5;
    private static final int MAX_SUJET_LENGTH = 100;
    private static final int MIN_DESCRIPTION_LENGTH = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final Pattern TEXT_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s.,!?éèêëàâäîïôöûüç-]+$");
    private static final int DESCRIPTION_DISPLAY_LIMIT = 110;

    @FXML
    public void initialize() {
        filterEtatComboBox.setItems(FXCollections.observableArrayList("Tous", "En Attente", "Traité"));
        filterEtatComboBox.setValue("Tous");
        filterEtatComboBox.setOnAction(event -> loadReclamations());

        loadReclamations();

        TFsujet.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > MAX_SUJET_LENGTH) {
                TFsujet.setText(oldValue);
                showAlert("Avertissement", "Le sujet ne peut pas dépasser " + MAX_SUJET_LENGTH + " caractères !");
            }
            String badWord = BadWordsFilter.containsBadWord(newValue);
            if (badWord != null) {
                TFsujet.setText(oldValue);
                showAlert("Erreur", "Oops ! Certains mots de votre commentaire ne respectent pas nos règles. Essayez de les reformuler 😊.");
            }
        });

        TFdescription.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > MAX_DESCRIPTION_LENGTH) {
                TFdescription.setText(oldValue);
                showAlert("Avertissement", "La description ne peut pas dépasser " + MAX_DESCRIPTION_LENGTH + " caractères !");
            }
            String badWord = BadWordsFilter.containsBadWord(newValue);
            if (badWord != null) {
                TFdescription.setText(oldValue);
                showAlert("error", "Oops ! Certains mots de votre commentaire ne respectent pas nos règles. Essayez de les reformuler 😊.");
            }
        });

        updateButtonStates();
    }

    private void loadReclamations() {
        String selectedEtat = filterEtatComboBox.getValue();
        List<Reclamation> reclamationList;

        if (selectedEtat == null || selectedEtat.equals("Tous")) {
            reclamationList = service.getAll();
        } else {
            reclamationList = service.getByEtat(selectedEtat);
        }

        observableReclamations = FXCollections.observableArrayList(reclamationList);

        totalPages = (int) Math.ceil((double) observableReclamations.size() / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;

        if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        cardPane.getChildren().clear();

        int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, observableReclamations.size());
        List<Reclamation> currentPageReclamations = observableReclamations.subList(startIndex, endIndex);

        for (Reclamation reclamation : currentPageReclamations) {
            VBox card = createReclamationCard(reclamation);
            cardPane.getChildren().add(card);

            FadeTransition fade = new FadeTransition(Duration.millis(500), card);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();
        }

        pageLabel.setText("Page " + currentPage + " of " + totalPages);
        updateButtonStates();
    }

    private void updateButtonStates() {
        prevButton.setDisable(currentPage <= 1);
        nextButton.setDisable(currentPage >= totalPages);
    }

    @FXML
    public void goToPreviousPage(ActionEvent event) {
        if (currentPage > 1) {
            currentPage--;
            loadReclamations();
        }
    }

    @FXML
    public void goToNextPage(ActionEvent event) {
        if (currentPage < totalPages) {
            currentPage++;
            loadReclamations();
        }
    }

    private VBox createReclamationCard(Reclamation reclamation) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(240);
        card.setPrefHeight(320);
        card.setMinHeight(320);

        Region imagePlaceholder = new Region();
        imagePlaceholder.getStyleClass().add("image-placeholder");
        imagePlaceholder.setPrefHeight(75);
        imagePlaceholder.setPrefWidth(75);

        Text sujetTitle = new Text(reclamation.getSujet());
        sujetTitle.getStyleClass().add("card-title");
        sujetTitle.setWrappingWidth(210);

        String description = reclamation.getDescription();
        Text descriptionText = new Text(description.length() > DESCRIPTION_DISPLAY_LIMIT ?
                description.substring(0, DESCRIPTION_DISPLAY_LIMIT) : description);
        descriptionText.getStyleClass().add("card-subtitle");
        descriptionText.setWrappingWidth(210);

        Text dateLabel = new Text("Date: ");
        dateLabel.getStyleClass().add("card-label");
        Text dateText = new Text(reclamation.getDateDebut().toString());
        dateText.getStyleClass().add("card-text");

        Text etatText = new Text(reclamation.getEtat());
        etatText.getStyleClass().add("etat-badge");

        HBox dateBox = new HBox(5, dateLabel, dateText);
        HBox etatBox = new HBox(etatText);
        etatBox.getStyleClass().add("etat-container");

        VBox detailsBox = new VBox(3);
        detailsBox.getStyleClass().add("card-details");
        detailsBox.getChildren().addAll(dateBox, etatBox);

        VBox descriptionContainer = new VBox(5);
        descriptionContainer.getChildren().add(descriptionText);

        HBox buttonsContainer = new HBox(10);
        if (description.length() > DESCRIPTION_DISPLAY_LIMIT) {
            Button seeMoreButton = new Button("Voir plus");
            seeMoreButton.getStyleClass().add("button-see-more");
            seeMoreButton.setOnAction(event -> showFullDescription(reclamation.getDescription()));
            buttonsContainer.getChildren().add(seeMoreButton);
        }

        if (reclamation.getEtat().equals("Traité")) {
            List<Reponse> responses = serviceReponse.getByReclamationId(reclamation.getId());
            if (!responses.isEmpty()) {
                Button seeResponseButton = new Button("Voir Réponse");
                seeResponseButton.getStyleClass().add("button-see-more");
                seeResponseButton.setOnAction(event -> showResponseDialog(reclamation, responses.get(0)));
                buttonsContainer.getChildren().add(seeResponseButton);
            }
        }

        descriptionContainer.getChildren().add(buttonsContainer);

        card.getChildren().addAll(imagePlaceholder, sujetTitle, descriptionContainer, detailsBox);

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

    private void showFullDescription(String fullDescription) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Description complète");

        VBox dialogContent = new VBox(15);
        dialogContent.setPadding(new javafx.geometry.Insets(20));
        dialogContent.getStyleClass().add("modal-dialog");

        Text dialogTitle = new Text("Détails de la Réclamation");
        dialogTitle.getStyleClass().add("modal-title");

        Text descriptionText = new Text(fullDescription);
        descriptionText.getStyleClass().add("modal-text");
        descriptionText.setWrappingWidth(400);

        Button closeButton = new Button("Fermer");
        closeButton.getStyleClass().add("modal-close-button");
        closeButton.setOnAction(event -> dialog.close());

        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);
        buttonContainer.getChildren().add(closeButton);

        dialogContent.getChildren().addAll(dialogTitle, descriptionText, buttonContainer);

        Scene dialogScene = new Scene(dialogContent, 500, 350);
        java.net.URL cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) {
            dialogScene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Error: CSS file not found for modal dialog at /tn/esprit/styles/styles.css");
        }

        FadeTransition fade = new FadeTransition(Duration.millis(300), dialogContent);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();

        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void showResponseDialog(Reclamation reclamation, Reponse response) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Détails de la Réponse");

        VBox dialogContent = new VBox(15);
        dialogContent.setPadding(new javafx.geometry.Insets(20));
        dialogContent.getStyleClass().add("modal-dialog");

        Text dialogTitle = new Text("Réponse à la Réclamation");
        dialogTitle.getStyleClass().add("modal-title");

        Text contentLabel = new Text("Contenu :");
        contentLabel.getStyleClass().add("modal-label");
        Text contentText = new Text(response.getContenu());
        contentText.getStyleClass().add("modal-text");
        contentText.setWrappingWidth(400);

        Text dateLabel = new Text("Date :");
        dateLabel.getStyleClass().add("modal-label");
        Text dateText = new Text(response.getDateReponse().toString());
        dateText.getStyleClass().add("modal-text");

        HBox ratingBox = new HBox(5);
        ratingBox.setAlignment(javafx.geometry.Pos.CENTER);
        Text ratingLabel = new Text("Note :");
        ratingLabel.getStyleClass().add("modal-label");

        if (response.getRating() == null) {
            Label[] stars = new Label[5];
            for (int i = 0; i < 5; i++) {
                final int starIndex = i; // Final variable for lambda expressions
                final int ratingValue = i + 1;
                stars[i] = new Label("☆");
                stars[i].getStyleClass().add("star");
                stars[i].setOnMouseClicked(event -> {
                    submitRating(reclamation, response, ratingValue);
                    dialog.close();
                    loadReclamations();
                });
                // Handle hover to fill stars up to the current one
                stars[i].setOnMouseEntered(event -> {
                    for (int j = 0; j <= starIndex; j++) {
                        stars[j].setText("★");
                        stars[j].getStyleClass().add("star-filled");
                    }
                });
                stars[i].setOnMouseExited(event -> {
                    for (int j = 0; j <= starIndex; j++) {
                        stars[j].setText("☆");
                        stars[j].getStyleClass().remove("star-filled");
                    }
                });
                ratingBox.getChildren().add(stars[i]);
            }
        } else {
            Text ratingText = new Text("★".repeat(response.getRating()) + "☆".repeat(5 - response.getRating()));
            ratingText.getStyleClass().add("rating-text");
            ratingBox.getChildren().add(ratingText);
        }

        VBox ratingContainer = new VBox(5, ratingLabel, ratingBox);

        Button closeButton = new Button("Fermer");
        closeButton.getStyleClass().add("modal-close-button");
        closeButton.setOnAction(event -> dialog.close());

        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);
        buttonContainer.getChildren().add(closeButton);

        dialogContent.getChildren().addAll(dialogTitle, contentLabel, contentText, dateLabel, dateText, ratingContainer, buttonContainer);

        Scene dialogScene = new Scene(dialogContent, 500, 450);
        java.net.URL cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) {
            dialogScene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Error: CSS file not found for modal dialog at /tn/esprit/styles/styles.css");
        }

        FadeTransition fade = new FadeTransition(Duration.millis(300), dialogContent);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();

        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }
    private void submitRating(Reclamation reclamation, Reponse response, int rating) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmer la note");
        confirmationAlert.setHeaderText("Soumettre la note pour la réponse");
        confirmationAlert.setContentText("Voulez-vous attribuer une note de " + rating + " étoile(s) à cette réponse ?\n\n" +
                "Sujet: " + reclamation.getSujet());

        java.net.URL cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) {
            confirmationAlert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            confirmationAlert.getDialogPane().getStyleClass().add("sweet-alert");
        }

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                response.setRating(rating);
                serviceReponse.update(response);
                showAlert("Succès", "Note de " + rating + " étoile(s) enregistrée avec succès !");
            } catch (Exception e) {
                showAlert("Erreur", "Échec de l'enregistrement de la note : " + e.getMessage());
            }
        }
    }

    @FXML
    public void Save(ActionEvent actionEvent) {
        try {
            String sujet = TFsujet.getText().trim();
            String description = TFdescription.getText().trim();
            LocalDate date = LocalDate.now();

            if (sujet.isEmpty() || description.isEmpty()) {
                showAlert("Erreur", "Les champs Sujet et Description doivent être remplis !");
                return;
            }

            if (sujet.length() < MIN_SUJET_LENGTH) {
                showAlert("Erreur", "Le sujet doit contenir au moins " + MIN_SUJET_LENGTH + " caractères !");
                return;
            }
            if (sujet.length() > MAX_SUJET_LENGTH) {
                showAlert("Erreur", "Le sujet ne peut pas dépasser " + MAX_SUJET_LENGTH + " caractères !");
                return;
            }

            if (description.length() < MIN_DESCRIPTION_LENGTH) {
                showAlert("Erreur", "La description doit contenir au moins " + MIN_DESCRIPTION_LENGTH + " caractères !");
                return;
            }
            if (description.length() > MAX_DESCRIPTION_LENGTH) {
                showAlert("Erreur", "La description ne peut pas dépasser " + MAX_DESCRIPTION_LENGTH + " caractères !");
                return;
            }

            if (!TEXT_PATTERN.matcher(sujet).matches()) {
                showAlert("Erreur", "Le sujet ne peut contenir que des lettres, chiffres, espaces et ponctuations de base (.,!?éèêëàâäîïôöûüç-) !");
                return;
            }
            if (!TEXT_PATTERN.matcher(description).matches()) {
                showAlert("Erreur", "La description ne peut contenir que des lettres, chiffres, espaces et ponctuations de base (.,!?éèêëàâäîïôöûüç-) !");
                return;
            }

            if (isSujetDuplicate(sujet, -1)) {
                showAlert("Erreur", "Ce sujet existe déjà ! Veuillez choisir un sujet unique.");
                return;
            }
            String badWord = BadWordsFilter.containsBadWord(sujet);
            if (badWord != null) {
                showAlert("error", "Oops ! Certains mots de votre commentaire ne respectent pas nos règles. Essayez de les reformuler 😊.");
                return;
            }
            badWord = BadWordsFilter.containsBadWord(description);
            if (badWord != null) {
                showAlert("error", "Oops ! Certains mots de votre commentaire ne respectent pas nos règles. Essayez de les reformuler 😊.");
                return;
            }

            Reclamation r = new Reclamation(sujet, description, date, null, 1);
            service.add(r);
            showAlert("Succès", "Réclamation ajoutée avec la date : " + date.toString() + " et l'état : " + r.getEtat() + " !");
            clearFields();
            loadReclamations();
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

            if (sujet.isEmpty() || description.isEmpty()) {
                showAlert("Erreur", "Les champs Sujet et Description doivent être remplis !");
                return;
            }

            if (sujet.length() < MIN_SUJET_LENGTH) {
                showAlert("Erreur", "Le sujet doit contenir au moins " + MIN_SUJET_LENGTH + " caractères !");
                return;
            }
            if (sujet.length() > MAX_SUJET_LENGTH) {
                showAlert("Erreur", "Le sujet ne peut pas dépasser " + MAX_SUJET_LENGTH + " caractères !");
                return;
            }

            if (description.length() < MIN_DESCRIPTION_LENGTH) {
                showAlert("Erreur", "La description doit contenir au moins " + MIN_DESCRIPTION_LENGTH + " caractères !");
                return;
            }
            if (description.length() > MAX_DESCRIPTION_LENGTH) {
                showAlert("Erreur", "La description ne peut pas dépasser " + MAX_DESCRIPTION_LENGTH + " caractères !");
                return;
            }

            if (!TEXT_PATTERN.matcher(sujet).matches()) {
                showAlert("Erreur", "Le sujet ne peut contenir que des lettres, chiffres, espaces et ponctuations de base (.,!?éèêëàâäîïôöûüç-) !");
                return;
            }
            if (!TEXT_PATTERN.matcher(description).matches()) {
                showAlert("Erreur", "La description ne peut contenir que des lettres, chiffres, espaces et ponctuations de base (.,!?éèêëàâäîïôöûüç-) !");
                return;
            }

            if (isSujetDuplicate(sujet, id)) {
                showAlert("Erreur", "Ce sujet existe déjà ! Veuillez choisir un sujet unique.");
                return;
            }
            String badWord = BadWordsFilter.containsBadWord(sujet);
            if (badWord != null) {
                showAlert("error", "Oops ! Certains mots de votre commentaire ne respectent pas nos règles. Essayez de les reformuler 😊.");
                return;
            }
            badWord = BadWordsFilter.containsBadWord(description);
            if (badWord != null) {
                showAlert("error", "Oops ! Certains mots de votre commentaire ne respectent pas nos règles. Essayez de les reformuler 😊.");
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
        java.net.URL cssUrl = getClass().getResource("/styles.css");
        if (cssUrl == null) {
            System.err.println("Error: CSS file not found at /tn/esprit/styles/styles.css");
        } else {
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