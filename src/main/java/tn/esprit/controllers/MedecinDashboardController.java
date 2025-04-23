package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import tn.esprit.models.Medecin;
import tn.esprit.models.User;
import tn.esprit.services.AuthException;
import tn.esprit.services.AuthService;
import tn.esprit.utils.SceneManager;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class MedecinDashboardController {
    @FXML private ImageView profileImage;
    @FXML private Button changeImageBtn;
    @FXML private Label welcomeLabel;
    @FXML private Label specialiteLabel;
    @FXML private Button deconnexionBtn;
    @FXML private Label messageLabel;
    @FXML private TextField nomField;
    @FXML private Label nomErrorLabel;
    @FXML private TextField prenomField;
    @FXML private Label prenomErrorLabel;
    @FXML private TextField emailField;
    @FXML private Label emailErrorLabel;
    @FXML private TextField telephoneField;
    @FXML private Label telErrorLabel;
    @FXML private TextField specialiteField;
    @FXML private Label specialiteErrorLabel;
    @FXML private TextArea adresseField;
    @FXML private Label adresseErrorLabel;
    @FXML private ComboBox<String> sexeComboBox;
    @FXML private Label sexeErrorLabel;
    @FXML private Hyperlink certificatLink;
    @FXML private Button changeCertificatBtn;
    @FXML private Label certificatErrorLabel;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;

    private AuthService authService;
    private SceneManager sceneManager;
    private Medecin currentUser;
    private File selectedImageFile;
    private boolean isEditing = false;

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public void setCurrentUser(User user) {
        if (user instanceof Medecin) {
            this.currentUser = (Medecin) user;
            loadUserData();
        } else {
            showAlert("Erreur", "Utilisateur invalide pour le tableau de bord Médecin", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void initialize() {
        sexeComboBox.getItems().addAll("Homme", "Femme", "Autre");
        // Fields are set to read-only in FXML via editable="false" and disable="true"
        modifierBtn.setText("Modifier Profil");
    }

    private void loadUserData() {
        if (currentUser == null) {
            return;
        }

        nomField.setText(currentUser.getNom() != null ? currentUser.getNom() : "");
        prenomField.setText(currentUser.getPrenom() != null ? currentUser.getPrenom() : "");
        emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        telephoneField.setText(currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
        specialiteField.setText(currentUser.getSpecialite() != null ? currentUser.getSpecialite() : "");
        adresseField.setText(currentUser.getAdresse() != null ? currentUser.getAdresse() : "");
        sexeComboBox.setValue(currentUser.getSexe());
        welcomeLabel.setText("Bienvenue, Dr. " + (currentUser.getPrenom() != null ? currentUser.getPrenom() : "") +
                " " + (currentUser.getNom() != null ? currentUser.getNom() : ""));
        specialiteLabel.setText(currentUser.getSpecialite() != null ? currentUser.getSpecialite() : "Spécialité non définie");

        // Load profile image
        if (currentUser.getImageProfil() != null && !currentUser.getImageProfil().isEmpty()) {
            try {
                File imageFile = new File(currentUser.getImageProfil());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    profileImage.setImage(image);
                } else {
                    System.err.println("Image file not found: " + currentUser.getImageProfil());
                    profileImage.setImage(null);
                }
            } catch (Exception e) {
                System.err.println("Erreur de chargement de l'image: " + e.getMessage());
                e.printStackTrace();
                profileImage.setImage(null);
            }
        } else {
            System.out.println("No profile image for user: " + currentUser.getEmail());
            profileImage.setImage(null);
        }

        // Load certificate path
        if (currentUser.getCertificat() != null && !currentUser.getCertificat().isEmpty()) {
            try {
                File certFile = new File(currentUser.getCertificat());
                if (certFile.exists()) {
                    certificatLink.setText(certFile.getName());
                } else {
                    System.err.println("Certificate file not found: " + currentUser.getCertificat());
                    certificatLink.setText("Certificat introuvable");
                }
            } catch (Exception e) {
                System.err.println("Erreur de chargement du certificat: " + e.getMessage());
                e.printStackTrace();
                certificatLink.setText("Erreur de certificat");
            }
        } else {
            System.out.println("No certificate for user: " + currentUser.getEmail());
            certificatLink.setText("Aucun certificat");
        }
    }

    @FXML
    public void handleChangeImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(profileImage.getScene().getWindow());
        if (file != null) {
            try {
                String imagePath = saveFile(file, "images");
                currentUser.setImageProfil(imagePath);
                Image image = new Image(file.toURI().toString());
                profileImage.setImage(image);
                authService.updateUser(currentUser);
                showAlert("Succès", "Image de profil mise à jour", Alert.AlertType.INFORMATION);
            } catch (AuthException e) {
                messageLabel.setText(e.getMessage());
                messageLabel.setStyle("-fx-text-fill: red;");
            } catch (Exception e) {
                messageLabel.setText("Erreur lors du changement d'image");
                messageLabel.setStyle("-fx-text-fill: red;");
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleChangeCertificat() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        File file = fileChooser.showOpenDialog(changeCertificatBtn.getScene().getWindow());
        if (file != null) {
            try {
                String certificatPath = saveFile(file, "certificats");
                currentUser.setCertificat(certificatPath);
                certificatLink.setText(file.getName());
                authService.updateUser(currentUser);
                showAlert("Succès", "Certificat mis à jour", Alert.AlertType.INFORMATION);
            } catch (AuthException e) {
                messageLabel.setText(e.getMessage());
                messageLabel.setStyle("-fx-text-fill: red;");
            } catch (Exception e) {
                messageLabel.setText("Erreur lors du changement de certificat");
                messageLabel.setStyle("-fx-text-fill: red;");
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void openCertificat() {
        if (currentUser.getCertificat() != null && !currentUser.getCertificat().isEmpty()) {
            try {
                File certFile = new File(currentUser.getCertificat());
                if (certFile.exists()) {
                    Desktop.getDesktop().open(certFile);
                } else {
                    showAlert("Erreur", "Le fichier certificat n'existe pas.", Alert.AlertType.ERROR);
                }
            } catch (IOException e) {
                showAlert("Erreur", "Impossible d'ouvrir le certificat: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        } else {
            showAlert("Information", "Aucun certificat n'est associé.", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    public void handleDeconnexion() {
        try {
            sceneManager.showLoginScene();
            showAlert("Succès", "Déconnexion réussie", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            messageLabel.setText("Erreur lors de la déconnexion");
            messageLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleModification() {
        if (!isEditing) {
            // Enter edit mode
            isEditing = true;
            modifierBtn.setText("Enregistrer");
            setFieldsEditable(true);
            messageLabel.setText("Modifiez les champs, puis cliquez sur Enregistrer.");
            messageLabel.setStyle("-fx-text-fill: blue;");
        } else {
            // Save changes
            clearErrorLabels();
            try {
                // Validate inputs
                if (nomField.getText().trim().isEmpty()) {
                    nomErrorLabel.setText("Le nom est requis");
                    return;
                }
                if (prenomField.getText().trim().isEmpty()) {
                    prenomErrorLabel.setText("Le prénom est requis");
                    return;
                }
                if (!emailField.getText().trim().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                    emailErrorLabel.setText("Email invalide");
                    return;
                }
                if (telephoneField.getText().trim().isEmpty()) {
                    telErrorLabel.setText("Le téléphone est requis");
                    return;
                }
                if (specialiteField.getText().trim().isEmpty()) {
                    specialiteErrorLabel.setText("La spécialité est requise");
                    return;
                }
                if (adresseField.getText().trim().isEmpty()) {
                    adresseErrorLabel.setText("L'adresse est requise");
                    return;
                }
                if (sexeComboBox.getValue() == null) {
                    sexeErrorLabel.setText("Le sexe est requis");
                    return;
                }

                // Update user
                currentUser.setNom(nomField.getText().trim());
                currentUser.setPrenom(prenomField.getText().trim());
                currentUser.setEmail(emailField.getText().trim());
                currentUser.setTelephone(telephoneField.getText().trim());
                currentUser.setSpecialite(specialiteField.getText().trim());
                currentUser.setAdresse(adresseField.getText().trim());
                currentUser.setSexe(sexeComboBox.getValue());

                authService.updateUser(currentUser);
                welcomeLabel.setText("Bienvenue, Dr. " + currentUser.getPrenom() + " " + currentUser.getNom());
                specialiteLabel.setText(currentUser.getSpecialite());
                messageLabel.setText("Profil mis à jour avec succès");
                messageLabel.setStyle("-fx-text-fill: green;");

                // Exit edit mode
                isEditing = false;
                modifierBtn.setText("Modifier Profil");
                setFieldsEditable(false);
            } catch (AuthException e) {
                messageLabel.setText(e.getMessage());
                messageLabel.setStyle("-fx-text-fill: red;");
            } catch (Exception e) {
                messageLabel.setText("Erreur lors de la mise à jour");
                messageLabel.setStyle("-fx-text-fill: red;");
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleSuppression() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le compte");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer votre compte ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    authService.deleteUser(currentUser.getId());
                    sceneManager.showLoginScene();
                    showAlert("Succès", "Compte supprimé avec succès", Alert.AlertType.INFORMATION);
                } catch (AuthException e) {
                    messageLabel.setText(e.getMessage());
                    messageLabel.setStyle("-fx-text-fill: red;");
                } catch (Exception e) {
                    messageLabel.setText("Erreur lors de la suppression");
                    messageLabel.setStyle("-fx-text-fill: red;");
                    e.printStackTrace();
                }
            }
        });
    }

    private void setFieldsEditable(boolean editable) {
        nomField.setEditable(editable);
        prenomField.setEditable(editable);
        emailField.setEditable(editable);
        telephoneField.setEditable(editable);
        specialiteField.setEditable(editable);
        adresseField.setEditable(editable);
        sexeComboBox.setDisable(!editable);
    }

    private String saveFile(File file, String directory) throws IOException {
        Path targetDir = Paths.get("src/main/resources/" + directory);
        Files.createDirectories(targetDir);
        Path targetPath = targetDir.resolve(file.getName());
        Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath.toString();
    }

    private void clearErrorLabels() {
        nomErrorLabel.setText("");
        prenomErrorLabel.setText("");
        emailErrorLabel.setText("");
        telErrorLabel.setText("");
        specialiteErrorLabel.setText("");
        adresseErrorLabel.setText("");
        sexeErrorLabel.setText("");
        certificatErrorLabel.setText("");
        messageLabel.setText("");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}