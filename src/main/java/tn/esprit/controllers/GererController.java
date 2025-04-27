package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.models.Evenement;
import tn.esprit.models.Article;
import tn.esprit.services.ServiceEvenement;
import tn.esprit.services.ServiceArticle;

import java.time.LocalDate;
import java.util.List;

public class GererController {

    // Evenement UI elements
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
    private TableView<Evenement> evenementTable;
    @FXML
    private TableColumn<Evenement, Integer> evenementIdColumn;
    @FXML
    private TableColumn<Evenement, String> evenementNomColumn;
    @FXML
    private TableColumn<Evenement, String> evenementContenueColumn;
    @FXML
    private TableColumn<Evenement, String> evenementTypeColumn;
    @FXML
    private TableColumn<Evenement, String> evenementStatutColumn;
    @FXML
    private TableColumn<Evenement, String> evenementLieuxColumn;
    @FXML
    private TableColumn<Evenement, LocalDate> evenementDateColumn;

    // Article UI elements
    @FXML
    private TextField articleTitreField;
    @FXML
    private TextArea articleContenueField;
    @FXML
    private TextField articleImageField;
    @FXML
    private Label articleErrorLabel;
    @FXML
    private TableView<Article> articleTable;
    @FXML
    private TableColumn<Article, Integer> articleIdColumn;
    @FXML
    private TableColumn<Article, String> articleTitreColumn;
    @FXML
    private TableColumn<Article, String> articleContenueColumn;
    @FXML
    private TableColumn<Article, String> articleImageColumn;

    private ServiceEvenement serviceEvenement;
    private ServiceArticle serviceArticle;
    private Evenement selectedEvenement;
    private Article selectedArticle;

    @FXML
    public void initialize() {
        serviceEvenement = new ServiceEvenement();
        serviceArticle = new ServiceArticle();

        // Set up Evenement table columns
        evenementIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        evenementNomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        evenementContenueColumn.setCellValueFactory(new PropertyValueFactory<>("contenue"));
        evenementTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        evenementStatutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        evenementLieuxColumn.setCellValueFactory(new PropertyValueFactory<>("lieuxEvent"));
        evenementDateColumn.setCellValueFactory(new PropertyValueFactory<>("dateEvent"));

        // Set up Article table columns
        articleIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        articleTitreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        articleContenueColumn.setCellValueFactory(new PropertyValueFactory<>("contenue"));
        articleImageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));

        // Load data into tables
        loadEvenements();
        loadArticles();

        // Add listeners for table selections
        evenementTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedEvenement = newSelection;
                evenementNomField.setText(selectedEvenement.getNom());
                evenementContenueField.setText(selectedEvenement.getContenue());
                evenementTypeField.setText(selectedEvenement.getType());
                evenementStatutField.setText(selectedEvenement.getStatut());
                evenementLieuxField.setText(selectedEvenement.getLieuxEvent());
                evenementDateField.setValue(selectedEvenement.getDateEvent());
            }
        });

        articleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedArticle = newSelection;
                articleTitreField.setText(selectedArticle.getTitre());
                articleContenueField.setText(selectedArticle.getContenue());
                articleImageField.setText(selectedArticle.getImage());
            }
        });
    }

    // Evenement CRUD methods
    private void loadEvenements() {
        List<Evenement> evenements = serviceEvenement.getAll();
        evenementTable.getItems().clear();
        evenementTable.getItems().addAll(evenements);
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

    // Article CRUD methods
    private void loadArticles() {
        List<Article> articles = serviceArticle.getAll();
        articleTable.getItems().clear();
        articleTable.getItems().addAll(articles);
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
        selectedArticle = null;
        articleErrorLabel.setText("");
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
}