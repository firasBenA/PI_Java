package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import tn.esprit.models.Admin;
import tn.esprit.models.Medecin;
import tn.esprit.models.Patient;
import tn.esprit.models.User;
import tn.esprit.services.AuthException;
import tn.esprit.services.AuthService;
import tn.esprit.utils.SceneManager;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class RegisterController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField ageField;
    @FXML private TextField adresseField;
    @FXML private ComboBox<String> sexeComboBox;
    @FXML private TextField telephoneField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField specialiteField;
    @FXML private Label certificatPathLabel;
    @FXML private Label imagePathLabel;
    @FXML private VBox medecinFields;

    // Messages d'erreur
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

    // Contrôles password
    @FXML private HBox passwordContainer;
    @FXML private Button togglePasswordButton;
    private TextField visiblePasswordField;
    private boolean passwordVisible = false;

    private File certificatFile;
    private File imageFile;

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
        initializeFields();
        setupRoleListener();
        setupPasswordToggle();
    }

    private void initializeFields() {
        sexeComboBox.getItems().addAll("Homme", "Femme", "Autre");
        roleComboBox.getItems().addAll("ROLE_PATIENT", "ROLE_MEDECIN");
        visiblePasswordField = new TextField();
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setStyle(passwordField.getStyle());
        visiblePasswordField.prefWidthProperty().bind(passwordField.prefWidthProperty());
        passwordContainer.getChildren().add(visiblePasswordField);
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
            case "ROLE_ADMIN":
                user = new Admin();
                break;
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
}