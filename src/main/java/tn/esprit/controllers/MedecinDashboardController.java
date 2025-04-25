package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.models.Article;
import tn.esprit.models.Evenement;
import tn.esprit.services.ServiceArticle;
import tn.esprit.services.ServiceEvenement;

import java.time.LocalDate;

public class MedecinDashboardController {

    @FXML
    private Button evenementButton;
    @FXML
    private Button articleButton;
    @FXML
    private ScrollPane contentArea;
    @FXML
    private VBox contentVBox;

    private ServiceEvenement serviceEvenement;
    private ServiceArticle serviceArticle;

    private TableView<Evenement> eventsTable;
    private TextField eventNomField;
    private TextArea eventContenueField;
    private TextField eventTypeField;
    private TextField eventStatutField;
    private TextField eventLieuxField;
    private DatePicker eventDatePicker;
    private Button addEventButton;
    private Button updateEventButton;
    private Button deleteEventButton;
    private Button clearEventFormButton;
    private Evenement selectedEvent;

    private TableView<Article> articlesTable;
    private TextField articleTitreField;
    private TextArea articleContenueField;
    private TextField articleImageField;
    private Button addArticleButton;
    private Button updateArticleButton;
    private Button deleteArticleButton;
    private Button clearArticleFormButton;
    private Article selectedArticle;

    @FXML
    public void initialize() {
        serviceEvenement = new ServiceEvenement();
        serviceArticle = new ServiceArticle();
        showEvenements(); // Display events by default
    }

    @FXML
    private void showEvenements() {
        contentVBox.getChildren().clear();

        // Title
        Label titleLabel = new Label("Événements");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Table for Events
        eventsTable = new TableView<>();
        TableColumn<Evenement, String> eventNomColumn = new TableColumn<>("Nom");
        eventNomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        eventNomColumn.setPrefWidth(150);

        TableColumn<Evenement, String> eventContenueColumn = new TableColumn<>("Contenu");
        eventContenueColumn.setCellValueFactory(new PropertyValueFactory<>("contenue"));
        eventContenueColumn.setPrefWidth(200);

        TableColumn<Evenement, String> eventTypeColumn = new TableColumn<>("Type");
        eventTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        eventTypeColumn.setPrefWidth(100);

        TableColumn<Evenement, String> eventStatutColumn = new TableColumn<>("Statut");
        eventStatutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        eventStatutColumn.setPrefWidth(100);

        TableColumn<Evenement, String> eventLieuxColumn = new TableColumn<>("Lieu");
        eventLieuxColumn.setCellValueFactory(new PropertyValueFactory<>("lieuxEvent"));
        eventLieuxColumn.setPrefWidth(150);

        TableColumn<Evenement, LocalDate> eventDateColumn = new TableColumn<>("Date");
        eventDateColumn.setCellValueFactory(new PropertyValueFactory<>("dateEvent"));
        eventDateColumn.setPrefWidth(100);

        eventsTable.getColumns().addAll(eventNomColumn, eventContenueColumn, eventTypeColumn, eventStatutColumn, eventLieuxColumn, eventDateColumn);
        refreshEventsTable();

        // Form for Adding/Editing Events
        VBox eventForm = new VBox(10);
        eventForm.setStyle("-fx-background-color: #ffffff; -fx-padding: 15; -fx-border-color: #dcdcdc; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label formTitle = new Label("Ajouter/Modifier un Événement");
        formTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox nomBox = new HBox(10);
        nomBox.getChildren().addAll(new Label("Nom:"), eventNomField = new TextField());
        eventNomField.setPromptText("Nom de l'événement");

        HBox contenueBox = new HBox(10);
        contenueBox.getChildren().addAll(new Label("Contenu:"), eventContenueField = new TextArea());
        eventContenueField.setPromptText("Contenu");
        eventContenueField.setPrefHeight(50);

        HBox typeBox = new HBox(10);
        typeBox.getChildren().addAll(new Label("Type:"), eventTypeField = new TextField());
        eventTypeField.setPromptText("Type");

        HBox statutBox = new HBox(10);
        statutBox.getChildren().addAll(new Label("Statut:"), eventStatutField = new TextField());
        eventStatutField.setPromptText("Statut");

        HBox lieuxBox = new HBox(10);
        lieuxBox.getChildren().addAll(new Label("Lieu:"), eventLieuxField = new TextField());
        eventLieuxField.setPromptText("Lieu");

        HBox dateBox = new HBox(10);
        dateBox.getChildren().addAll(new Label("Date:"), eventDatePicker = new DatePicker());

        HBox buttonBox = new HBox(10);
        addEventButton = new Button("Ajouter");
        addEventButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        addEventButton.setOnAction(e -> addEvent());

        updateEventButton = new Button("Modifier");
        updateEventButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        updateEventButton.setDisable(true);
        updateEventButton.setOnAction(e -> updateEvent());

        deleteEventButton = new Button("Supprimer");
        deleteEventButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        deleteEventButton.setDisable(true);
        deleteEventButton.setOnAction(e -> deleteEvent());

        clearEventFormButton = new Button("Effacer");
        clearEventFormButton.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        clearEventFormButton.setOnAction(e -> clearEventForm());

        buttonBox.getChildren().addAll(addEventButton, updateEventButton, deleteEventButton, clearEventFormButton);
        eventForm.getChildren().addAll(formTitle, nomBox, contenueBox, typeBox, statutBox, lieuxBox, dateBox, buttonBox);

        // Handle event selection
        eventsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedEvent = newSelection;
                eventNomField.setText(selectedEvent.getNom());
                eventContenueField.setText(selectedEvent.getContenue());
                eventTypeField.setText(selectedEvent.getType());
                eventStatutField.setText(selectedEvent.getStatut());
                eventLieuxField.setText(selectedEvent.getLieuxEvent());
                eventDatePicker.setValue(selectedEvent.getDateEvent());
                updateEventButton.setDisable(false);
                deleteEventButton.setDisable(false);
            }
        });

        contentVBox.getChildren().addAll(titleLabel, eventsTable, eventForm);
    }

    @FXML
    private void showArticles() {
        contentVBox.getChildren().clear();

        // Title
        Label titleLabel = new Label("Articles");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Table for Articles
        articlesTable = new TableView<>();
        TableColumn<Article, String> articleTitreColumn = new TableColumn<>("Titre");
        articleTitreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        articleTitreColumn.setPrefWidth(150);

        TableColumn<Article, String> articleContenueColumn = new TableColumn<>("Contenu");
        articleContenueColumn.setCellValueFactory(new PropertyValueFactory<>("contenue"));
        articleContenueColumn.setPrefWidth(300);

        TableColumn<Article, String> articleImageColumn = new TableColumn<>("Image");
        articleImageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
        articleImageColumn.setPrefWidth(200);

        articlesTable.getColumns().addAll(articleTitreColumn, articleContenueColumn, articleImageColumn);
        refreshArticlesTable();

        // Form for Adding/Editing Articles
        VBox articleForm = new VBox(10);
        articleForm.setStyle("-fx-background-color: #ffffff; -fx-padding: 15; -fx-border-color: #dcdcdc; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label formTitle = new Label("Ajouter/Modifier un Article");
        formTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox titreBox = new HBox(10);
        titreBox.getChildren().addAll(new Label("Titre:"), articleTitreField = new TextField());
        articleTitreField.setPromptText("Titre de l'article");

        HBox contenueBox = new HBox(10);
        contenueBox.getChildren().addAll(new Label("Contenu:"), articleContenueField = new TextArea());
        articleContenueField.setPromptText("Contenu");
        articleContenueField.setPrefHeight(50);

        HBox imageBox = new HBox(10);
        imageBox.getChildren().addAll(new Label("Image:"), articleImageField = new TextField());
        articleImageField.setPromptText("Chemin de l'image");

        HBox buttonBox = new HBox(10);
        addArticleButton = new Button("Ajouter");
        addArticleButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        addArticleButton.setOnAction(e -> addArticle());

        updateArticleButton = new Button("Modifier");
        updateArticleButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        updateArticleButton.setDisable(true);
        updateArticleButton.setOnAction(e -> updateArticle());

        deleteArticleButton = new Button("Supprimer");
        deleteArticleButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        deleteArticleButton.setDisable(true);
        deleteArticleButton.setOnAction(e -> deleteArticle());

        clearArticleFormButton = new Button("Effacer");
        clearArticleFormButton.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        clearArticleFormButton.setOnAction(e -> clearArticleForm());

        buttonBox.getChildren().addAll(addArticleButton, updateArticleButton, deleteArticleButton, clearArticleFormButton);
        articleForm.getChildren().addAll(formTitle, titreBox, contenueBox, imageBox, buttonBox);

        // Handle article selection
        articlesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedArticle = newSelection;
                articleTitreField.setText(selectedArticle.getTitre());
                articleContenueField.setText(selectedArticle.getContenue());
                articleImageField.setText(selectedArticle.getImage());
                updateArticleButton.setDisable(false);
                deleteArticleButton.setDisable(false);
            }
        });

        contentVBox.getChildren().addAll(titleLabel, articlesTable, articleForm);
    }

    // Events CRUD Operations
    private void addEvent() {
        if (validateEventForm()) {
            Evenement event = new Evenement();
            event.setNom(eventNomField.getText());
            event.setContenue(eventContenueField.getText());
            event.setType(eventTypeField.getText());
            event.setStatut(eventStatutField.getText());
            event.setLieuxEvent(eventLieuxField.getText());
            event.setDateEvent(eventDatePicker.getValue());

            serviceEvenement.add(event);
            refreshEventsTable();
            clearEventForm();
        }
    }

    private void updateEvent() {
        if (selectedEvent != null && validateEventForm()) {
            selectedEvent.setNom(eventNomField.getText());
            selectedEvent.setContenue(eventContenueField.getText());
            selectedEvent.setType(eventTypeField.getText());
            selectedEvent.setStatut(eventStatutField.getText());
            selectedEvent.setLieuxEvent(eventLieuxField.getText());
            selectedEvent.setDateEvent(eventDatePicker.getValue());

            serviceEvenement.update(selectedEvent);
            refreshEventsTable();
            clearEventForm();
        }
    }

    private void deleteEvent() {
        if (selectedEvent != null) {
            serviceEvenement.delete(selectedEvent);
            refreshEventsTable();
            clearEventForm();
        }
    }

    private void clearEventForm() {
        eventNomField.clear();
        eventContenueField.clear();
        eventTypeField.clear();
        eventStatutField.clear();
        eventLieuxField.clear();
        eventDatePicker.setValue(null);
        selectedEvent = null;
        updateEventButton.setDisable(true);
        deleteEventButton.setDisable(true);
    }

    private void refreshEventsTable() {
        eventsTable.getItems().clear();
        eventsTable.getItems().addAll(serviceEvenement.getAll());
    }

    private boolean validateEventForm() {
        if (eventNomField.getText().isEmpty() || eventContenueField.getText().isEmpty() ||
                eventTypeField.getText().isEmpty() || eventStatutField.getText().isEmpty() ||
                eventLieuxField.getText().isEmpty() || eventDatePicker.getValue() == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return false;
        }
        return true;
    }

    // Articles CRUD Operations
    private void addArticle() {
        if (validateArticleForm()) {
            Article article = new Article();
            article.setTitre(articleTitreField.getText());
            article.setContenue(articleContenueField.getText());
            article.setImage(articleImageField.getText());

            serviceArticle.add(article);
            refreshArticlesTable();
            clearArticleForm();
        }
    }

    private void updateArticle() {
        if (selectedArticle != null && validateArticleForm()) {
            selectedArticle.setTitre(articleTitreField.getText());
            selectedArticle.setContenue(articleContenueField.getText());
            selectedArticle.setImage(articleImageField.getText());

            serviceArticle.update(selectedArticle);
            refreshArticlesTable();
            clearArticleForm();
        }
    }

    private void deleteArticle() {
        if (selectedArticle != null) {
            serviceArticle.delete(selectedArticle);
            refreshArticlesTable();
            clearArticleForm();
        }
    }

    private void clearArticleForm() {
        articleTitreField.clear();
        articleContenueField.clear();
        articleImageField.clear();
        selectedArticle = null;
        updateArticleButton.setDisable(true);
        deleteArticleButton.setDisable(true);
    }

    private void refreshArticlesTable() {
        articlesTable.getItems().clear();
        articlesTable.getItems().addAll(serviceArticle.getAll());
    }

    private boolean validateArticleForm() {
        if (articleTitreField.getText().isEmpty() || articleContenueField.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez remplir les champs obligatoires (Titre et Contenu).");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}