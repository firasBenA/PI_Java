package tn.esprit.controllers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.models.Medecin;
import tn.esprit.models.Patient;
import tn.esprit.models.User;
import tn.esprit.services.AuthException;
import tn.esprit.services.AuthService;
import tn.esprit.utils.SceneManager;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class RegisterController {

    // Existing fields (unchanged)
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField ageField;
    @FXML private TextField adresseField;
    @FXML private ComboBox<String> sexeComboBox;
    @FXML private TextField telephoneField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField specialiteField;
    @FXML private Label certificatPathLabel;
    @FXML private Label imagePathLabel;
    @FXML private VBox medecinFields;
    @FXML private Button googleLoginButton;
    @FXML private Label nomError;
    @FXML private Label prenomError;
    @FXML private Label emailError;
    @FXML private Label ageError;
    @FXML private Label adresseError;
    @FXML private Label sexeError;
    @FXML private Label telephoneError;
    @FXML private Label passwordError;
    @FXML private Label roleError;
    @FXML private Label specialiteError;
    @FXML private Label certificatError;
    @FXML private Label imageError;
    @FXML private HBox passwordContainer;
    @FXML private Button togglePasswordButton;
    private boolean passwordVisible = false;
    private File certificatFile;
    private File imageFile;
    private AuthService authService;
    private SceneManager sceneManager;
    private HttpServer callbackServer;
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private static final String GOOGLE_CLIENT_ID = "909960585216-pgbfhj9u9mhf0afbgu6uqqepqhjp6u86.apps.googleusercontent.com";
    private static final String GOOGLE_CLIENT_SECRET = "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";
    private static final String GOOGLE_REDIRECT_URI = "http://localhost:8081/auth/google/callback"; // Use 8081 to avoid port conflict
    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    // Existing methods (initialize, setupRoleListener, etc.) remain unchanged

    private void handleGoogleCallback(String code) {
        try {
            // Exchange code for access token
            String tokenRequest = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                    "&client_id=" + GOOGLE_CLIENT_ID +
                    "&client_secret=" + GOOGLE_CLIENT_SECRET +
                    "&redirect_uri=" + URLEncoder.encode(GOOGLE_REDIRECT_URI, StandardCharsets.UTF_8) +
                    "&grant_type=authorization_code";
            HttpRequest tokenReq = HttpRequest.newBuilder()
                    .uri(URI.create(GOOGLE_TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(tokenRequest))
                    .build();
            HttpResponse<String> tokenRes = client.send(tokenReq, HttpResponse.BodyHandlers.ofString());
            if (tokenRes.statusCode() != 200) {
                throw new IOException("Token request failed: " + tokenRes.statusCode());
            }
            String tokenJson = tokenRes.body();
            GoogleTokenResponse tokenResponse = gson.fromJson(tokenJson, GoogleTokenResponse.class);

            // Get user info
            HttpRequest userReq = HttpRequest.newBuilder()
                    .uri(URI.create(GOOGLE_USER_INFO_URL))
                    .header("Authorization", "Bearer " + tokenResponse.access_token)
                    .GET()
                    .build();
            HttpResponse<String> userRes = client.send(userReq, HttpResponse.BodyHandlers.ofString());
            if (userRes.statusCode() != 200) {
                throw new IOException("User info request failed: " + userRes.statusCode());
            }
            String userJson = userRes.body();
            GoogleUserInfo userInfo = gson.fromJson(userJson, GoogleUserInfo.class);

            // Register user
            Platform.runLater(() -> {
                try {
                    User user = createSocialUser(userInfo.email, userInfo.given_name, userInfo.family_name, "ROLE_PATIENT");
                    authService.registerSocial(user, "google", tokenResponse.access_token);
                    showSuccessDialog("Compte créé avec Google !");
                    sceneManager.showLoginScene();
                } catch (AuthException e) {
                    sceneManager.showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
                }
            });
        } catch (Exception e) {
            Platform.runLater(() -> sceneManager.showAlert("Erreur", "Erreur lors de l'authentification Google: " + e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    private void showSuccessDialog(String message) {
        // Create a custom dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(nomField.getScene().getWindow()); // Set owner to current stage

        // Create dialog content
        VBox dialogContent = new VBox(15);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(20));
        dialogContent.setStyle("-fx-background-color: linear-gradient(to bottom, #805ad5, #b794f4); " +
                "-fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");

        // Checkmark icon (SVG)
        SVGPath checkmark = new SVGPath();
        checkmark.setContent("M10 15.27L4.122 9.39l-1.414 1.414L10 18.096l12.292-12.292-1.414-1.414L10 15.27z");
        checkmark.setStyle("-fx-fill: #ffffff; -fx-scale-x: 2; -fx-scale-y: 2;");

        // Title
        Label title = new Label("Succès !");
        title.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-family: 'Arial';");

        // Message
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #ffffff; -fx-font-family: 'Arial'; -fx-wrap-text: true;");

        // Close button
        Button closeButton = new Button("OK");
        closeButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #805ad5; -fx-font-weight: bold; " +
                "-fx-padding: 8 20; -fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-cursor: hand;");
        closeButton.setOnAction(e -> dialog.close());

        // Add components
        dialogContent.getChildren().addAll(checkmark, title, messageLabel, closeButton);

        // Animations
        FadeTransition fade = new FadeTransition(Duration.millis(300), dialogContent);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(300), dialogContent);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1);
        scale.setToY(1);

        ParallelTransition transition = new ParallelTransition(fade, scale);
        dialog.setOnShown(e -> transition.play());

        // Set dialog properties
        dialog.getDialogPane().setContent(dialogContent);
        dialog.getDialogPane().setStyle("-fx-background-color: transparent;");
        dialog.setHeaderText(null);
        dialog.setGraphic(null);

        // Show dialog
        dialog.showAndWait();
    }

    // Rest of the class (unchanged methods and fields)
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    public void initialize() {
        initializeFields();
        setupRoleListener();
        setupPasswordToggle();
        startCallbackServer();
    }

    private void initializeFields() {
        sexeComboBox.getItems().addAll("Homme", "Femme", "Autre");
        roleComboBox.getItems().addAll("ROLE_PATIENT", "ROLE_MEDECIN");
    }

    private void setupRoleListener() {
        roleComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isMedecin = "ROLE_MEDECIN".equals(newVal);
            medecinFields.setVisible(isMedecin);
            medecinFields.setManaged(isMedecin);
        });
    }

    private void setupPasswordToggle() {
        Objects.requireNonNull(togglePasswordButton, "togglePasswordButton non injecté!");
        togglePasswordButton.setOnAction(e -> togglePasswordVisibility());
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setManaged(true);
            visiblePasswordField.setVisible(true);
            passwordField.setManaged(false);
            passwordField.setVisible(false);
        } else {
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setManaged(true);
            passwordField.setVisible(true);
            visiblePasswordField.setManaged(false);
            visiblePasswordField.setVisible(false);
        }
    }

    private void startCallbackServer() {
        try {
            callbackServer = HttpServer.create(new InetSocketAddress(8081), 0);
            callbackServer.createContext("/auth/google/callback", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                if (query == null || !query.contains("code=")) {
                    exchange.sendResponseHeaders(400, 0);
                    exchange.getResponseBody().close();
                    return;
                }
                String code = query.split("code=")[1].split("&")[0];
                handleGoogleCallback(code);
                String response = "<html><body><h1>Authentification réussie</h1><p>Vous pouvez fermer cette fenêtre.</p></body></html>";
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
            });
            callbackServer.setExecutor(null);
            callbackServer.start();
            System.out.println("Callback server started on port 8081");
        } catch (IOException e) {
            System.err.println("Failed to start callback server: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> sceneManager.showAlert("Erreur", "Impossible de démarrer le serveur de callback: " + e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    @FXML
    private void handleGoogleLogin() {
        String authUrl = GOOGLE_AUTH_URL + "?client_id=" + GOOGLE_CLIENT_ID +
                "&redirect_uri=" + URLEncoder.encode(GOOGLE_REDIRECT_URI, StandardCharsets.UTF_8) +
                "&response_type=code&scope=email%20profile&access_type=offline";
        try {
            java.awt.Desktop.getDesktop().browse(URI.create(authUrl));
        } catch (IOException e) {
            sceneManager.showAlert("Erreur", "Impossible d'ouvrir le navigateur", Alert.AlertType.ERROR);
        }
    }

    private User createSocialUser(String email, String firstName, String lastName, String role) {
        User user = switch (role) {
            case "ROLE_PATIENT" -> new Patient();
            case "ROLE_MEDECIN" -> new Medecin();
            default -> throw new IllegalStateException("Rôle invalide : " + role);
        };
        user.setNom(lastName != null ? lastName : "");
        user.setPrenom(firstName != null ? firstName : "");
        user.setEmail(email);
        user.setAge(18);
        user.setAdresse("");
        user.setSexe("Autre");
        user.setTelephone("");
        user.setRoles(new ArrayList<>(Arrays.asList(role)));
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    @FXML
    private void handleRegister() {
        resetErrors();
        if (validateForm()) {
            try {
                User user = createUserFromForm();
                String rawPassword = getPassword();
                user.hashPassword(rawPassword);
                System.out.println("Registering user: " + user.getClass().getSimpleName() + ", user_type: " + user.getUserType() + ", role: " + user.getRoles());
                authService.register(user);
                sceneManager.showLoginScene();
                sceneManager.showAlert("Succès", "Compte créé avec succès !", Alert.AlertType.INFORMATION);
            } catch (AuthException e) {
                sceneManager.showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
                System.err.println("Registration error: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                sceneManager.showAlert("Erreur", "Erreur inattendue : " + (e.getMessage() != null ? e.getMessage() : "Erreur inconnue"), Alert.AlertType.ERROR);
                System.err.println("Unexpected error: " + e.getClass().getName() + " - " + (e.getMessage() != null ? e.getMessage() : "No message"));
                e.printStackTrace();
            }
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (nomField.getText().trim().isEmpty()) {
            nomError.setText("Le nom est requis");
            nomError.setVisible(true);
            isValid = false;
        }

        if (prenomField.getText().trim().isEmpty()) {
            prenomError.setText("Le prénom est requis");
            prenomError.setVisible(true);
            isValid = false;
        }

        if (!emailField.getText().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            emailError.setText("Format email invalide");
            emailError.setVisible(true);
            isValid = false;
        }

        try {
            int age = Integer.parseInt(ageField.getText());
            if (age < 1 || age > 120) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            ageError.setText("Âge invalide (1-120)");
            ageError.setVisible(true);
            isValid = false;
        }

        if (adresseField.getText().trim().isEmpty()) {
            adresseError.setText("L'adresse est requise");
            adresseError.setVisible(true);
            isValid = false;
        }

        if (sexeComboBox.getValue() == null) {
            sexeError.setText("Veuillez sélectionner un sexe");
            sexeError.setVisible(true);
            isValid = false;
        }

        if (!telephoneField.getText().matches("^\\d{8}$")) {
            telephoneError.setText("Téléphone invalide (8 chiffres)");
            telephoneError.setVisible(true);
            isValid = false;
        }

        if (getPassword().length() < 6) {
            passwordError.setText("Minimum 6 caractères");
            passwordError.setVisible(true);
            isValid = false;
        }

        if (roleComboBox.getValue() == null) {
            roleError.setText("Veuillez sélectionner un rôle");
            roleError.setVisible(true);
            isValid = false;
        }

        if ("ROLE_MEDECIN".equals(roleComboBox.getValue())) {
            if (specialiteField.getText().trim().isEmpty()) {
                specialiteError.setText("Spécialité requise");
                specialiteError.setVisible(true);
                isValid = false;
            }
            if (certificatFile == null) {
                certificatError.setText("Certificat requis");
                certificatError.setVisible(true);
                isValid = false;
            }
        }

        return isValid;
    }

    private User createUserFromForm() {
        User user;
        String role = roleComboBox.getValue();
        switch (role) {
            case "ROLE_PATIENT":
                user = new Patient();
                break;
            case "ROLE_MEDECIN":
                user = new Medecin();
                break;
            default:
                throw new IllegalStateException("Rôle invalide : " + role);
        }

        user.setNom(nomField.getText().trim());
        user.setPrenom(prenomField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setAge(Integer.parseInt(ageField.getText().trim()));
        user.setAdresse(adresseField.getText().trim());
        user.setSexe(sexeComboBox.getValue());
        user.setTelephone(telephoneField.getText().trim());
        user.setRoles(new ArrayList<>(Arrays.asList(role)));
        user.setCreatedAt(LocalDateTime.now());

        if ("ROLE_MEDECIN".equals(role)) {
            user.setSpecialite(specialiteField.getText().trim());
            user.setCertificat(certificatFile.getAbsolutePath());
        }

        if (imageFile != null) {
            user.setImageProfil(imageFile.getAbsolutePath());
        }

        return user;
    }

    private String getPassword() {
        return passwordVisible ? visiblePasswordField.getText() : passwordField.getText();
    }

    private void resetErrors() {
        nomError.setVisible(false);
        prenomError.setVisible(false);
        emailError.setVisible(false);
        ageError.setVisible(false);
        adresseError.setVisible(false);
        sexeError.setVisible(false);
        telephoneError.setVisible(false);
        passwordError.setVisible(false);
        roleError.setVisible(false);
        specialiteError.setVisible(false);
        certificatError.setVisible(false);
        imageError.setVisible(false);
    }

    @FXML
    private void handleCertificatUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un certificat");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        certificatFile = fileChooser.showOpenDialog(null);
        if (certificatFile != null) {
            certificatPathLabel.setText(certificatFile.getName());
        }
    }

    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image de profil");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        imageFile = fileChooser.showOpenDialog(null);
        if (imageFile != null) {
            imagePathLabel.setText(imageFile.getName());
        }
    }

    @FXML
    private void handleLogin() {
        sceneManager.showLoginScene();
    }

    // Helper classes for JSON parsing
    private static class GoogleTokenResponse {
        String access_token;
    }

    private static class GoogleUserInfo {
        String email;
        String given_name;
        String family_name;
    }
}