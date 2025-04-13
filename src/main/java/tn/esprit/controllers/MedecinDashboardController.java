package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import tn.esprit.models.User;
import tn.esprit.services.AuthException;
import tn.esprit.services.AuthService;
import tn.esprit.utils.SceneManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

public class MedecinDashboardController {
    // Champs FXML
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField specialiteField;
    @FXML private TextArea adresseField;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;
    @FXML private Button deconnexionBtn;
    @FXML private Label messageLabel;
    @FXML private ImageView profileImage;
    @FXML private Button changeImageBtn;
    @FXML private Label welcomeLabel;
    @FXML private Label specialiteLabel;
    @FXML private ComboBox<String> sexeComboBox;
   // @FXML private TextField certificatField;
    @FXML private ImageView certificatImageView;
    @FXML private Button changeCertificatBtn;
    @FXML private Label nomErrorLabel;
    @FXML private Label prenomErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label telErrorLabel;
    @FXML private Label specialiteErrorLabel;
    @FXML private Label adresseErrorLabel;
    @FXML private Label sexeErrorLabel;
    @FXML private Label certificatErrorLabel;

    private File selectedCertificatFile;

    private User currentUser;
    private AuthService authService;
    private SceneManager sceneManager;
    private File selectedImageFile;


    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8,15}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s-]{2,50}$");

    @FXML
    public void initialize() {
        configureButtonActions();
        disableFields();
        initializeSexeComboBox();
        configureCertificatButton();
        clearErrorMessages();
        messageLabel.getStyleClass().add("status-message");

    }

    private void clearErrorMessages() {
        nomErrorLabel.setText("");
        prenomErrorLabel.setText("");
        emailErrorLabel.setText("");
        telErrorLabel.setText("");
        specialiteErrorLabel.setText("");
        adresseErrorLabel.setText("");
        sexeErrorLabel.setText("");
        certificatErrorLabel.setText("");

    }

    private void configureCertificatButton() {
        changeCertificatBtn.setOnAction(e -> handleChangeCertificat());
    }
    private void handleChangeCertificat() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un certificat");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.pdf")
        );

        selectedCertificatFile = fileChooser.showOpenDialog(certificatImageView.getScene().getWindow());
        if (selectedCertificatFile != null) {
            try {
                // Sauvegarder le fichier dans le dossier des certificats
                Path certificatsDir = Paths.get("src/main/resources/certificats");
                if (!Files.exists(certificatsDir)) {
                    Files.createDirectories(certificatsDir);
                }

                String fileName = "certificat_" + currentUser.getId() + "_" + System.currentTimeMillis() +
                        getFileExtension(selectedCertificatFile.getName());
                Path destination = certificatsDir.resolve(fileName);
                Files.copy(selectedCertificatFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                // Mettre à jour l'affichage et l'utilisateur
                if (selectedCertificatFile.getName().toLowerCase().endsWith(".pdf")) {
                    // Pour les PDF, afficher une icône spécifique
                    certificatImageView.setImage(new Image(getClass().getResourceAsStream("/images/pdf-icon.png")));
                } else {
                    // Pour les images, afficher l'image
                    certificatImageView.setImage(new Image(destination.toUri().toString()));
                }

                currentUser.setCertificat(destination.toString());
                showSuccessMessage("Certificat mis à jour avec succès");
            } catch (IOException e) {
                showErrorMessage("Erreur lors de la mise à jour du certificat");
                e.printStackTrace();
            }
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        // Normalisation pour les chemins Windows/Unix
        String normalizedFileName = fileName.replace('\\', '/');
        int lastDot = normalizedFileName.lastIndexOf('.');
        int lastSeparator = normalizedFileName.lastIndexOf('/');

        // Vérification des cas limites
        if (lastDot == -1 || (lastSeparator != -1 && lastDot < lastSeparator)) {
            return "";
        }

        return normalizedFileName.substring(lastDot).toLowerCase(); // Extension en minuscules
    }


    private void initializeSexeComboBox() {
        sexeComboBox.getItems().addAll("Homme", "Femme", "Autre");
    }

    // Setters
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserData();
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    private void handleChangeImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(profileImage.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // 1. Enregistrer l'image dans le dossier
                Path destination = saveImageToProfileFolder(selectedFile);

                // 2. Mettre à jour l'utilisateur
                currentUser.setImageProfil(destination.toString());

                // 3. Sauvegarder dans la base de données
                authService.updateUser(currentUser);

                // 4. Mettre à jour l'affichage
                profileImage.setImage(new Image(destination.toUri().toString()));

                showSuccessMessage("Photo de profil mise à jour avec succès");
            } catch (IOException e) {
                showErrorMessage("Erreur lors de l'enregistrement de l'image");
                e.printStackTrace();
            } catch (AuthException e) {
                showErrorMessage("Erreur lors de la mise à jour en base de données");
                e.printStackTrace();
            }
        }
    }

    private Path saveImageToProfileFolder(File imageFile) throws IOException {
        // Créer le dossier s'il n'existe pas
        Path profileDir = Paths.get("src/main/resources/profile_images");
        if (!Files.exists(profileDir)) {
            Files.createDirectories(profileDir);
        }

        // Générer un nom de fichier unique
        String fileName = "profile_" + currentUser.getId() + "_"
                + System.currentTimeMillis() + getFileExtension(imageFile.getName());

        // Copier le fichier
        Path destination = profileDir.resolve(fileName);
        Files.copy(imageFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        return destination;
    }




    @FXML
    private void handleDeconnexion() {
        sceneManager.showLoginScene();
    }

    @FXML
    private void handleModification() {
        if (modifierBtn.getText().equals("Modifier Profil")) {
            enableFields();
            modifierBtn.setText("Enregistrer");
            clearErrorMessages();
        } else {
            if (saveChanges()) {
                showSuccessMessage("Profil mis à jour avec succès");
                disableFields();
                modifierBtn.setText("Modifier Profil");
            }
        }
    }

    @FXML
    private void handleSuppression() {
        confirmAndDeleteAccount();
    }

    // Méthodes utilitaires
    private void configureButtonActions() {
        changeImageBtn.setOnAction(e -> handleChangeImage());
        deconnexionBtn.setOnAction(e -> handleDeconnexion());
        modifierBtn.setOnAction(e -> handleModification());
        supprimerBtn.setOnAction(e -> handleSuppression());
    }

    private void loadUserData() {
        if (currentUser != null) {
            nomField.setText(currentUser.getNom());
            prenomField.setText(currentUser.getPrenom());
            emailField.setText(currentUser.getEmail());
            telephoneField.setText(currentUser.getTelephone());
            specialiteField.setText(currentUser.getSpecialite());
            adresseField.setText(currentUser.getAdresse());
            sexeComboBox.setValue(currentUser.getSexe());
            //certificatField.setText(currentUser.getCertificat());

            welcomeLabel.setText("Bienvenue Dr. " + currentUser.getPrenom() + " " + currentUser.getNom());
            specialiteLabel.setText("Spécialité: " + currentUser.getSpecialite());

            loadProfileImage();

        }
        if (currentUser.getCertificat() != null && !currentUser.getCertificat().isEmpty()) {
            try {
                if (currentUser.getCertificat().toLowerCase().endsWith(".pdf")) {
                    certificatImageView.setImage(new Image(getClass().getResourceAsStream("/images/pdf-icon.png")));
                } else {
                    certificatImageView.setImage(new Image(new File(currentUser.getCertificat()).toURI().toString()));
                }
            } catch (Exception e) {
                System.err.println("Erreur de chargement du certificat: " + e.getMessage());
                certificatImageView.setImage(new Image(getClass().getResourceAsStream("/images/default-certificat.png")));
            }
        }

    }

    private void loadProfileImage() {
        try {
            if (currentUser.getImageProfil() != null && !currentUser.getImageProfil().isEmpty()) {
                File file = new File(currentUser.getImageProfil());
                if (file.exists()) {
                    profileImage.setImage(new Image(file.toURI().toString()));
                    return;
                }
            }
            profileImage.setImage(new Image(getClass().getResourceAsStream("/images/default-profile.png")));
        } catch (Exception e) {
            System.err.println("Erreur de chargement de l'image: " + e.getMessage());
            profileImage.setImage(new Image(getClass().getResourceAsStream("/images/default-profil.jpg")));
        }
    }

    private void enableFields() {
        setFieldsEditable(true);
        emailField.setEditable(false);
    }

    private void disableFields() {
        setFieldsEditable(false);
    }

    private void setFieldsEditable(boolean editable) {
        nomField.setEditable(editable);
        prenomField.setEditable(editable);
        telephoneField.setEditable(editable);
        specialiteField.setEditable(editable);
        adresseField.setEditable(editable);

        sexeComboBox.setDisable(!editable);
        changeCertificatBtn.setDisable(!editable);
        if (editable) {
            sexeComboBox.setStyle("-fx-opacity: 1;");
            certificatImageView.setStyle("-fx-opacity: 1;");
        } else {
            sexeComboBox.setStyle("-fx-opacity: 1;");
            certificatImageView.setStyle("-fx-opacity: 0.7;");
        }
    }

    private boolean saveChanges() {
        try {
            if (validateFields()) {
                updateUserFromFields();
                authService.updateUser(currentUser);
                return true;
            }
            return false;
        } catch (Exception e) {
            showErrorMessage("Erreur lors de la mise à jour: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (AuthException e) {
            throw new RuntimeException(e);
        }

    }

    private boolean validateFields() {
        boolean isValid = true;
        clearErrorMessages();

        // Validation du nom
        if (nomField.getText().isEmpty()) {
            nomErrorLabel.setText("Le nom est obligatoire");
            isValid = false;
        } else if (!NAME_PATTERN.matcher(nomField.getText()).matches()) {
            nomErrorLabel.setText("Nom invalide (2-50 caractères alphabétiques)");
            isValid = false;
        }

        // Validation du prénom
        if (prenomField.getText().isEmpty()) {
            prenomErrorLabel.setText("Le prénom est obligatoire");
            isValid = false;
        } else if (!NAME_PATTERN.matcher(prenomField.getText()).matches()) {
            prenomErrorLabel.setText("Prénom invalide (2-50 caractères alphabétiques)");
            isValid = false;
        }

        // Validation de l'email
        if (emailField.getText().isEmpty()) {
            emailErrorLabel.setText("L'email est obligatoire");
            isValid = false;
        } else if (!EMAIL_PATTERN.matcher(emailField.getText()).matches()) {
            emailErrorLabel.setText("Email invalide");
            isValid = false;
        }

        // Validation du téléphone
        if (telephoneField.getText().isEmpty()) {
            telErrorLabel.setText("Le téléphone est obligatoire");
            isValid = false;
        } else if (!PHONE_PATTERN.matcher(telephoneField.getText()).matches()) {
            telErrorLabel.setText("Téléphone invalide (8-15 chiffres)");
            isValid = false;
        }

        // Validation de la spécialité
        if (specialiteField.getText().isEmpty()) {
            specialiteErrorLabel.setText("La spécialité est obligatoire");
            isValid = false;
        }

        // Validation de l'adresse
        if (adresseField.getText().isEmpty()) {
            adresseErrorLabel.setText("L'adresse est obligatoire");
            isValid = false;
        }

        // Validation du sexe
        if (sexeComboBox.getValue() == null || sexeComboBox.getValue().isEmpty()) {
            sexeErrorLabel.setText("Veuillez sélectionner un sexe");
            isValid = false;
        }

        // Validation du certificat
        if (currentUser.getCertificat() == null || currentUser.getCertificat().isEmpty()) {
            certificatErrorLabel.setText("Le certificat est obligatoire");
            isValid = false;
        }

        return isValid;
    }


    private boolean validateField(TextField field, Pattern pattern, String errorMessage) {
        if (field.getText().isEmpty() || !pattern.matcher(field.getText()).matches()) {
            showErrorMessage(errorMessage);
            field.requestFocus();
            return false;
        }
        return true;
    }


    private void updateUserFromFields() {
        currentUser.setNom(nomField.getText().trim());
        currentUser.setPrenom(prenomField.getText().trim());
        currentUser.setTelephone(telephoneField.getText().trim());
        currentUser.setSpecialite(specialiteField.getText().trim());
        currentUser.setAdresse(adresseField.getText().trim());
        currentUser.setSexe(sexeComboBox.getValue());
        //currentUser.setCertificat(certificatField.getText().trim());
    }

    private void confirmAndDeleteAccount() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Suppression du compte médecin");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer définitivement votre compte ? Cette action est irréversible.");

        // Personnalisation des boutons
        ButtonType confirmButton = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        // Application du style moderne
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/patient.css").toExternalForm());
        dialogPane.getStyleClass().add("confirmation-dialog");

        // Affichage et attente de la réponse
        alert.showAndWait().ifPresent(response -> {
            if (response == confirmButton) {
                try {
                    authService.deleteUser(currentUser.getId());
                    sceneManager.showLoginScene();
                    showSuccessMessage("Compte supprimé avec succès");
                } catch (Exception e) {
                    showErrorMessage("Échec de la suppression: " + e.getMessage());
                    e.printStackTrace();
                } catch (AuthException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void showSuccessMessage(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().remove("error");
        messageLabel.getStyleClass().add("success");

        // Masquer le message après 3 secondes
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> {
                    messageLabel.setText("");
                    messageLabel.getStyleClass().remove("success");
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showErrorMessage(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().remove("success");
        messageLabel.getStyleClass().add("error");
    }
}