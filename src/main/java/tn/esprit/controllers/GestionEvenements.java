package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.models.Evenement;
import tn.esprit.models.User;
import tn.esprit.services.ServiceEvenement;

import java.util.List;

public class GestionEvenements {

    @FXML
    private ScrollPane contentArea;
    @FXML
    private VBox contentVBox;
    @FXML
    private Label titleLabel;

    private ServiceEvenement serviceEvenement;
    private User currentUser;

    @FXML
    public void initialize() {
        serviceEvenement = new ServiceEvenement();
        currentUser = new User(6, "patient1"); // Replace with actual authentication
        displayEvenements();
    }

    private void displayEvenements() {
        titleLabel.setText("Événements");

        // Clear previous content except title
        contentVBox.getChildren().clear();
        contentVBox.getChildren().add(titleLabel);

        // Fetch events
        List<Evenement> evenements = serviceEvenement.getAll();
        System.out.println("Fetched " + evenements.size() + " events"); // Debug
        if (evenements.isEmpty()) {
            Label noEventsLabel = new Label("Aucun événement trouvé.");
            noEventsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            contentVBox.getChildren().add(noEventsLabel);
            return;
        }

        // Display events
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

    private void showEventDetails(Evenement evenement) {
        try {
            // Clear the current content
            contentVBox.getChildren().clear();

            // Debug the FXML resource path
            String fxmlPath = "/EventDetails.fxml";
            System.out.println("Loading FXML: " + getClass().getResource(fxmlPath));
            if (getClass().getResource(fxmlPath) == null) {
                throw new IllegalStateException("FXML file not found at: " + fxmlPath);
            }

            // Load the event details view
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            BorderPane eventDetailsPane = loader.load();

            // Set the event in the controller
            EventDetailsController controller = loader.getController();
            System.out.println("Setting event: " + (evenement != null ? evenement.getNom() : "null"));
            controller.setEvent(evenement);

            // Add a "Retour" button to go back to the events list
            Button backButton = new Button("Retour aux Événements");
            backButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            backButton.setOnAction(event -> displayEvenements());

            // Add the back button and event details to the contentVBox
            contentVBox.getChildren().addAll(backButton, eventDetailsPane);
        } catch (Exception ex) {
            System.err.println("Error loading event details: " + ex.getMessage());
            ex.printStackTrace();
            Label errorLabel = new Label("Erreur lors du chargement des détails de l'événement: " + ex.getMessage());
            errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c;");
            Button backButton = new Button("Retour aux Événements");
            backButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            backButton.setOnAction(event -> displayEvenements());
            contentVBox.getChildren().addAll(errorLabel, backButton);
        }
    }
}