package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import tn.esprit.models.Article;
import tn.esprit.models.Evenement;
import tn.esprit.models.User;
import tn.esprit.services.ServiceArticle;
import tn.esprit.services.ServiceEvenement;

import java.util.List;

public class GestionEvenements {

    @FXML
    private Button evenementButton;
    @FXML
    private Button articleButton;
    @FXML
    private ScrollPane contentArea;
    @FXML
    private VBox contentVBox;
    @FXML
    private Label titleLabel;

    private ServiceEvenement serviceEvenement;
    private ServiceArticle serviceArticle;
    private User currentUser; // Simulate a logged-in user

    @FXML
    public void initialize() {
        serviceEvenement = new ServiceEvenement();
        serviceArticle = new ServiceArticle();
        currentUser = new User(); // Replace with actual authentication
        showEvenements(); // Display events by default
    }

    @FXML
    private void showEvenements() {
        titleLabel.setText("Événements");

        // Clear previous content
        contentVBox.getChildren().clear();
        contentVBox.getChildren().add(titleLabel);

        // Fetch events
        List<Evenement> evenements = serviceEvenement.getAll();
        if (evenements.isEmpty()) {
            Label noEventsLabel = new Label("Aucun événement trouvé.");
            noEventsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            contentVBox.getChildren().add(noEventsLabel);
            return;
        }

        // Display events in rows
        for (Evenement evenement : evenements) {
            VBox eventBox = new VBox(5);
            eventBox.setStyle("-fx-background-color: #ffffff; -fx-padding: 15; -fx-border-color: #dcdcdc; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
            eventBox.setPrefWidth(600);

            Label nomLabel = new Label("Nom: " + evenement.getNom());
            nomLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            Label contenueLabel = new Label("Contenu: " + evenement.getContenue());
            contenueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");

            Label typeLabel = new Label("Type: " + evenement.getType());
            typeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");

            Label statutLabel = new Label("Statut: " + evenement.getStatut());
            statutLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");

            Label lieuxLabel = new Label("Lieu: " + evenement.getLieuxEvent());
            lieuxLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");

            Label dateLabel = new Label("Date: " + evenement.getDateEvent().toString());
            dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");

            // Add buttons
            HBox buttonBox = new HBox(10);
            Button participateButton = new Button("Participer");
            participateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
            if (serviceEvenement.hasParticipated(currentUser, evenement)) {
                participateButton.setDisable(true);
                participateButton.setText("Déjà inscrit");
            }
            participateButton.setOnAction(e -> {
                serviceEvenement.participate(currentUser, evenement);
                participateButton.setDisable(true);
                participateButton.setText("Déjà inscrit");
            });

            Button detailsButton = new Button("Voir Détails");
            detailsButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
            detailsButton.setOnAction(e -> showEventDetails(evenement));

            buttonBox.getChildren().addAll(participateButton, detailsButton);

            eventBox.getChildren().addAll(nomLabel, contenueLabel, typeLabel, statutLabel, lieuxLabel, dateLabel, buttonBox);
            contentVBox.getChildren().add(eventBox);
        }
    }

    @FXML
    private void showArticles() {
        titleLabel.setText("Articles");

        // Clear previous content
        contentVBox.getChildren().clear();
        contentVBox.getChildren().add(titleLabel);

        // Fetch articles
        List<Article> articles = serviceArticle.getAll();
        if (articles.isEmpty()) {
            Label noArticlesLabel = new Label("Aucun article trouvé.");
            noArticlesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            contentVBox.getChildren().add(noArticlesLabel);
            return;
        }

        // Use a GridPane to display articles in a card view (2 columns)
        GridPane gridPane = new GridPane();
        gridPane.setHgap(20);
        gridPane.setVgap(20);
        gridPane.setStyle("-fx-padding: 10;");

        int column = 0;
        int row = 0;
        for (Article article : articles) {
            // Create a card for each article
            VBox articleCard = new VBox(10);
            articleCard.setStyle("-fx-background-color: #ffffff; -fx-padding: 15; -fx-border-color: #dcdcdc; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
            articleCard.setPrefWidth(250);
            articleCard.setPrefHeight(300);

            // Article title
            Label titreLabel = new Label(article.getTitre());
            titreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            titreLabel.setWrapText(true);

            // Article content (truncate if too long)
            Label contenueLabel = new Label(article.getContenue().length() > 100 ? article.getContenue().substring(0, 100) + "..." : article.getContenue());
            contenueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
            contenueLabel.setWrapText(true);

            // Article image (if available)
            ImageView imageView = new ImageView();
            try {
                if (article.getImage() != null && !article.getImage().isEmpty()) {
                    Image image = new Image("file:" + article.getImage(), 200, 150, true, true);
                    imageView.setImage(image);
                } else {
                    imageView.setImage(new Image("file:src/main/resources/default-image.png", 200, 150, true, true));
                }
            } catch (Exception e) {
                System.err.println("Error loading image for article: " + e.getMessage());
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

            // Add the card to the GridPane
            gridPane.add(articleCard, column, row);

            // Update column and row for the next card
            column++;
            if (column == 2) { // 2 columns per row
                column = 0;
                row++;
            }
        }

        contentVBox.getChildren().add(gridPane);
    }

    private void showEventDetails(Evenement evenement) {
        try {
            // Clear the current content
            contentVBox.getChildren().clear();

            // Load the event details view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetails.fxml"));
            BorderPane eventDetailsPane = loader.load();

            // Set the event in the controller
            EventDetailsController controller = loader.getController();
            controller.setEvent(evenement);

            // Add a "Retour" button to go back to the events list
            Button backButton = new Button("Retour aux Événements");
            backButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            backButton.setOnAction(e -> showEvenements());

            // Add the back button and event details to the contentVBox
            contentVBox.getChildren().addAll(backButton, eventDetailsPane);
        } catch (Exception e) {
            System.err.println("Error loading event details: " + e.getMessage());
            e.printStackTrace();
            Label errorLabel = new Label("Erreur lors du chargement des détails de l'événement.");
            errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c;");
            contentVBox.getChildren().add(errorLabel);
        }
    }
}