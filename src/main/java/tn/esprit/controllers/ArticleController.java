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
        try {
            articleList.clear();
            articleList.addAll(serviceArticle.getAll());
            articleTable.setItems(articleList);
            System.out.println("Loaded " + articleList.size() + " articles from the database.");
        } catch (Exception e) {
            showError("Échec du chargement : " + e.getMessage());
        }

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
            showAlert("Succès", "Article ajouté avec succès.");
        } catch (Exception e) {
            showError("Erreur d'ajout : " + e.getMessage());
        }
    }

    @FXML
    public void updateArticle() {
        Article selectedArticle = articleTable.getSelectionModel().getSelectedItem();
        if (selectedArticle == null) {
            showError("Veuillez sélectionner un article à modifier.");
            return;
        }

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
            showAlert("Succès", "Article modifié avec succès.");
        } catch (Exception e) {
            showError("Erreur de modification : " + e.getMessage());
        }
    }

    @FXML
    public void deleteArticle() {
        Article selectedArticle = articleTable.getSelectionModel().getSelectedItem();
        if (selectedArticle == null) {
            showError("Veuillez sélectionner un article à supprimer.");
            return;
        }

        try {
            serviceArticle.delete(selectedArticle);
            articleList.remove(selectedArticle);
            clearFields();
            showAlert("Succès", "Article supprimé avec succès.");
        } catch (Exception e) {
            showError("Erreur de suppression : " + e.getMessage());
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
            showError("Le titre est obligatoire.");
            return false;
        }
        if (contenueField.getText().trim().isEmpty()) {
            showError("Le contenu est obligatoire.");
            return false;
        }
        if (imageField.getText().trim().isEmpty()) {
            showError("L'image est obligatoire.");
            return false;
        }
        return true;
    }

    private void clearFields() {
        titreField.clear();
        contenueField.clear();
        imageField.clear();
    }

    // Overloaded showAlert methods
    private void showAlert(String message) {
        showAlert("Information", message);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
