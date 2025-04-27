package tn.esprit.controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import tn.esprit.models.Article;
import tn.esprit.models.Evenement;
import tn.esprit.services.ServiceArticle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

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
    private ImageView qrCodeImage;
    @FXML
    private VBox relatedArticlesSection;
    @FXML
    private Button showRelatedArticlesButton;
    @FXML
    private ScrollPane relatedArticlesScrollPane;
    @FXML
    private HBox relatedArticlesBox;
    @FXML
    private ImageView mapImage;
    @FXML
    private Label weatherLabel;

    private ServiceArticle serviceArticle;
    private Evenement event;

    // Replace with your API keys
    private static final String GOOGLE_MAPS_API_KEY = "YOUR_GOOGLE_MAPS_API_KEY";
    private static final String OPENWEATHERMAP_API_KEY = "YOUR_OPENWEATHERMAP_API_KEY";
    private final OkHttpClient client = new OkHttpClient();

    public void setEvent(Evenement event) {
        this.event = event;
        serviceArticle = new ServiceArticle();
        populateDetails();
        generateQRCode();
        loadMap();
        loadWeather();
    }

    private void populateDetails() {
        titleLabel.setText("Détails de l'Événement: " + event.getNom());
        nomLabel.setText("Nom: " + event.getNom());
        contenueLabel.setText("Contenu: " + event.getContenue());
        typeLabel.setText("Type: " + event.getType());
        statutLabel.setText("Statut: " + event.getStatut());
        lieuxLabel.setText("Lieu: " + event.getLieuxEvent());
        dateLabel.setText("Date: " + event.getDateEvent().toString());
    }

    @FXML
    private void showRelatedArticles() {
        // Clear previous content
        relatedArticlesBox.getChildren().clear();

        // Fetch related articles
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

        // Show the articles and hide the button
        relatedArticlesScrollPane.setVisible(true);
        showRelatedArticlesButton.setVisible(false);
    }

    private void generateQRCode() {
        try {
            String eventUrl = "https://yourapp.com/event/" + event.getId();
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(eventUrl, BarcodeFormat.QR_CODE, 150, 150);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(pngOutputStream.toByteArray());

            Image qrImage = new Image(inputStream);
            qrCodeImage.setImage(qrImage);
        } catch (WriterException | IOException e) {
            System.err.println("Error generating QR code: " + e.getMessage());
            qrCodeImage.setImage(new Image("https://via.placeholder.com/150?text=QR+Code+Erreur"));
        }
    }

    private void loadMap() {
        try {
            String location = event.getLieuxEvent().replace(" ", "+");
            String mapUrl = String.format(
                    "https://maps.googleapis.com/maps/api/staticmap?center=%s&zoom=14&size=300x200&maptype=roadmap&key=%s",
                    location, GOOGLE_MAPS_API_KEY
            );

            Request request = new Request.Builder().url(mapUrl).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(response.body().bytes());
                    Image map = new Image(inputStream);
                    mapImage.setImage(map);
                } else {
                    throw new IOException("Failed to fetch map image");
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading map: " + e.getMessage());
            mapImage.setImage(new Image("https://via.placeholder.com/300x200?text=Carte+Erreur"));
        }
    }

    private void loadWeather() {
        try {
            String location = event.getLieuxEvent().replace(" ", "+");
            String weatherUrl = String.format(
                    "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric&lang=fr",
                    location, OPENWEATHERMAP_API_KEY
            );

            Request request = new Request.Builder().url(weatherUrl).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonData);
                    String description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    double temp = jsonObject.getJSONObject("main").getDouble("temp");
                    weatherLabel.setText(String.format("Météo à %s: %s, %.1f°C", event.getLieuxEvent(), description, temp));
                    weatherLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
                } else {
                    throw new IOException("Failed to fetch weather data");
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading weather: " + e.getMessage());
            weatherLabel.setText("Erreur lors du chargement de la météo");
            weatherLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c;");
        }
    }
}