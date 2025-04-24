package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.models.Admin;
import tn.esprit.models.Medecin;
import tn.esprit.models.Patient;
import tn.esprit.models.User;
import tn.esprit.services.AuthException;
import tn.esprit.services.AuthService;
import tn.esprit.utils.SceneManager;

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

    private AuthService authService;
    private SceneManager sceneManager;
    private boolean isPasswordVisible;

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    public void initialize() {
        loginButton.setOnAction(this::handleLogin);
        registerButton.setOnAction(this::handleRegister);
        forgotPasswordLink.setOnAction(this::handleForgotPassword);
        togglePasswordButton.setOnAction(this::togglePasswordVisibility);
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateEmail());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePassword();
            if (!isPasswordVisible) {
                visiblePasswordField.setText(newVal);
            }
        });
        visiblePasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isPasswordVisible) {
                passwordField.setText(newVal);
            }
        });
        passwordField.setOnAction(this::handleLogin);
        isPasswordVisible = false;
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
        String password = isPasswordVisible ? visiblePasswordField.getText().trim() : passwordField.getText().trim();

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
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
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

    private void handleForgotPassword(ActionEvent event) {
        sceneManager.showForgotPasswordScene();
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
                    errorLabel.setText("Erreur de connexion : " + message);
            }
        }
    }
}