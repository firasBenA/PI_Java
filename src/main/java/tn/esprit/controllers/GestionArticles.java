package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.models.Article;
import tn.esprit.models.User;
import tn.esprit.repository.UserRepository;
import tn.esprit.repository.UserRepositoryImpl;
import tn.esprit.services.AuthService;
import tn.esprit.services.ServiceArticle;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import java.io.File;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import java.util.ArrayList;
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
    private TextField searchField;
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

    private AuthService authService;

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        serviceArticle = new ServiceArticle();

        // Initialize AuthService if not already done
        if (authService == null) {
            try {
                UserRepository userRepository = new UserRepositoryImpl();
                authService = AuthService.getInstance(userRepository);
                currentUser = authService.getCurrentUser();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible d'initialiser le service d'authentification", Alert.AlertType.ERROR);
            }
        }

        refreshArticles();
        displayArticles();
        setupSearchListener();
    }

    private void refreshArticles() {
        if (currentUser == null) {
            System.out.println("No user logged in.");
            showAlert("Erreur", "Aucun utilisateur connecté", Alert.AlertType.WARNING);
            allArticles = new ArrayList<>();
        } else {
            allArticles = serviceArticle.getAll();
        }

        filteredArticles = allArticles;
        updatePagination();
    }

    private void setupSearchListener() {
        searchField.setOnKeyReleased(this::filterArticles);
    }

    @FXML
    private void filterArticles(KeyEvent event) {
        String searchText = searchField.getText().toLowerCase();
        filteredArticles = allArticles.stream()
                .filter(article -> article.getTitre().toLowerCase().contains(searchText) ||
                        (article.getContenue() != null && article.getContenue().toLowerCase().contains(searchText)))
                .collect(Collectors.toList());
        currentPage = 1;
        updatePagination();
        displayArticles();
    }

    private void displayArticles() {
        titleLabel.setText("Articles");

        if (contentVBox.getChildren().size() > 4) {
            contentVBox.getChildren().remove(4, contentVBox.getChildren().size());
        }
        articlesGrid.getChildren().clear();

        if (currentUser == null) {
            Label noUserLabel = new Label("Veuillez vous connecter pour voir les articles.");
            noUserLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d; -fx-font-family: 'Arial';");
            VBox.setMargin(noUserLabel, new Insets(20, 0, 0, 0));
            contentVBox.getChildren().add(noUserLabel);
            return;
        }

        if (filteredArticles.isEmpty()) {
            Label noArticlesLabel = new Label("Aucun article trouvé.");
            noArticlesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d; -fx-font-family: 'Arial';");
            VBox.setMargin(noArticlesLabel, new Insets(20, 0, 0, 0));
            contentVBox.getChildren().add(noArticlesLabel);
            return;
        }

        int startIndex = (currentPage - 1) * articlesPerPage;
        int endIndex = Math.min(startIndex + articlesPerPage, filteredArticles.size());
        List<Article> articlesToDisplay = filteredArticles.subList(startIndex, endIndex);

        int column = 0;
        int row = 0;
        for (Article article : articlesToDisplay) {
            VBox articleCard = new VBox(12);
            articleCard.setStyle("-fx-background-color: #ffffff; -fx-padding: 20; -fx-border-color: #e0e0e0; " +
                    "-fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
            articleCard.setPrefWidth(280);
            articleCard.setPrefHeight(300); // Increased height to accommodate the image

            // Hover effect
            articleCard.setOnMouseEntered(e -> articleCard.setStyle(
                    "-fx-background-color: #f9f9f9; -fx-padding: 20; -fx-border-color: #e0e0e0; " +
                            "-fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 3);"));
            articleCard.setOnMouseExited(e -> articleCard.setStyle(
                    "-fx-background-color: #ffffff; -fx-padding: 20; -fx-border-color: #e0e0e0; " +
                            "-fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"));


            ImageView articleImageView = new ImageView();
            String imagePath = article.getImage();
            System.out.println("Image path for article '" + article.getTitre() + "': " + imagePath); // Debug print
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    // Attempt to load the image from the classpath
                    String resourcePath = getClass().getResource(imagePath) != null
                            ? getClass().getResource(imagePath).toExternalForm()
                            : null;

                    if (resourcePath == null) {
                        // Fallback: Try loading as a file (useful for local testing)
                        File file = new File("src/main/resources" + imagePath);
                        if (file.exists()) {
                            resourcePath = file.toURI().toString();
                        } else {
                            throw new Exception("Image file not found in resources: " + imagePath);
                        }
                    }

                    Image articleImage = new Image(resourcePath);
                    articleImageView.setImage(articleImage);
                    articleImageView.setFitWidth(240);
                    articleImageView.setFitHeight(120);
                    articleImageView.setPreserveRatio(true);
                } catch (Exception e) {
                    System.out.println("Failed to load image for '" + article.getTitre() + "': " + e.getMessage()); // Debug print
                    Label errorLabel = new Label("Image non disponible");
                    errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c; -fx-font-family: 'Arial';");
                    articleCard.getChildren().add(errorLabel);
                }
            } else {
                System.out.println("No image path for article '" + article.getTitre() + "'"); // Debug print
                Label noImageLabel = new Label("Aucune image");
                noImageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-font-family: 'Arial';");
                articleCard.getChildren().add(noImageLabel);
            }

            Label titreLabel = new Label(article.getTitre());
            titreLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-family: 'Arial';");
            titreLabel.setWrapText(true);

            String content = article.getContenue() != null ?
                    (article.getContenue().length() > 60 ? article.getContenue().substring(0, 60) + "..." : article.getContenue()) :
                    "No content available";
            Text contenueLabel = new Text(content);
            contenueLabel.setStyle("-fx-font-size: 14px; -fx-fill: #34495e; -fx-font-family: 'Arial';");
            contenueLabel.setWrappingWidth(240);

            Button likeButton = new Button();
            boolean hasLiked = serviceArticle.hasLiked(currentUser, article);
            likeButton.setText(hasLiked ? "Aimé" : "J'aime");
            likeButton.setStyle("-fx-background-color: " + (hasLiked ? "#bdc3c7" : "#1abc9c") +
                    "; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 16; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-family: 'Arial';");
            likeButton.setDisable(hasLiked);
            likeButton.setOnAction(e -> {
                serviceArticle.like(currentUser, article);
                likeButton.setText("Aimé");
                likeButton.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 16; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-family: 'Arial';");
                likeButton.setDisable(true);
            });
            Tooltip.install(likeButton, new Tooltip(hasLiked ? "Vous avez déjà aimé cet article" : "Aimer cet article"));

            articleCard.getChildren().addAll(articleImageView, titreLabel, contenueLabel, likeButton);
            articlesGrid.add(articleCard, column, row);

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

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to refresh the display of articles
    public void refreshDisplay() {
        refreshArticles();
        displayArticles();
    }
}