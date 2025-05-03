package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.models.Evenement;
import tn.esprit.models.User;
import tn.esprit.services.ServiceEvenement;

import java.util.List;
import java.util.stream.Collectors;

public class GestionEvenements {

    @FXML
    private ScrollPane contentArea;
    @FXML
    private VBox contentVBox;
    @FXML
    private Label titleLabel;
    @FXML
    private ComboBox<String> typeFilter;
    @FXML
    private Button filterButton;
    @FXML
    private HBox filterHBox;
    @FXML
    private HBox paginationControls;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private Label pageLabel;

    private ServiceEvenement serviceEvenement;
    private User currentUser;
    private List<Evenement> allEvents;
    private List<Evenement> filteredEvents;
    private int currentPage = 1;
    private final int eventsPerPage = 3;

    @FXML
    public void initialize() {
        serviceEvenement = new ServiceEvenement();
        currentUser = new User(1, "patient1");

        typeFilter.getItems().addAll("Tous les types", "Conference", "Workshop", "Seminar"); // Add your event types
        typeFilter.setValue("Tous les types");

        loadEvents();
        displayEvents();
    }

    private void loadEvents() {
        allEvents = serviceEvenement.getAll();
        filteredEvents = allEvents;
        updatePagination();
    }

    @FXML
    private void filterEvents() {
        String selectedType = typeFilter.getValue();
        currentPage = 1;

        if (selectedType.equals("Tous les types")) {
            filteredEvents = allEvents;
        } else {
            filteredEvents = allEvents.stream()
                    .filter(e -> e.getType().equals(selectedType))
                    .collect(Collectors.toList());
        }

        updatePagination();
        displayEvents();
    }

    private void displayEvents() {
        contentVBox.getChildren().clear();

        contentVBox.getChildren().addAll(titleLabel, filterHBox, paginationControls);

        if (filteredEvents.isEmpty()) {
            Label noEventsLabel = new Label("Aucun événement trouvé.");
            noEventsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            contentVBox.getChildren().add(noEventsLabel);
            return;
        }

        int startIndex = (currentPage - 1) * eventsPerPage;
        int endIndex = Math.min(startIndex + eventsPerPage, filteredEvents.size());

        for (int i = startIndex; i < endIndex; i++) {
            Evenement evenement = filteredEvents.get(i);
            VBox eventBox = createEventBox(evenement);
            contentVBox.getChildren().add(eventBox);
        }
    }

    private VBox createEventBox(Evenement evenement) {
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

        return eventBox;
    }

    @FXML
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
            displayEvents();
        }
    }

    @FXML
    private void nextPage() {
        if (currentPage < getTotalPages()) {
            currentPage++;
            updatePagination();
            displayEvents();
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
        return (int) Math.ceil((double) filteredEvents.size() / eventsPerPage);
    }

    private void showEventDetails(Evenement evenement) {
        try {
            contentVBox.getChildren().clear();
            String fxmlPath = "/EventDetails.fxml";
            if (getClass().getResource(fxmlPath) == null) {
                throw new IllegalStateException("FXML file not found at: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            BorderPane eventDetailsPane = loader.load();
            EventDetailsController controller = loader.getController();
            controller.setEvent(evenement);

            Button backButton = new Button("Retour aux Événements");
            backButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            backButton.setOnAction(event -> {
                contentVBox.getChildren().clear();
                contentVBox.getChildren().addAll(titleLabel, filterHBox, paginationControls);
                displayEvents();
            });

            contentVBox.getChildren().addAll(backButton, eventDetailsPane);
        } catch (Exception ex) {
            System.err.println("Error loading event details: " + ex.getMessage());
            ex.printStackTrace();
            Label errorLabel = new Label("Erreur lors du chargement des détails de l'événement: " + ex.getMessage());
            errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c;");
            Button backButton = new Button("Retour aux Événements");
            backButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            backButton.setOnAction(event -> {
                contentVBox.getChildren().clear();
                contentVBox.getChildren().addAll(titleLabel, filterHBox, paginationControls);
                displayEvents();
            });
            contentVBox.getChildren().addAll(errorLabel, backButton);
        }
    }
}