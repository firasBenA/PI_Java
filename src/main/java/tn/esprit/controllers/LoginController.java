package tn.esprit.controllers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.models.User;
import tn.esprit.services.AuthException;
import tn.esprit.services.AuthService;
import tn.esprit.services.SessionManager;
import tn.esprit.utils.SceneManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private Button togglePasswordButton;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerButton;
    @FXML private Label errorLabel;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label countdownLabel;
    @FXML private Button googleLoginButton;

    private AuthService authService;
    private SceneManager sceneManager;
    private boolean passwordVisible;
    private HttpServer callbackServer;
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private static final String GOOGLE_CLIENT_ID = "909960585216-pgbfhj9u9mhf0afbgu6uqqepqhjp6u86.apps.googleusercontent.com";
    private static final String GOOGLE_CLIENT_SECRET = "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";
    private static final String GOOGLE_REDIRECT_URI = "http://localhost:8093/auth/google/callback";
    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    public void setAuthService(AuthService authService) {
        this.authService = authService;
        tryStartCallbackServer();
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        tryStartCallbackServer();
    }

    private void tryStartCallbackServer() {
        if (authService == null || sceneManager == null) {
            System.out.println("Cannot start callback server: authService or sceneManager is null");
            return;
        }
        if (callbackServer != null) {
            System.out.println("Callback server already running on port 8093");
            return;
        }
        startCallbackServer();
    }

    @FXML
    public void initialize() {
        if (forgotPasswordLink == null) {
            System.err.println("forgotPasswordLink is null - FXML injection failed");
        } else {
            System.out.println("forgotPasswordLink initialized successfully");
            forgotPasswordLink.setOnAction(this::handleForgotPassword);
        }
        if (emailField == null || passwordField == null || loginButton == null) {
            System.err.println("One or more FXML fields are null - check FXML file and controller");
        }

        loginButton.setOnAction(this::handleLogin);
        registerButton.setOnAction(this::handleRegister);
        togglePasswordButton.setOnAction(this::togglePasswordVisibility);
        googleLoginButton.setOnAction(this::handleGoogleLogin);
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateEmail());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePassword();
            if (!passwordVisible) {
                visiblePasswordField.setText(newVal);
            }
        });
        visiblePasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (passwordVisible) {
                passwordField.setText(newVal);
            }
        });
        passwordField.setOnAction(this::handleLogin);
        passwordVisible = false;
    }

    private void startCallbackServer() {
        int port = 8093;
        int maxRetries = 3;
        int retryDelayMs = 2000;

        // Ensure any existing server is stopped
        stop();

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                checkAndReleasePort(port);
                callbackServer = HttpServer.create(new InetSocketAddress(port), 0);
                callbackServer.createContext("/auth/google/callback", exchange -> {
                    // ... existing callback handling code ...
                });
                callbackServer.setExecutor(null);
                callbackServer.start();
                System.out.println("Callback server started on port " + port + " (attempt " + attempt + ")");
                Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
                return;
            } catch (IOException e) {
                System.err.println("Attempt " + attempt + " failed to start callback server on port " + port + ": " + e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        System.err.println("Failed to start callback server on port " + port + " after " + maxRetries + " attempts");
    }

    public void stop() {
        if (callbackServer != null) {
            System.out.println("Stopping callback server on port 8093...");
            try {
                callbackServer.stop(1); // Allow 1 second for connections to close
                System.out.println("Callback server stopped successfully");
                // Verify port is free after stopping
                checkAndReleasePort(8093);
            } catch (Exception e) {
                System.err.println("Error stopping callback server: " + e.getMessage());
            } finally {
                callbackServer = null;
            }
        } else {
            System.out.println("No callback server to stop");
        }
    }
    private void checkAndReleasePort(int port) {
        try {
            // First, try to bind to the port to check if it's free
            try (ServerSocket socket = new ServerSocket(port)) {
                System.out.println("Port " + port + " is free");
                return;
            } catch (IOException e) {
                System.out.println("Port " + port + " is in use, attempting to release...");
            }

            // Use platform-specific commands to find and terminate the process
            String os = System.getProperty("os.name").toLowerCase();
            String command;
            if (os.contains("win")) {
                command = "netstat -aon | findstr :" + port;
            } else {
                command = "lsof -i :" + port;
            }

            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            reader.close();
            process.waitFor();

            if (output.length() > 0) {
                if (os.contains("win")) {
                    // Windows: Extract PID from netstat output
                    String[] lines = output.toString().split("\n");
                    for (String l : lines) {
                        String[] parts = l.trim().split("\\s+");
                        if (parts.length >= 5) {
                            String pid = parts[parts.length - 1];
                            System.out.println("Found process " + pid + " binding port " + port + ", attempting to terminate...");
                            Runtime.getRuntime().exec("taskkill /PID " + pid + " /F");
                        }
                    }
                } else {
                    // Linux/Mac: Extract PID from lsof output
                    String[] lines = output.toString().split("\n");
                    for (String l : lines) {
                        if (l.contains("LISTEN")) {
                            String[] parts = l.trim().split("\\s+");
                            if (parts.length >= 2) {
                                String pid = parts[1];
                                System.out.println("Found process " + pid + " binding port " + port + ", attempting to terminate...");
                                Runtime.getRuntime().exec("kill -9 " + pid);
                            }
                        }
                    }
                }
                // Wait for the process to terminate
                Thread.sleep(2000);
            }

            // Verify the port is free
            try (ServerSocket socket = new ServerSocket(port)) {
                System.out.println("Port " + port + " is now free");
            } catch (IOException e) {
                System.err.println("Port " + port + " is still in use after release attempt");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error checking/releasing port " + port + ": " + e.getMessage());
        }
    }



    public void cleanup() {
        stop();
    }

    @FXML
    private void handleGoogleLogin(ActionEvent event) {
        String authUrl = GOOGLE_AUTH_URL + "?client_id=" + GOOGLE_CLIENT_ID +
                "&redirect_uri=" + URLEncoder.encode(GOOGLE_REDIRECT_URI, StandardCharsets.UTF_8) +
                "&response_type=code&scope=email%20profile&access_type=offline";
        System.out.println("OAuth URL: " + authUrl);
        try {
            java.awt.Desktop.getDesktop().browse(URI.create(authUrl));
        } catch (IOException e) {
            sceneManager.showAlert("Erreur", "Impossible d'ouvrir le navigateur", Alert.AlertType.ERROR);
        }
    }

    private void handleGoogleCallback(String code) {
        try {
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

            HttpRequest userReq = HttpRequest.newBuilder()
                    .uri(URI.create(GOOGLE_USER_INFO_URL))
                    .header("Authorization", "Bearer " + tokenResponse.access_token)
                    .GET()
                    .build();
            HttpResponse<String> userRes = client.send(userReq, HttpResponse.BodyHandlers.ofString());
            if (userRes.statusCode() != 200) {
                throw new IOException("User info request failed: " + tokenRes.statusCode());
            }
            String userJson = userRes.body();
            GoogleUserInfo userInfo = gson.fromJson(userJson, GoogleUserInfo.class);

            Platform.runLater(() -> {
                try {
                    User existingUser = authService.getUserByEmail(userInfo.email);
                    if (existingUser == null) {
                        sceneManager.showAlert("Erreur", "Aucun compte trouvé avec cet email. Redirection vers l'inscription.", Alert.AlertType.ERROR);
                        sceneManager.showRegisterScene();
                        return;
                    }
                    existingUser.setSocialProvider("google");
                    existingUser.setSocialAccessToken(tokenResponse.access_token);
                    authService.updateUser(existingUser);
                    SessionManager.saveSession(userInfo.email); // Save session after Google login
                    handleSocialLogin(existingUser);
                } catch (AuthException e) {
                    sceneManager.showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
                }
            });
        } catch (Exception e) {
            Platform.runLater(() -> sceneManager.showAlert("Erreur", "Erreur lors de l'authentification Google: " + e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    private void handleSocialLogin(User user) {
        try {
            switch (user.getUserType()) {
                case "PATIENT":
                    sceneManager.showPatientDashboard(user);
                    break;
                case "MEDECIN":
                    sceneManager.showMedecinDashboard(user);
                    break;
                case "ADMIN":
                    sceneManager.showAdminDashboard(user);
                    break;
                default:
                    errorLabel.setText("Type d'utilisateur non supporté : " + user.getUserType());
                    return;
            }
        } catch (Exception e) {
            sceneManager.showAlert("Erreur", "Erreur lors de la connexion : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void validateEmail() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailError.setText("L'email est requis");
        } else if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            emailError.setText("Format email invalide");
        } else {
            emailError.setText("");
        }
    }

    private void validatePassword() {
        String password = passwordField.getText().trim();
        if (password.isEmpty()) {
            passwordError.setText("Le mot de passe est requis");
        } else if (password.length() < 6) {
            passwordError.setText("Minimum 6 caractères");
        } else {
            passwordError.setText("");
        }
    }

    private boolean validateLoginFields() {
        validateEmail();
        validatePassword();
        return emailError.getText().isEmpty() && passwordError.getText().isEmpty();
    }

    private void handleLogin(ActionEvent event) {
        resetErrors();
        String email = emailField.getText().trim();
        String password = passwordVisible ? visiblePasswordField.getText().trim() : passwordField.getText().trim();

        try {
            if (!validateLoginFields()) {
                return;
            }

            User user = authService.login(email, password);
            System.out.println("Logged in user: " + user.getClass().getSimpleName() +
                    ", user_type: " + user.getUserType() +
                    ", roles: " + user.getRoles());

            switch (user.getUserType()) {
                case "ADMIN":
                    sceneManager.showAdminDashboard(user);
                    break;
                case "MEDECIN":
                    sceneManager.showMedecinDashboard(user);
                    break;
                case "PATIENT":
                    sceneManager.showPatientDashboard(user);
                    break;
                default:
                    errorLabel.setText("Type d'utilisateur non supporté : " + user.getUserType());
                    System.err.println("Unsupported user_type: " + user.getUserType() +
                            ", roles: " + user.getRoles());
                    return;
            }
        } catch (AuthException e) {
            handleAuthError(e);
        } catch (Exception e) {
            System.err.println("Login error: " + e.getClass().getName() +
                    " - " + (e.getMessage() != null ? e.getMessage() : "No message"));
            e.printStackTrace();
            errorLabel.setText("Erreur lors de la connexion : " +
                    (e.getMessage() != null ? e.getMessage() : "Erreur inconnue"));
        }
    }

    private void togglePasswordVisibility(ActionEvent event) {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordButton.getStyleClass().add("visible");
        } else {
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            togglePasswordButton.getStyleClass().remove("visible");
        }
    }

    private void resetErrors() {
        emailError.setText("");
        passwordError.setText("");
        errorLabel.setText("");
        countdownLabel.setText("");
        countdownLabel.setVisible(false);
    }

    private void handleRegister(ActionEvent event) {
        sceneManager.showRegisterScene();
    }

    public void handleForgotPassword(ActionEvent event) {
        Platform.runLater(() -> {
            try {
                System.out.println("handleForgotPassword called");
                cleanup();

                Stage mainStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                if (mainStage == null) {
                    throw new IllegalStateException("Main stage not found");
                }

                TextField emailInput = new TextField();
                emailInput.setPromptText("Entrez votre email");
                emailInput.getStyleClass().add("form-field");
                Label emailInputLabel = new Label("Entrez votre email pour réinitialiser le mot de passe");
                emailInputLabel.getStyleClass().add("input-label");
                Button sendCodeButton = new Button("Envoyer le code");
                sendCodeButton.getStyleClass().add("btn-primary");
                Label errorLabel = new Label();
                errorLabel.getStyleClass().add("error-message");
                errorLabel.setVisible(false);
                Label emailErrorLabel = new Label();
                emailErrorLabel.getStyleClass().add("error-text");
                emailErrorLabel.setVisible(false);

                VBox emailDialogVBox = new VBox(10, emailInputLabel, emailInput, emailErrorLabel, sendCodeButton, errorLabel);
                emailDialogVBox.setPadding(new Insets(20));
                emailDialogVBox.setAlignment(Pos.CENTER);
                emailDialogVBox.getStyleClass().add("form-container");

                Scene dialogScene = new Scene(emailDialogVBox, 350, 250);
                URL cssUrl = getClass().getResource("/css/login.css");
                if (cssUrl != null) {
                    dialogScene.getStylesheets().add(cssUrl.toExternalForm());
                }

                Stage emailDialog = new Stage();
                emailDialog.initModality(Modality.APPLICATION_MODAL);
                emailDialog.initOwner(mainStage);
                emailDialog.setTitle("Réinitialisation du mot de passe");
                emailDialog.setScene(dialogScene);

                emailInput.textProperty().addListener((obs, oldVal, newVal) -> {
                    String email = newVal.trim();
                    if (email.isEmpty()) {
                        emailErrorLabel.setText("L'email est requis");
                        emailErrorLabel.setVisible(true);
                    } else if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                        emailErrorLabel.setText("Format email invalide");
                        emailErrorLabel.setVisible(true);
                    } else {
                        emailErrorLabel.setText("");
                        emailErrorLabel.setVisible(false);
                    }
                });

                sendCodeButton.setOnAction(e -> {
                    String email = emailInput.getText().trim();
                    if (!emailErrorLabel.getText().isEmpty()) return;

                    try {
                        User existingUser = authService.getUserByEmail(email);
                        if (existingUser == null) {
                            errorLabel.setText("Aucun compte trouvé avec cet email");
                            errorLabel.setVisible(true);
                            return;
                        }

                        authService.sendPasswordResetCode(email);
                        emailDialog.close();
                        Platform.runLater(() -> showResetPasswordDialog(email, mainStage));
                    } catch (AuthException ex) {
                        errorLabel.setText(ex.getMessage());
                        errorLabel.setVisible(true);
                    } catch (Exception ex) {
                        errorLabel.setText("Erreur inattendue");
                        errorLabel.setVisible(true);
                    }
                });

                emailDialog.showAndWait();
            } catch (Exception e) {
                sceneManager.showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void showResetPasswordDialog(String email, Stage parentStage) {
        Platform.runLater(() -> {
            try {
                TextField codeField = new TextField();
                codeField.setPromptText("Code de réinitialisation");
                PasswordField newPasswordField = new PasswordField();
                newPasswordField.setPromptText("Nouveau mot de passe");
                PasswordField confirmPasswordField = new PasswordField();
                confirmPasswordField.setPromptText("Confirmer le mot de passe");

                Label codeLabel = new Label("Entrez le code envoyé à " + email);
                Label newPasswordLabel = new Label("Nouveau mot de passe");
                Label confirmPasswordLabel = new Label("Confirmer le mot de passe");
                Button resetButton = new Button("Réinitialiser");

                Label codeErrorLabel = new Label();
                codeErrorLabel.getStyleClass().add("error-text");
                Label passwordErrorLabel = new Label();
                passwordErrorLabel.getStyleClass().add("error-text");
                Label confirmPasswordErrorLabel = new Label();
                confirmPasswordErrorLabel.getStyleClass().add("error-text");
                Label errorLabel = new Label();
                errorLabel.getStyleClass().add("error-message");

                VBox resetDialogVBox = new VBox(10,
                        codeLabel, codeField, codeErrorLabel,
                        newPasswordLabel, newPasswordField, passwordErrorLabel,
                        confirmPasswordLabel, confirmPasswordField, confirmPasswordErrorLabel,
                        resetButton, errorLabel);
                resetDialogVBox.setPadding(new Insets(20));
                resetDialogVBox.setAlignment(Pos.CENTER);
                resetDialogVBox.getStyleClass().add("form-container");

                Scene dialogScene = new Scene(resetDialogVBox, 350, 400);
                URL cssUrl = getClass().getResource("/css/login.css");
                if (cssUrl != null) {
                    dialogScene.getStylesheets().add(cssUrl.toExternalForm());
                }

                Stage resetDialog = new Stage();
                resetDialog.initModality(Modality.APPLICATION_MODAL);
                resetDialog.initOwner(parentStage);
                resetDialog.setTitle("Réinitialisation du mot de passe");
                resetDialog.setScene(dialogScene);

                codeField.textProperty().addListener((obs, oldVal, newVal) -> {
                    String code = newVal.trim();
                    if (code.isEmpty()) {
                        codeErrorLabel.setText("Le code est requis");
                        codeErrorLabel.setVisible(true);
                    } else if (!code.matches("\\d{6}")) {
                        codeErrorLabel.setText("Le code doit être 6 chiffres");
                        codeErrorLabel.setVisible(true);
                    } else {
                        codeErrorLabel.setText("");
                        codeErrorLabel.setVisible(false);
                    }
                });

                newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
                    String password = newVal.trim();
                    String confirmPassword = confirmPasswordField.getText().trim();
                    if (password.isEmpty()) {
                        passwordErrorLabel.setText("Le mot de passe est requis");
                        passwordErrorLabel.setVisible(true);
                    } else if (password.length() < 6) {
                        passwordErrorLabel.setText("Minimum 6 caractères");
                        passwordErrorLabel.setVisible(true);
                    } else {
                        passwordErrorLabel.setText("");
                        passwordErrorLabel.setVisible(false);
                    }
                    if (!confirmPassword.isEmpty()) {
                        if (!confirmPassword.equals(password)) {
                            confirmPasswordErrorLabel.setText("Les mots de passe ne correspondent pas");
                            confirmPasswordErrorLabel.setVisible(true);
                        } else {
                            confirmPasswordErrorLabel.setText("");
                            confirmPasswordErrorLabel.setVisible(false);
                        }
                    }
                });

                confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
                    String confirmPassword = newVal.trim();
                    String password = newPasswordField.getText().trim();
                    if (confirmPassword.isEmpty()) {
                        confirmPasswordErrorLabel.setText("La confirmation est requise");
                        confirmPasswordErrorLabel.setVisible(true);
                    } else if (!confirmPassword.equals(password)) {
                        confirmPasswordErrorLabel.setText("Les mots de passe ne correspondent pas");
                        confirmPasswordErrorLabel.setVisible(true);
                    } else {
                        confirmPasswordErrorLabel.setText("");
                        confirmPasswordErrorLabel.setVisible(false);
                    }
                });

                resetButton.setOnAction(e -> {
                    codeErrorLabel.setText("");
                    passwordErrorLabel.setText("");
                    confirmPasswordErrorLabel.setText("");
                    errorLabel.setText("");

                    String code = codeField.getText().trim();
                    String newPassword = newPasswordField.getText().trim();
                    String confirmPassword = confirmPasswordField.getText().trim();

                    boolean valid = true;
                    if (code.isEmpty()) {
                        codeErrorLabel.setText("Le code est requis");
                        codeErrorLabel.setVisible(true);
                        valid = false;
                    } else if (!code.matches("\\d{6}")) {
                        codeErrorLabel.setText("Le code doit être 6 chiffres");
                        codeErrorLabel.setVisible(true);
                        valid = false;
                    }
                    if (newPassword.isEmpty()) {
                        passwordErrorLabel.setText("Le mot de passe est requis");
                        passwordErrorLabel.setVisible(true);
                        valid = false;
                    } else if (newPassword.length() < 6) {
                        passwordErrorLabel.setText("Minimum 6 caractères");
                        passwordErrorLabel.setVisible(true);
                        valid = false;
                    }
                    if (confirmPassword.isEmpty()) {
                        confirmPasswordErrorLabel.setText("La confirmation est requise");
                        confirmPasswordErrorLabel.setVisible(true);
                        valid = false;
                    } else if (!confirmPassword.equals(newPassword)) {
                        confirmPasswordErrorLabel.setText("Les mots de passe ne correspondent pas");
                        confirmPasswordErrorLabel.setVisible(true);
                        valid = false;
                    }

                    if (valid) {
                        try {
                            authService.resetPassword(email, code, newPassword);
                            resetDialog.close();
                            sceneManager.showAlert("Succès", "Mot de passe réinitialisé !", Alert.AlertType.INFORMATION);
                        } catch (AuthException ex) {
                            errorLabel.setText(ex.getMessage());
                            errorLabel.setVisible(true);
                        }
                    }
                });

                resetDialog.show();
            } catch (Exception e) {
                sceneManager.showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void handleAuthError(AuthException e) {
        String message = e.getMessage();
        if (message.startsWith("Votre compte est bloqué par l'administrateur.") ||
                message.startsWith("Compte verrouillé. Réessayez après")) {
            countdownLabel.setText(message);
            countdownLabel.setVisible(true);
        } else {
            switch (message) {
                case "L'email est requis.":
                case "Aucun compte trouvé avec cet email.":
                    emailError.setText(message);
                    break;
                case "Le mot de passe est requis.":
                case "Mot de passe incorrect.":
                    passwordError.setText(message);
                    break;
                default:
                    errorLabel.setText("Erreur : " + message);
            }
        }
    }

    private static class GoogleTokenResponse {
        String access_token;
    }

    private static class GoogleUserInfo {
        String email;
        String given_name;
        String family_name;
    }
}