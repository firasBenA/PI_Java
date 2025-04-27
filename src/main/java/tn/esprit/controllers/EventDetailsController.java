package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import tn.esprit.models.Evenement;
import tn.esprit.models.Article;
import tn.esprit.services.ServiceArticle;
import org.json.JSONObject;
import org.json.JSONArray;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class EventDetailsController {

    @FXML
    private Label titleLabel;
    @FXML
    private VBox detailsBox;
    @FXML
    private Label nomLabel;
    @FXML
    private Label contenueLabel;
    @FXML
    private Label typeLabel;
    @FXML
    private Label statutLabel;
    @FXML
    private Label lieuxLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private VBox relatedArticlesSection;
    @FXML
    private Button showRelatedArticlesButton;
    @FXML
    private ScrollPane relatedArticlesScrollPane;
    @FXML
    private HBox relatedArticlesBox;
    @FXML
    private ImageView qrCodeImageView;
    @FXML
    private StackPane mapPlaceholder;
    @FXML
    private HBox weatherBox;
    @FXML
    private ImageView weatherIcon;
    @FXML
    private Label weatherLabel;

    private ServiceArticle serviceArticle;
    private Evenement event;
    private String WEATHER_API_KEY;
    private String ORS_API_KEY;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @FXML
    public void initialize() {
        System.out.println("EventDetailsController initialized");
        System.out.println("titleLabel: " + titleLabel);
        System.out.println("mapPlaceholder: " + mapPlaceholder);
        if (relatedArticlesScrollPane != null) {
            relatedArticlesScrollPane.setVisible(false);
        }
        loadApiKeys();
    }

    private void loadApiKeys() {
        try {
            Properties props = new Properties();
            props.load(getClass().getResourceAsStream("/config.properties"));
            WEATHER_API_KEY = props.getProperty("weather.api.key");
            ORS_API_KEY = props.getProperty("ors.api.key");
            System.out.println("Loaded API keys: Weather=" + (WEATHER_API_KEY != null) + ", ORS=" + (ORS_API_KEY != null));
        } catch (Exception e) {
            System.err.println("Error loading API keys: " + e.getMessage());
            WEATHER_API_KEY = "";
            ORS_API_KEY = "";
        }
    }

    public void setEvent(Evenement event) {
        System.out.println("Setting event: " + (event != null ? event.getNom() : "null"));
        this.event = event;
        serviceArticle = new ServiceArticle();
        populateDetails();
        generateQRCode();
        fetchWeatherData();
        loadMap();
    }

    private void populateDetails() {
        if (event != null) {
            Platform.runLater(() -> {
                titleLabel.setText("Détails de l'Événement: " + event.getNom());
                nomLabel.setText("Nom: " + event.getNom());
                contenueLabel.setText("Contenu: " + event.getContenue());
                typeLabel.setText("Type: " + event.getType());
                statutLabel.setText("Statut: " + event.getStatut());
                lieuxLabel.setText("Lieu: " + event.getLieuxEvent());
                dateLabel.setText("Date: " + event.getDateEvent().toString());
            });
        }
    }

    private void fetchWeatherData() {
        if (event == null || event.getLieuxEvent() == null || event.getLieuxEvent().isEmpty()) {
            Platform.runLater(() -> weatherLabel.setText("Lieu non spécifié"));
            return;
        }
        if (WEATHER_API_KEY == null || WEATHER_API_KEY.isEmpty()) {
            Platform.runLater(() -> weatherLabel.setText("Clé API météo manquante"));
            return;
        }

        String location = event.getLieuxEvent();
        String url = String.format("http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&appid=%s", URLEncoder.encode(location, StandardCharsets.UTF_8), WEATHER_API_KEY);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        JSONObject json = new JSONObject(response.body());
                        String description = json.getJSONArray("weather").getJSONObject(0).getString("description");
                        double temp = json.getJSONObject("main").getDouble("temp");
                        String iconCode = json.getJSONArray("weather").getJSONObject(0).getString("icon");
                        String iconUrl = String.format("http://openweathermap.org/img/wn/%s@2x.png", iconCode);

                        Platform.runLater(() -> {
                            weatherLabel.setText(String.format("%s, %.1f°C", description, temp));
                            weatherIcon.setImage(new Image(iconUrl));
                        });
                    } else {
                        Platform.runLater(() -> weatherLabel.setText("Erreur lors de la récupération de la météo"));
                    }
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> weatherLabel.setText("Erreur: " + throwable.getMessage()));
                    return null;
                });
    }

    @FXML
    private void showRelatedArticles() {
        relatedArticlesBox.getChildren().clear();
        List<Article> relatedArticles = serviceArticle.getRelatedArticles(event);
        if (relatedArticles.isEmpty()) {
            Label noArticlesLabel = new Label("Aucun article associé.");
            noArticlesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
            relatedArticlesBox.getChildren().add(noArticlesLabel);
        } else {
            for (Article article : relatedArticles) {
                VBox articleCard = new VBox(10);
                articleCard.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 10; -fx-border-color: #dcdcdc; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
                articleCard.setPrefWidth(200);

                Label titreLabel = new Label(article.getTitre());
                titreLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                titreLabel.setWrapText(true);

                Label contenueLabel = new Label(article.getContenue().length() > 50 ? article.getContenue().substring(0, 50) + "..." : article.getContenue());
                contenueLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #34495e;");
                contenueLabel.setWrapText(true);

                articleCard.getChildren().addAll(titreLabel, contenueLabel);
                relatedArticlesBox.getChildren().add(articleCard);
            }
        }
        relatedArticlesScrollPane.setVisible(true);
        showRelatedArticlesButton.setVisible(false);
    }

    private void generateQRCode() {
        // Placeholder for QR Code generation
        if (qrCodeImageView != null) {
            qrCodeImageView.setImage(null); // Placeholder
        }
    }

    private double[] geocodeLocation(String location) {
        if (ORS_API_KEY == null || ORS_API_KEY.isEmpty()) {
            System.err.println("Clé API ORS manquante");
            return null;
        }
        try {
            String url = String.format("https://api.openrouteservice.org/geocode/search?api_key=%s&text=%s&size=1", ORS_API_KEY, URLEncoder.encode(location, StandardCharsets.UTF_8));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());
                JSONArray features = json.getJSONArray("features");
                if (!features.isEmpty()) {
                    JSONObject geometry = features.getJSONObject(0).getJSONObject("geometry");
                    JSONArray coordinates = geometry.getJSONArray("coordinates");
                    double lon = coordinates.getDouble(0);
                    double lat = coordinates.getDouble(1);
                    return new double[]{lat, lon};
                }
            }
            System.err.println("Geocoding failed: " + response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadMap() {
        if (event == null || event.getLieuxEvent() == null || event.getLieuxEvent().isEmpty()) {
            Platform.runLater(() -> mapPlaceholder.getChildren().setAll(new Label("Lieu non spécifié")));
            return;
        }

        double[] coordinates = geocodeLocation(event.getLieuxEvent());
        if (coordinates == null) {
            Platform.runLater(() -> mapPlaceholder.getChildren().setAll(new Label("Impossible de géocoder le lieu")));
            return;
        }

        try {
            // Load the HTML template
            java.net.URL resourceUrl = getClass().getResource("/ors_map.html");
            if (resourceUrl == null) {
                Platform.runLater(() -> mapPlaceholder.getChildren().setAll(new Label("Fichier ors_map.html introuvable")));
                return;
            }
            String htmlContent = new String(Files.readAllBytes(Paths.get(resourceUrl.toURI())));

            // Replace placeholders with actual values
            htmlContent = htmlContent.replace("LATITUDE", String.valueOf(coordinates[0]))
                    .replace("LONGITUDE", String.valueOf(coordinates[1]))
                    .replace("LOCATION_NAME", event.getLieuxEvent());

            // Create and configure WebView
            WebView webView = new WebView();
            webView.getEngine().loadContent(htmlContent);
            webView.setPrefSize(504, 250);
            webView.getEngine().setOnError(event -> System.err.println("WebView error: " + event.getMessage()));

            // Add WebView to mapPlaceholder
            Platform.runLater(() -> mapPlaceholder.getChildren().setAll(webView));
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> mapPlaceholder.getChildren().setAll(new Label("Erreur lors du chargement de la carte: " + e.getMessage())));
        }
    }
}