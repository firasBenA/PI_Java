package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.models.Article;
import tn.esprit.models.User;
import tn.esprit.services.ServiceArticle;

import java.util.List;
import java.util.stream.Collectors;

public class GestionArticles {

    @FXML
    private ScrollPane contentArea;
    @FXML
    private VBox contentVBox;
    @FXML
    private Label titleLabel;
    @FXML
    private GridPane articlesGrid;
    @FXML
    private HBox paginationControls;
    @FXML
    private Button prevButton;
    @FXML
    private Label pageLabel;
    @FXML
    private Button nextButton;

    private ServiceArticle serviceArticle;
    private User currentUser;
    private List<Article> allArticles;
    private List<Article> filteredArticles;
    private int currentPage = 1;
    private final int articlesPerPage = 4;

    @FXML
    public void initialize() {
        serviceArticle = new ServiceArticle();
        currentUser = new User(6, "patient1");
        loadArticles();
        displayArticles();
    }

    private void loadArticles() {
        allArticles = serviceArticle.getAll();
        filteredArticles = allArticles;
        updatePagination();
    }

    private void displayArticles() {
        titleLabel.setText("Articles");

        // Clear previous content except title, grid, and pagination controls
        if (contentVBox.getChildren().size() > 3) {
            contentVBox.getChildren().remove(3, contentVBox.getChildren().size());
        }
        articlesGrid.getChildren().clear();
        System.out.println("Cleared articlesGrid");

        if (filteredArticles.isEmpty()) {
            Label noArticlesLabel = new Label("Aucun article trouvé.");
            noArticlesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            contentVBox.getChildren().add(noArticlesLabel);
            System.out.println("No articles found, added label");
            return;
        }

        // Calculate articles to display for the current page
        int startIndex = (currentPage - 1) * articlesPerPage;
        int endIndex = Math.min(startIndex + articlesPerPage, filteredArticles.size());
        List<Article> articlesToDisplay = filteredArticles.subList(startIndex, endIndex);

        // Display articles in GridPane
        int column = 0;
        int row = 0;
        for (Article article : articlesToDisplay) {
            System.out.println("Creating card for article: " + article.getTitre());
            VBox articleCard = new VBox(10);
            articleCard.setStyle("-fx-background-color: #ffffff; -fx-padding: 15; -fx-border-color: #dcdcdc; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
            articleCard.setPrefWidth(250);
            articleCard.setPrefHeight(200); // Reduced height since there's no image

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

            articleCard.getChildren().addAll(titreLabel, contenueLabel, likeButton);

            // Add to GridPane
            articlesGrid.add(articleCard, column, row);
            System.out.println("Added article card at column " + column + ", row " + row);

            // Update column and row
            column++;
            if (column == 2) {
                column = 0;
                row++;
            }
        }
    }

    @FXML
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
            displayArticles();
        }
    }

    @FXML
    private void nextPage() {
        if (currentPage < getTotalPages()) {
            currentPage++;
            updatePagination();
            displayArticles();
        }
    }

    private void updatePagination() {
        int totalPages = getTotalPages();
        paginationControls.setVisible(totalPages > 1);
        paginationControls.setManaged(totalPages > 1);

        pageLabel.setText("Page " + currentPage + " / " + totalPages);
        prevButton.setDisable(currentPage == 1);
        nextButton.setDisable(currentPage == totalPages);
    }

    private int getTotalPages() {
        return (int) Math.ceil((double) filteredArticles.size() / articlesPerPage);
    }
}