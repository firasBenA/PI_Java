package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import tn.esprit.models.Evenement;
import tn.esprit.models.Article;
import tn.esprit.services.ServiceArticle;
import org.json.JSONObject;
import org.json.JSONArray;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;

public class EventDetailsController {

    @FXML private Label titleLabel;
    @FXML private VBox detailsBox;
    @FXML private Label nomLabel;
    @FXML private Label contenueLabel;
    @FXML private Label typeLabel;
    @FXML private Label statutLabel;
    @FXML private Label lieuxLabel;
    @FXML private Label dateLabel;
    @FXML private VBox relatedArticlesSection;
    @FXML private Button showRelatedArticlesButton;
    @FXML private ScrollPane relatedArticlesScrollPane;
    @FXML private HBox relatedArticlesBox;
    @FXML private ImageView qrCodeImageView;
    @FXML private StackPane mapPlaceholder;
    @FXML private HBox weatherBox;
    @FXML private ImageView weatherIcon;
    @FXML private Label weatherLabel;

    private ServiceArticle serviceArticle;
    private Evenement event;
    private String WEATHER_API_KEY;
    private String ORS_API_KEY;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @FXML
    public void initialize() {
        System.out.println("EventDetailsController initialized");
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
        String url = String.format("http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&appid=%s",
                URLEncoder.encode(location, StandardCharsets.UTF_8), WEATHER_API_KEY);

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
        try {
            String instagramUrl = "https://www.instagram.com/esprit.recover.plus/";

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            int width = 300;
            int height = 300;

            BitMatrix bitMatrix = qrCodeWriter.encode(instagramUrl, BarcodeFormat.QR_CODE, width, height);

            WritableImage qrImage = new WritableImage(width, height);
            PixelWriter pixelWriter = qrImage.getPixelWriter();

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    pixelWriter.setColor(x, y, bitMatrix.get(x, y) ? javafx.scene.paint.Color.BLACK : javafx.scene.paint.Color.WHITE);
                }
            }

            qrCodeImageView.setImage(qrImage);

            Tooltip tooltip = new Tooltip("Scanner pour visiter notre page Instagram");
            Tooltip.install(qrCodeImageView, tooltip);

            System.out.println("QR Code generated successfully for URL: " + instagramUrl);
        } catch (Exception e) {
            System.err.println("Error generating QR code: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // FIX: Improved map loading method using direct OpenStreetMap URL
    private void loadMap() {
        if (event == null || event.getLieuxEvent() == null || event.getLieuxEvent().isEmpty()) {
            Platform.runLater(() -> mapPlaceholder.getChildren().setAll(new Label("Lieu non spécifié")));
            return;
        }

        try {
            // This method directly uses OpenStreetMap's embed functionality
            // Instead of trying to load HTML file from resources
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();

            // Default coordinates for Tunis if we can't geocode
            double[] coordinates = {36.8065, 10.1815}; // Default: Tunis

            // Try to geocode if possible
            double[] geocoded = geocodeLocation(event.getLieuxEvent());
            if (geocoded != null) {
                coordinates = geocoded;
            }

            // Format the OpenStreetMap URL with the coordinates
            String mapUrl = String.format(
                    "https://www.openstreetmap.org/export/embed.html?bbox=%.6f%%2C%.6f%%2C%.6f%%2C%.6f&layer=mapnik&marker=%.6f%%2C%.6f",
                    coordinates[1] - 0.01, // longitude - offset
                    coordinates[0] - 0.01, // latitude - offset
                    coordinates[1] + 0.01, // longitude + offset
                    coordinates[0] + 0.01, // latitude + offset
                    coordinates[0],        // marker latitude
                    coordinates[1]         // marker longitude
            );

            System.out.println("Loading map with URL: " + mapUrl);
            webEngine.load(mapUrl);

            webView.setPrefSize(504, 250);
            webEngine.setOnError(event -> System.err.println("WebView error: " + event.getMessage()));

            Platform.runLater(() -> mapPlaceholder.getChildren().setAll(webView));

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() ->
                    mapPlaceholder.getChildren().setAll(
                            new Label("Erreur lors du chargement de la carte: " + e.getMessage())
                    )
            );
        }
    }

    private double[] geocodeLocation(String location) {
        if (ORS_API_KEY == null || ORS_API_KEY.isEmpty()) {
            System.err.println("Clé API ORS manquante");
            return null;
        }
        try {
            String url = String.format(
                    "https://api.openrouteservice.org/geocode/search?api_key=%s&text=%s&size=1",
                    ORS_API_KEY,
                    URLEncoder.encode(location, StandardCharsets.UTF_8)
            );

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
}