package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.models.Evenement;
import tn.esprit.models.Article;
import tn.esprit.services.ServiceEvenement;
import tn.esprit.services.ServiceArticle;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private TextField articleImageField;
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
                articleErrorLabel.setStyle("-fx-text-fill: black;");
                articleEvenementCombo.getSelectionModel().clearSelection();
            }
        });
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
        HBox card = new HBox(10);
        card.setUserData(evenement);
        card.setOnMouseClicked(event -> selectEvenement(evenement));
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dcdcdc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #dcdcdc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dcdcdc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"));
        VBox details = new VBox(5);
        details.getChildren().addAll(
                new Label("Nom: " + truncate(evenement.getNom(), 30)),
                new Label("Type: " + truncate(evenement.getType(), 30)),
                new Label("Statut: " + truncate(evenement.getStatut(), 30)),
                new Label("Lieu: " + truncate(evenement.getLieuxEvent(), 30)),
                new Label("Date: " + evenement.getDateEvent())
        );
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
        evenementErrorLabel.setStyle("-fx-text-fill: red;");
    }

    private void showEvenementSuccess(String message) {
        evenementErrorLabel.setText(message);
        evenementErrorLabel.setStyle("-fx-text-fill: green;");
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
        HBox card = new HBox(10);
        card.setUserData(article);
        card.setOnMouseClicked(event -> selectArticle(article));
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dcdcdc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #dcdcdc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dcdcdc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"));
        VBox details = new VBox(5);
        String evenementsStr = article.getEvenements().isEmpty() ? "Aucun" :
                String.join(", ", article.getEvenements().stream().map(Evenement::getNom).toList());
        details.getChildren().addAll(
                new Label("Titre: " + truncate(article.getTitre(), 30)),
                new Label("Événements: " + truncate(evenementsStr, 30)),
                new Label("Image: " + truncate(article.getImage(), 30)),
                new Label("Likes: " + article.getLikeCount())
        );
        card.getChildren().add(details);
        return card;
    }

    private void selectArticle(Article article) {
        selectedArticle = article;
        articleTitreField.setText(article.getTitre());
        articleContenueField.setText(article.getContenue());
        articleImageField.setText(article.getImage());
        tempEvenementList.clear();
        tempEvenementList.addAll(article.getEvenements());
        articleEvenementCombo.getSelectionModel().clearSelection();
        articleErrorLabel.setText("Événements sélectionnés : " +
                String.join(", ", tempEvenementList.stream().map(Evenement::getNom).toList()));
        articleErrorLabel.setStyle("-fx-text-fill: black;");
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
        article.setImage(articleImageField.getText());
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
        selectedArticle.setImage(articleImageField.getText());
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
        articleImageField.clear();
        tempEvenementList.clear();
        articleEvenementCombo.getSelectionModel().clearSelection();
        articleErrorLabel.setText("Événements sélectionnés : Aucun");
        articleErrorLabel.setStyle("-fx-text-fill: black;");
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
        if (articleImageField.getText().isEmpty()) {
            showArticleError("Le chemin de l'image ne peut pas être vide.");
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
        articleErrorLabel.setStyle("-fx-text-fill: red;");
    }

    private void showArticleSuccess(String message) {
        articleErrorLabel.setText(message);
        articleErrorLabel.setStyle("-fx-text-fill: green;");
    }

    private String truncate(String text, int maxLength) {
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}