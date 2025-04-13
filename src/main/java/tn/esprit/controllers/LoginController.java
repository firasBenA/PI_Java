package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.models.User;
import tn.esprit.services.AuthException;
import tn.esprit.services.AuthService;
import tn.esprit.utils.SceneManager;
//
//public class LoginController {
//    @FXML private TextField emailField;
//    @FXML private PasswordField passwordField;
//    @FXML private Button loginButton;
//    @FXML private Button registerButton;
//    @FXML private Label errorLabel;
//
//    private AuthService authService;
//    private SceneManager sceneManager;
//
//    public void initialize() {
//        // Initialize services if needed
//    }
//
//    public void setAuthService(AuthService authService) {
//        this.authService = authService;
//    }
//
//    public void setSceneManager(SceneManager sceneManager) {
//        this.sceneManager = sceneManager;
//    }
//
//    @FXML
//    private void handleLogin(ActionEvent event) {
//        String email = emailField.getText();
//        String password = passwordField.getText();
//
//        try {
//            User user = authService.login(email, password);
//
//            // Redirect based on role
//            if (user.getRoles().contains("ROLE_ADMIN")) {
//                sceneManager.showAdminDashboard(user);
//            } else if (user.getRoles().contains("ROLE_MEDECIN")) {
//                sceneManager.showMedecinDashboard(user);
//            } else if (user.getRoles().contains("ROLE_PATIENT")) {
//                sceneManager.showPatientDashboard(user);
//            } else {
//                sceneManager.showHomePage(user);
//            }
//        } catch (AuthException e) {
//            errorLabel.setText(e.getMessage());
//        }
//    }
//
//    @FXML
//    private void handleRegister(ActionEvent event) {
//        sceneManager.showRegisterScene();
//    }
//}


public class LoginController {
        @FXML private TextField emailField;
        @FXML private PasswordField passwordField;
        @FXML private Button loginButton;
        @FXML private Hyperlink registerButton;
        @FXML private Label errorLabel;
        @FXML private Hyperlink forgotPasswordLink;
        @FXML private Label emailError;
        @FXML private Label passwordError;

        private AuthService authService;
        private SceneManager sceneManager;

        public void setAuthService(AuthService authService) {
            this.authService = authService;
        }

        public void setSceneManager(SceneManager sceneManager) {
            this.sceneManager = sceneManager;
        }

        @FXML
        public void initialize() {
            // Set up event handlers
            loginButton.setOnAction(this::handleLogin);
            registerButton.setOnAction(this::handleRegister);
            forgotPasswordLink.setOnAction(this::handleForgotPassword);

            // Validation en temps réel
            emailField.textProperty().addListener((obs, oldVal, newVal) -> validateEmail());
            passwordField.textProperty().addListener((obs, oldVal, newVal) -> validatePassword());

            // Set up enter key press for login
            passwordField.setOnAction(this::handleLogin);
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
        boolean isValid = true;

        validateEmail();
        validatePassword();

        if (!emailError.getText().isEmpty() || !passwordError.getText().isEmpty()) {
            isValid = false;
        }

        return isValid;
    }

    private void handleLogin(ActionEvent event) {
        resetErrors();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        try {
            // Utilisez la validation sans paramètres qui s'appuie sur la validation en temps réel
            if (!validateLoginFields()) {
                return;
            }

            User user = authService.login(email, password);

            // Redirect based on role
            if (user.getRoles().contains("ROLE_ADMIN")) {
                sceneManager.showAdminDashboard(user);
            } else if (user.getRoles().contains("ROLE_MEDECIN")) {
                sceneManager.showMedecinDashboard(user);
            } else if (user.getRoles().contains("ROLE_PATIENT")) {
                sceneManager.showPatientDashboard(user);
            } else {
                sceneManager.showHomePage(user);
            }
        } catch (AuthException e) {
            handleAuthError(e);
        } catch (Exception e) {
            errorLabel.setText("Une erreur inattendue s'est produite: " + e.getMessage());
            e.printStackTrace();
        }
    }

        private void resetErrors() {
            emailError.setText("");
            passwordError.setText("");
            errorLabel.setText("");
        }

//        private boolean validateLoginFields(String email, String password) {
//            boolean isValid = true;
//
//            if (email.isEmpty()) {
//                emailError.setText("L'email est requis");
//                isValid = false;
//            } else if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
//                emailError.setText("Format email invalide");
//                isValid = false;
//            }
//
//            if (password.isEmpty()) {
//                passwordError.setText("Le mot de passe est requis");
//                isValid = false;
//            }
//
//            return isValid;
//        }

        private void handleRegister(ActionEvent event) {
            sceneManager.showRegisterScene();
        }

        private void handleForgotPassword(ActionEvent event) {
            sceneManager.showForgotPasswordScene();
        }

        private void handleAuthError(AuthException e) {
            switch(e.getMessage()) {
                case "Aucun compte trouvé avec cet email.":
                    emailError.setText(e.getMessage());
                    break;
                case "Mot de passe incorrect.":
                    passwordError.setText(e.getMessage());
                    break;
                default:
                    errorLabel.setText(e.getMessage());
            }
        }
    }
