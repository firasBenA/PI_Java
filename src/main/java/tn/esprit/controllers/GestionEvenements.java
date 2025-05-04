package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
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

        typeFilter.getItems().addAll("Tous les types", "Conference", "Workshop", "Seminar");
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
            participateButton.setText("Cancel Participation");
            participateButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        }

        participateButton.setOnAction(e -> {
            if (serviceEvenement.hasParticipated(currentUser, evenement)) {
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Confirmation d'annulation");
                confirmationAlert.setHeaderText(null);
                confirmationAlert.setContentText("Êtes-vous sûr de vouloir annuler votre participation à cet événement ?");

                ButtonType buttonTypeYes = new ButtonType("Oui");
                ButtonType buttonTypeCancel = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
                confirmationAlert.getButtonTypes().setAll(buttonTypeYes, buttonTypeCancel);

                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == buttonTypeYes) {
                        serviceEvenement.cancelParticipation(currentUser, evenement);
                        participateButton.setText("Participer");
                        participateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        buttonBox.getChildren().removeIf(node -> node instanceof Button && ((Button) node).getText().equals("Receive Details by Email"));
                    }
                });
            } else {
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Confirmation de participation");
                confirmationAlert.setHeaderText(null);
                confirmationAlert.setContentText("Êtes-vous sûr de vouloir participer à cet événement ?");

                ButtonType buttonTypeYes = new ButtonType("Oui");
                ButtonType buttonTypeCancel = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
                confirmationAlert.getButtonTypes().setAll(buttonTypeYes, buttonTypeCancel);

                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == buttonTypeYes) {
                        serviceEvenement.participate(currentUser, evenement);
                        participateButton.setText("Cancel Participation");
                        participateButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                        Button emailButton = new Button("Receive Details by Email");
                        emailButton.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: white;");
                        emailButton.setOnAction(ev -> sendEventDetailsEmail(currentUser, evenement));
                        buttonBox.getChildren().add(emailButton);
                    }
                });
            }
        });

        Button detailsButton = new Button("Voir Détails");
        detailsButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        detailsButton.setOnAction(e -> showEventDetails(evenement));

        if (serviceEvenement.hasParticipated(currentUser, evenement)) {
            Button emailButton = new Button("Receive Details by Email");
            emailButton.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: white;");
            emailButton.setOnAction(e -> sendEventDetailsEmail(currentUser, evenement));
            buttonBox.getChildren().add(emailButton);
        }

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
    private void sendEventDetailsEmail(User user, Evenement evenement) {
        final String username = "esprit.recover.plus@gmail.com";
        final String password = "jxsdekiolyggrpjj";
        final String recipientEmail = "houssem.jamei1899@gmail.com"; // Replace with real email

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Event Details: " + evenement.getNom());

            // Create the HTML content with CSS styling
            String htmlContent =
                    "<!DOCTYPE html>" +
                            "<html>" +
                            "<head>" +
                            "<style>" +
                            "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333333; max-width: 600px; margin: 0 auto; }" +
                            ".email-container { border: 1px solid #dddddd; border-radius: 5px; padding: 20px; background-color: #f9f9f9; }" +
                            ".header { text-align: center; padding-bottom: 20px; border-bottom: 2px solid #4285f4; margin-bottom: 20px; }" +
                            ".header h1 { color: #4285f4; margin: 0; }" +
                            ".greeting { font-size: 18px; margin-bottom: 20px; }" +
                            ".event-details { background-color: #ffffff; padding: 15px; border-radius: 5px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }" +
                            ".event-property { margin-bottom: 10px; }" +
                            ".property-label { font-weight: bold; color: #4285f4; display: inline-block; width: 100px; }" +
                            ".footer { margin-top: 30px; text-align: center; font-size: 14px; color: #666666; }" +
                            "</style>" +
                            "</head>" +
                            "<body>" +
                            "<div class='email-container'>" +
                            "<div class='header'><h1>Event Notification</h1></div>" +
                            "<div class='greeting'>Dear " + user.getPrenom() + ",</div>" +
                            "<p>Thank you for participating in the following event:</p>" +
                            "<div class='event-details'>" +
                            "<div class='event-property'><span class='property-label'>Event Name:</span> " + evenement.getNom() + "</div>" +
                            "<div class='event-property'><span class='property-label'>Content:</span> " + evenement.getContenue() + "</div>" +
                            "<div class='event-property'><span class='property-label'>Type:</span> " + evenement.getType() + "</div>" +
                            "<div class='event-property'><span class='property-label'>Status:</span> " + evenement.getStatut() + "</div>" +
                            "<div class='event-property'><span class='property-label'>Location:</span> " + evenement.getLieuxEvent() + "</div>" +
                            "<div class='event-property'><span class='property-label'>Date:</span> " + evenement.getDateEvent().toString() + "</div>" +
                            "</div>" +
                            "<p>We look forward to seeing you there!</p>" +
                            "<div class='footer'>" +
                            "<p>Best regards,<br>Event Team</p>" +
                            "</div>" +
                            "</div>" +
                            "</body>" +
                            "</html>";

            // Set the email content as HTML
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Email Sent");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Event details have been sent to " + recipientEmail);
            successAlert.showAndWait();

        } catch (MessagingException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Email Error");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Failed to send email: " + e.getMessage());
            errorAlert.showAndWait();
            e.printStackTrace();
        }
    }


}