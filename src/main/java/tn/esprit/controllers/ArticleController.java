package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Article;
import tn.esprit.services.ServiceArticle;

import java.io.IOException;

public class ArticleController {

    @FXML
    private TextField titreField, imageField;
    @FXML
    private TextArea contenueField;
    @FXML
    private TableView<Article> articleTable;

    private ServiceArticle serviceArticle = new ServiceArticle();
    private ObservableList<Article> articleList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Load articles from the database
        try {
            articleList.clear(); // Clear any existing data
            articleList.addAll(serviceArticle.getAll()); // Fetch data from the database
            articleTable.setItems(articleList); // Set the data in the TableView
            System.out.println("Loaded " + articleList.size() + " articles from the database.");
        } catch (Exception e) {
            showAlert("Error", "Failed to load articles: " + e.getMessage());
        }

        // Select an article to populate fields
        articleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                titreField.setText(newSelection.getTitre());
                contenueField.setText(newSelection.getContenue());
                imageField.setText(newSelection.getImage());
            }
        });
    }

    @FXML
    public void addArticle() {
        // Input validation
        if (!validateArticleInputs()) {
            return;
        }

        Article article = new Article();
        article.setTitre(titreField.getText());
        article.setContenue(contenueField.getText());
        article.setImage(imageField.getText());

        try {
            serviceArticle.add(article);
            articleList.clear();
            articleList.addAll(serviceArticle.getAll());
            clearFields();
            showAlert("Success", "Article added successfully!");
        } catch (Exception e) {
            showAlert("Error", "Failed to add article: " + e.getMessage());
        }
    }

    @FXML
    public void updateArticle() {
        Article selectedArticle = articleTable.getSelectionModel().getSelectedItem();
        if (selectedArticle == null) {
            showAlert("Error", "Please select an article to update.");
            return;
        }

        // Input validation
        if (!validateArticleInputs()) {
            return;
        }

        selectedArticle.setTitre(titreField.getText());
        selectedArticle.setContenue(contenueField.getText());
        selectedArticle.setImage(imageField.getText());

        try {
            serviceArticle.update(selectedArticle);
            articleList.clear();
            articleList.addAll(serviceArticle.getAll());
            clearFields();
            showAlert("Success", "Article updated successfully!");
        } catch (Exception e) {
            showAlert("Error", "Failed to update article: " + e.getMessage());
        }
    }

    @FXML
    public void deleteArticle() {
        Article selectedArticle = articleTable.getSelectionModel().getSelectedItem();
        if (selectedArticle == null) {
            showAlert("Error", "Please select an article to delete.");
            return;
        }

        try {
            serviceArticle.delete(selectedArticle);
            articleList.remove(selectedArticle);
            clearFields();
            showAlert("Success", "Article deleted successfully!");
        } catch (Exception e) {
            showAlert("Error", "Failed to delete article: " + e.getMessage());
        }
    }

    @FXML
    public void switchToEvenement() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/EvenementController.fxml"));
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    private boolean validateArticleInputs() {
        if (titreField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Titre cannot be empty.");
            return false;
        }
        if (contenueField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Contenue cannot be empty.");
            return false;
        }
        if (imageField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Image path cannot be empty.");
            return false;
        }
        return true;
    }

    private void clearFields() {
        titreField.clear();
        contenueField.clear();
        imageField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}