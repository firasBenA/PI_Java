package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.models.Article;
import tn.esprit.services.ServiceArticle;

import java.util.List;

public class ArticleController {

    @FXML
    private TextField titreField;
    @FXML
    private TextArea contenueField;
    @FXML
    private TextField imageField;
    @FXML
    private Label errorLabel;
    @FXML
    private TableView<Article> articleTable;
    @FXML
    private TableColumn<Article, Integer> idColumn;
    @FXML
    private TableColumn<Article, String> titreColumn;
    @FXML
    private TableColumn<Article, String> contenueColumn;
    @FXML
    private TableColumn<Article, String> imageColumn;

    private ServiceArticle serviceArticle;
    private Article selectedArticle;

    @FXML
    public void initialize() {
        serviceArticle = new ServiceArticle();

        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        contenueColumn.setCellValueFactory(new PropertyValueFactory<>("contenue"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));

        // Load articles into the table
        loadArticles();

        // Add listener to table selection
        articleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedArticle = newSelection;
                titreField.setText(selectedArticle.getTitre());
                contenueField.setText(selectedArticle.getContenue());
                imageField.setText(selectedArticle.getImage());
            }
        });
    }

    private void loadArticles() {
        List<Article> articles = serviceArticle.getAll();
        articleTable.getItems().clear();
        articleTable.getItems().addAll(articles);
    }

    @FXML
    private void addArticle() {
        if (!validateInputs()) {
            return;
        }

        Article article = new Article();
        article.setTitre(titreField.getText());
        article.setContenue(contenueField.getText());
        article.setImage(imageField.getText());

        serviceArticle.add(article);
        loadArticles();
        clearFields();
        showSuccess("Article ajouté avec succès !");
    }

    @FXML
    private void updateArticle() {
        if (selectedArticle == null) {
            showError("Veuillez sélectionner un article à modifier.");
            return;
        }

        if (!validateInputs()) {
            return;
        }

        selectedArticle.setTitre(titreField.getText());
        selectedArticle.setContenue(contenueField.getText());
        selectedArticle.setImage(imageField.getText());

        serviceArticle.update(selectedArticle);
        loadArticles();
        clearFields();
        showSuccess("Article modifié avec succès !");
    }

    @FXML
    private void deleteArticle() {
        if (selectedArticle == null) {
            showError("Veuillez sélectionner un article à supprimer.");
            return;
        }

        serviceArticle.delete(selectedArticle);
        loadArticles();
        clearFields();
        showSuccess("Article supprimé avec succès !");
    }

    @FXML
    private void clearFields() {
        titreField.clear();
        contenueField.clear();
        imageField.clear();
        selectedArticle = null;
        errorLabel.setText("");
    }

    private boolean validateInputs() {
        if (titreField.getText().isEmpty()) {
            showError("Le titre ne peut pas être vide.");
            return false;
        }
        if (contenueField.getText().isEmpty()) {
            showError("Le contenu ne peut pas être vide.");
            return false;
        }
        if (imageField.getText().isEmpty()) {
            showError("Le chemin de l'image ne peut pas être vide.");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: green;");
    }
}