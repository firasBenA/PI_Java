package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import tn.esprit.models.Article;
import tn.esprit.models.User;
import tn.esprit.services.ServiceArticle;

import java.util.List;

public class GestionArticles {

    @FXML
    private ScrollPane contentArea;
    @FXML
    private VBox contentVBox;
    @FXML
    private Label titleLabel;
    @FXML
    private GridPane articlesGrid;

    private ServiceArticle serviceArticle;
    private User currentUser;

    @FXML
    public void initialize() {
        serviceArticle = new ServiceArticle();
        currentUser = new User(6, "patient1"); // Replace with actual authentication
        displayArticles();
    }

    private void displayArticles() {
        titleLabel.setText("Articles");

        // Clear previous content except title and grid
        if (contentVBox.getChildren().size() > 2) {
            contentVBox.getChildren().remove(2, contentVBox.getChildren().size());
        }
        articlesGrid.getChildren().clear();
        System.out.println("Cleared articlesGrid"); // Debug

        // Fetch articles
        List<Article> articles = serviceArticle.getAll();
        System.out.println("Fetched " + articles.size() + " articles"); // Debug
        if (articles.isEmpty()) {
            Label noArticlesLabel = new Label("Aucun article trouvé.");
            noArticlesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            contentVBox.getChildren().add(noArticlesLabel);
            System.out.println("No articles found, added label"); // Debug
            return;
        }

        // Display articles in GridPane
        int column = 0;
        int row = 0;
        for (Article article : articles) {
            System.out.println("Creating card for article: " + article.getTitre()); // Debug
            VBox articleCard = new VBox(10);
            articleCard.setStyle("-fx-background-color: #ffffff; -fx-padding: 15; -fx-border-color: #dcdcdc; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
            articleCard.setPrefWidth(250);
            articleCard.setPrefHeight(300);

            // Article title
            Label titreLabel = new Label(article.getTitre());
            titreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            titreLabel.setWrapText(true);

            // Article content (truncate if too long)
            String content = article.getContenue() != null ?
                    (article.getContenue().length() > 100 ? article.getContenue().substring(0, 100) + "..." : article.getContenue()) :
                    "No content available";
            Label contenueLabel = new Label(content);
            contenueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
            contenueLabel.setWrapText(true);

            // Article image
            ImageView imageView = new ImageView();
            try {
                if (article.getImage() != null && !article.getImage().isEmpty()) {
                    Image image = new Image("file:" + article.getImage(), 200, 150, true, true);
                    imageView.setImage(image);
                    System.out.println("Loaded image: " + article.getImage()); // Debug
                } else {
                    imageView.setImage(new Image("file:src/main/resources/default-image.png", 200, 150, true, true));
                    System.out.println("Loaded default image"); // Debug
                }
            } catch (Exception e) {
                System.err.println("Error loading image for article " + article.getTitre() + ": " + e.getMessage());
                imageView.setImage(new Image("file:src/main/resources/default-image.png", 200, 150, true, true));
            }

            // Like button
            Button likeButton = new Button("J'aime");
            likeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            if (serviceArticle.hasLiked(currentUser, article)) {
                likeButton.setDisable(true);
                likeButton.setText("Déjà aimé");
            }
            likeButton.setOnAction(e -> {
                serviceArticle.like(currentUser, article);
                likeButton.setDisable(true);
                likeButton.setText("Déjà aimé");
            });

            articleCard.getChildren().addAll(imageView, titreLabel, contenueLabel, likeButton);

            // Add to GridPane
            articlesGrid.add(articleCard, column, row);
            System.out.println("Added article card at column " + column + ", row " + row); // Debug

            // Update column and row
            column++;
            if (column == 2) {
                column = 0;
                row++;
            }
        }
    }
}