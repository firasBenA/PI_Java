package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import tn.esprit.models.Evenement;
import tn.esprit.models.Article;
import tn.esprit.services.ServiceEvenement;
import tn.esprit.services.ServiceArticle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GererController {

    @FXML
    private TextField evenementNomField;
    @FXML
    private TextArea evenementContenueField;
    @FXML
    private TextField evenementTypeField;
    @FXML
    private TextField evenementStatutField;
    @FXML
    private TextField evenementLieuxField;
    @FXML
    private DatePicker evenementDateField;
    @FXML
    private Label evenementErrorLabel;
    @FXML
    private VBox evenementContainer;

    @FXML
    private TextField articleTitreField;
    @FXML
    private TextArea articleContenueField;
    @FXML
    private Button articleImageButton;
    @FXML
    private Label articleImageLabel;
    @FXML
    private ComboBox<Evenement> articleEvenementCombo;
    @FXML
    private Label articleErrorLabel;
    @FXML
    private VBox articleContainer;

    private ServiceEvenement serviceEvenement;
    private ServiceArticle serviceArticle;
    private Evenement selectedEvenement;
    private Article selectedArticle;
    private List<Evenement> tempEvenementList;
    private String uploadedImagePath;

    @FXML
    public void initialize() {
        serviceEvenement = new ServiceEvenement();
        serviceArticle = new ServiceArticle();
        tempEvenementList = new ArrayList<>();
        updateEvenementCombo();
        loadEvenements();
        loadArticles();
        articleEvenementCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !tempEvenementList.contains(newValue)) {
                tempEvenementList.add(newValue);
                articleErrorLabel.setText("Événements sélectionnés : " +
                        String.join(", ", tempEvenementList.stream().map(Evenement::getNom).toList()));
                articleErrorLabel.setStyle("-fx-text-fill: #34495e; -fx-font-family: 'Arial';");
                articleEvenementCombo.getSelectionModel().clearSelection();
            }
        });
        articleImageButton.setOnAction(e -> uploadImage());
    }

    private void uploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(articleImageButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Path targetDir = Paths.get("src/main/resources/ImagesArticles");
                Files.createDirectories(targetDir);
                String fileName = UUID.randomUUID() + "_" + selectedFile.getName();
                Path targetPath = targetDir.resolve(fileName);
                Files.copy(selectedFile.toPath(), targetPath);
                uploadedImagePath = "/ImagesArticles/" + fileName;
                articleImageLabel.setText("Image: " + selectedFile.getName());
                articleImageLabel.setStyle("-fx-text-fill: #34495e; -fx-font-family: 'Arial';");
            } catch (IOException ex) {
                showArticleError("Erreur lors du téléchargement de l'image : " + ex.getMessage());
            }
        }
    }

    private void loadEvenements() {
        evenementContainer.getChildren().clear();
        List<Evenement> evenements = serviceEvenement.getAll();
        for (Evenement evenement : evenements) {
            HBox card = createEvenementCard(evenement);
            evenementContainer.getChildren().add(card);
        }
        updateEvenementCombo();
    }

    private HBox createEvenementCard(Evenement evenement) {
        HBox card = new HBox(15);
        card.setUserData(evenement);
        card.setOnMouseClicked(event -> selectEvenement(evenement));
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 3);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"));
        VBox details = new VBox(8);
        Label nomLabel = new Label("Nom: " + truncate(evenement.getNom(), 30));
        nomLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-family: 'Arial';");
        Label typeLabel = new Label("Type: " + truncate(evenement.getType(), 30));
        typeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e; -fx-font-family: 'Arial';");
        Label statutLabel = new Label("Statut: " + truncate(evenement.getStatut(), 30));
        statutLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e; -fx-font-family: 'Arial';");
        Label lieuLabel = new Label("Lieu: " + truncate(evenement.getLieuxEvent(), 30));
        lieuLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e; -fx-font-family: 'Arial';");
        Label dateLabel = new Label("Date: " + evenement.getDateEvent());
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e; -fx-font-family: 'Arial';");
        details.getChildren().addAll(nomLabel, typeLabel, statutLabel, lieuLabel, dateLabel);
        card.getChildren().add(details);
        return card;
    }

    private void selectEvenement(Evenement evenement) {
        selectedEvenement = evenement;
        evenementNomField.setText(evenement.getNom());
        evenementContenueField.setText(evenement.getContenue());
        evenementTypeField.setText(evenement.getType());
        evenementStatutField.setText(evenement.getStatut());
        evenementLieuxField.setText(evenement.getLieuxEvent());
        evenementDateField.setValue(evenement.getDateEvent());
    }

    @FXML
    private void addEvenement() {
        if (!validateEvenementInputs()) {
            return;
        }
        Evenement evenement = new Evenement();
        evenement.setNom(evenementNomField.getText());
        evenement.setContenue(evenementContenueField.getText());
        evenement.setType(evenementTypeField.getText());
        evenement.setStatut(evenementStatutField.getText());
        evenement.setLieuxEvent(evenementLieuxField.getText());
        evenement.setDateEvent(evenementDateField.getValue());
        serviceEvenement.add(evenement);
        loadEvenements();
        clearEvenementFields();
        showEvenementSuccess("Événement ajouté avec succès !");
    }

    @FXML
    private void updateEvenement() {
        if (selectedEvenement == null) {
            showEvenementError("Veuillez sélectionner un événement à modifier.");
            return;
        }
        if (!validateEvenementInputs()) {
            return;
        }
        selectedEvenement.setNom(evenementNomField.getText());
        selectedEvenement.setContenue(evenementContenueField.getText());
        selectedEvenement.setType(evenementTypeField.getText());
        selectedEvenement.setStatut(evenementStatutField.getText());
        selectedEvenement.setLieuxEvent(evenementLieuxField.getText());
        selectedEvenement.setDateEvent(evenementDateField.getValue());
        serviceEvenement.update(selectedEvenement);
        loadEvenements();
        clearEvenementFields();
        showEvenementSuccess("Événement modifié avec succès !");
    }

    @FXML
    private void deleteEvenement() {
        if (selectedEvenement == null) {
            showEvenementError("Veuillez sélectionner un événement à supprimer.");
            return;
        }
        serviceEvenement.delete(selectedEvenement);
        loadEvenements();
        clearEvenementFields();
        showEvenementSuccess("Événement supprimé avec succès !");
    }

    @FXML
    private void clearEvenementFields() {
        evenementNomField.clear();
        evenementContenueField.clear();
        evenementTypeField.clear();
        evenementStatutField.clear();
        evenementLieuxField.clear();
        evenementDateField.setValue(null);
        selectedEvenement = null;
        evenementErrorLabel.setText("");
    }

    private boolean validateEvenementInputs() {
        if (evenementNomField.getText().isEmpty()) {
            showEvenementError("Le nom ne peut pas être vide.");
            return false;
        }
        if (evenementContenueField.getText().isEmpty()) {
            showEvenementError("Le contenu ne peut pas être vide.");
            return false;
        }
        if (evenementTypeField.getText().isEmpty()) {
            showEvenementError("Le type ne peut pas être vide.");
            return false;
        }
        if (evenementStatutField.getText().isEmpty()) {
            showEvenementError("Le statut ne peut pas être vide.");
            return false;
        }
        if (evenementLieuxField.getText().isEmpty()) {
            showEvenementError("Le lieu ne peut pas être vide.");
            return false;
        }
        if (evenementDateField.getValue() == null) {
            showEvenementError("La date ne peut pas être vide.");
            return false;
        }
        if (!evenementDateField.getValue().isAfter(LocalDate.now())) {
            showEvenementError("Ce champ doit être ultérieur à aujourd'hui.");
            return false;
        }
        return true;
    }

    private void showEvenementError(String message) {
        evenementErrorLabel.setText(message);
        evenementErrorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-family: 'Arial';");
    }

    private void showEvenementSuccess(String message) {
        evenementErrorLabel.setText(message);
        evenementErrorLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-family: 'Arial';");
    }

    private void loadArticles() {
        articleContainer.getChildren().clear();
        List<Article> articles = serviceArticle.getAll();
        for (Article article : articles) {
            HBox card = createArticleCard(article);
            articleContainer.getChildren().add(card);
        }
    }

    private HBox createArticleCard(Article article) {
        HBox card = new HBox(15);
        card.setUserData(article);
        card.setOnMouseClicked(event -> selectArticle(article));
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 3);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"));
        VBox details = new VBox(8);
        String evenementsStr = article.getEvenements().isEmpty() ? "Aucun" :
                String.join(", ", article.getEvenements().stream().map(Evenement::getNom).toList());
        Label titreLabel = new Label("Titre: " + truncate(article.getTitre(), 30));
        titreLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-family: 'Arial';");
        Label evenementsLabel = new Label("Événements: " + truncate(evenementsStr, 30));
        evenementsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e; -fx-font-family: 'Arial';");
        Label imageLabel = new Label("Image: " + truncate(article.getImage(), 30));
        imageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e; -fx-font-family: 'Arial';");
        Label likesLabel = new Label("Likes: " + article.getLikeCount());
        likesLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e; -fx-font-family: 'Arial';");
        details.getChildren().addAll(titreLabel, evenementsLabel, imageLabel, likesLabel);
        card.getChildren().add(details);
        return card;
    }

    private void selectArticle(Article article) {
        selectedArticle = article;
        articleTitreField.setText(article.getTitre());
        articleContenueField.setText(article.getContenue());
        uploadedImagePath = article.getImage();
        articleImageLabel.setText("Image: " + (article.getImage().isEmpty() ? "Aucune" : Paths.get(article.getImage()).getFileName().toString()));
        articleImageLabel.setStyle("-fx-text-fill: #34495e; -fx-font-family: 'Arial';");
        tempEvenementList.clear();
        tempEvenementList.addAll(article.getEvenements());
        articleEvenementCombo.getSelectionModel().clearSelection();
        articleErrorLabel.setText("Événements sélectionnés : " +
                String.join(", ", tempEvenementList.stream().map(Evenement::getNom).toList()));
        articleErrorLabel.setStyle("-fx-text-fill: #34495e; -fx-font-family: 'Arial';");
    }

    private void updateEvenementCombo() {
        articleEvenementCombo.getItems().clear();
        List<Evenement> evenements = serviceEvenement.getAll();
        articleEvenementCombo.getItems().addAll(evenements);
        articleEvenementCombo.setCellFactory(param -> new ListCell<Evenement>() {
            @Override
            protected void updateItem(Evenement item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNom());
            }
        });
        articleEvenementCombo.setButtonCell(new ListCell<Evenement>() {
            @Override
            protected void updateItem(Evenement item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNom());
            }
        });
    }

    @FXML
    private void addArticle() {
        if (!validateArticleInputs()) {
            return;
        }
        Article article = new Article();
        article.setTitre(articleTitreField.getText());
        article.setContenue(articleContenueField.getText());
        article.setImage(uploadedImagePath);
        article.setEvenements(new ArrayList<>(tempEvenementList));
        serviceArticle.add(article);
        loadArticles();
        clearArticleFields();
        showArticleSuccess("Article ajouté avec succès !");
    }

    @FXML
    private void updateArticle() {
        if (selectedArticle == null) {
            showArticleError("Veuillez sélectionner un article à modifier.");
            return;
        }
        if (!validateArticleInputs()) {
            return;
        }
        selectedArticle.setTitre(articleTitreField.getText());
        selectedArticle.setContenue(articleContenueField.getText());
        selectedArticle.setImage(uploadedImagePath);
        selectedArticle.setEvenements(new ArrayList<>(tempEvenementList));
        serviceArticle.update(selectedArticle);
        loadArticles();
        clearArticleFields();
        showArticleSuccess("Article modifié avec succès !");
    }

    @FXML
    private void deleteArticle() {
        if (selectedArticle == null) {
            showArticleError("Veuillez sélectionner un article à supprimer.");
            return;
        }
        serviceArticle.delete(selectedArticle);
        loadArticles();
        clearArticleFields();
        showArticleSuccess("Article supprimé avec succès !");
    }

    @FXML
    private void clearArticleFields() {
        articleTitreField.clear();
        articleContenueField.clear();
        uploadedImagePath = null;
        articleImageLabel.setText("Image: Aucune");
        articleImageLabel.setStyle("-fx-text-fill: #34495e; -fx-font-family: 'Arial';");
        tempEvenementList.clear();
        articleEvenementCombo.getSelectionModel().clearSelection();
        articleErrorLabel.setText("Événements sélectionnés : Aucun");
        articleErrorLabel.setStyle("-fx-text-fill: #34495e; -fx-font-family: 'Arial';");
        selectedArticle = null;
    }

    private boolean validateArticleInputs() {
        if (articleTitreField.getText().isEmpty()) {
            showArticleError("Le titre ne peut pas être vide.");
            return false;
        }
        if (articleContenueField.getText().isEmpty()) {
            showArticleError("Le contenu ne peut pas être vide.");
            return false;
        }
        if (uploadedImagePath == null || uploadedImagePath.isEmpty()) {
            showArticleError("Veuillez sélectionner une image.");
            return false;
        }
        if (tempEvenementList.isEmpty()) {
            showArticleError("Veuillez associer au moins un événement.");
            return false;
        }
        return true;
    }

    private void showArticleError(String message) {
        articleErrorLabel.setText(message);
        articleErrorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-family: 'Arial';");
    }

    private void showArticleSuccess(String message) {
        articleErrorLabel.setText(message);
        articleErrorLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-family: 'Arial';");
    }

    private String truncate(String text, int maxLength) {
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}