package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import tn.esprit.models.User;
import tn.esprit.services.AuthException;
import tn.esprit.services.AuthService;
import tn.esprit.utils.SceneManager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AdminDashboard {
    private static final Logger LOGGER = Logger.getLogger(AdminDashboard.class.getName());
    private static final String DEFAULT_PROFILE_IMAGE = "/images/default_profile.png"; // Ensure this exists in resources

    @FXML private VBox root;
    @FXML private Label welcomeLabel;
    @FXML private TextField searchField;
    @FXML private TabPane tabPane;
    @FXML private Tab patientsTab;
    @FXML private Tab medecinsTab;
    @FXML private VBox patientUserCards;
    @FXML private VBox medecinUserCards;

    private User currentUser;
    private SceneManager sceneManager;
    private AuthService authService;
    private List<User> allUsers = new ArrayList<>();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Bienvenue, " + user.getPrenom() + " " + user.getNom());
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
        initialize();
    }

    private void initialize() {
        try {
            allUsers = authService.getAllUsers();
            setupTabListener();
            setupSearchListener();
            displayUsers("PATIENT");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des utilisateurs: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (AuthException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupTabListener() {
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == patientsTab) {
                displayUsers("PATIENT");
            } else if (newTab == medecinsTab) {
                displayUsers("MEDECIN");
            }
        });
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            filterUsers(newValue);
        });
    }

    private void displayUsers(String userType) {
        VBox targetCards = userType.equals("PATIENT") ? patientUserCards : medecinUserCards;
        targetCards.getChildren().clear();
        List<User> filteredUsers = allUsers.stream()
                .filter(user -> user.getUserType().equals(userType))
                .toList();

        for (User user : filteredUsers) {
            targetCards.getChildren().add(createUserCard(user));
        }
    }

    private void filterUsers(String keyword) {
        String selectedTab = tabPane.getSelectionModel().getSelectedItem().getText();
        String userType = selectedTab.equals("Patients") ? "PATIENT" : "MEDECIN";
        VBox targetCards = userType.equals("PATIENT") ? patientUserCards : medecinUserCards;
        targetCards.getChildren().clear();

        List<User> filteredUsers = allUsers.stream()
                .filter(user -> user.getUserType().equals(userType) &&
                        (user.getNom().toLowerCase().contains(keyword.toLowerCase()) ||
                                user.getPrenom().toLowerCase().contains(keyword.toLowerCase()) ||
                                user.getEmail().toLowerCase().contains(keyword.toLowerCase())))
                .toList();

        for (User user : filteredUsers) {
            targetCards.getChildren().add(createUserCard(user));
        }
    }

    private VBox createUserCard(User user) {
        VBox card = new VBox(10);
        card.getStyleClass().add("user-card");

        // Profile Image
        ImageView profileImage = new ImageView();
        profileImage.setFitWidth(80);
        profileImage.setFitHeight(80);

        String imagePath = user.getImageProfil();
        Image image = null;
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                // Try loading the image as a resource or file
                if (imagePath.startsWith("/")) {
                    // Resource path
                    image = new Image(getClass().getResourceAsStream(imagePath));
                } else {
                    // File path or URL
                    image = new Image(imagePath);
                }
            } catch (Exception e) {
                LOGGER.warning("Failed to load image for user ID: " + user.getId() + ", email: " + user.getEmail() +
                        ", image_profil: " + imagePath + ". Error: " + e.getMessage());
            }
        }
        if (image == null || image.isError()) {
            try {
                image = new Image(getClass().getResourceAsStream(DEFAULT_PROFILE_IMAGE));
            } catch (Exception e) {
                LOGGER.severe("Failed to load default profile image: " + DEFAULT_PROFILE_IMAGE + ". Error: " + e.getMessage());
                image = new Image("file:src/main/resources/images/default_profile.png"); // Fallback
            }
        }
        profileImage.setImage(image);

        // User Info
        Text name = new Text(user.getPrenom() + " " + user.getNom());
        name.getStyleClass().add("user-name");

        Text email = new Text("Email: " + user.getEmail());
        Text adresse = new Text("Adresse: " + user.getAdresse());
        Text sexe = new Text("Sexe: " + user.getSexe());
        Text age = new Text("Âge: " + user.getAge());

        VBox infoBox = new VBox(5, email, adresse, sexe, age);

        if (user.getUserType().equals("MEDECIN")) {
            Text specialite = new Text("Spécialité: " + (user.getSpecialite() != null ? user.getSpecialite() : "N/A"));
            Text certificat = new Text("Certificat: " + (user.getCertificat() != null ? user.getCertificat() : "N/A"));
            infoBox.getChildren().addAll(specialite, certificat);
        }

        // Block/Unblock Button
        Button blockButton = new Button(user.getRoles().contains("ROLE_BLOCKED") ? "Débloquer" : "Bloquer");
        blockButton.getStyleClass().add("block-button");
        blockButton.setOnAction(e -> toggleUserBlockStatus(user, blockButton));

        HBox buttonBox = new HBox(blockButton);
        buttonBox.setSpacing(10);

        card.getChildren().addAll(profileImage, name, infoBox, buttonBox);
        return card;
    }

    private void toggleUserBlockStatus(User user, Button button) {
        try {
            if (user.getRoles().contains("ROLE_BLOCKED")) {
                // Unblock: Restore original role
                List<String> newRoles = new ArrayList<>();
                if (user.getRoles().contains("ORIGINAL_ROLE_ROLE_MEDECIN")) {
                    newRoles.add("ROLE_MEDECIN");
                } else if (user.getRoles().contains("ORIGINAL_ROLE_ROLE_PATIENT")) {
                    newRoles.add("ROLE_PATIENT");
                } else {
                    String restoredRole = user.getUserType().equals("PATIENT") ? "ROLE_PATIENT" : "ROLE_MEDECIN";
                    newRoles.add(restoredRole);
                }
                user.setRoles(newRoles);
                user.setLockUntil(null);
                button.setText("Bloquer");
            } else {
                // Block: Store original role and set to ROLE_BLOCKED
                List<String> newRoles = new ArrayList<>();
                newRoles.add("ROLE_BLOCKED");
                String originalRole = user.getUserType().equals("PATIENT") ? "ROLE_PATIENT" : "ROLE_MEDECIN";
                newRoles.add("ORIGINAL_ROLE_" + originalRole);
                user.setRoles(newRoles);
                user.setLockUntil(java.time.LocalDateTime.now().plusHours(24));
                button.setText("Débloquer");
            }
            authService.updateUser(user);
            showAlert("Succès", "Statut de l'utilisateur mis à jour.", Alert.AlertType.INFORMATION);
            // Refresh the displayed users
            allUsers = authService.getAllUsers(); // Reload users to reflect changes
            String selectedTab = tabPane.getSelectionModel().getSelectedItem().getText();
            String userType = selectedTab.equals("Patients") ? "PATIENT" : "MEDECIN";
            displayUsers(userType);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la mise à jour du statut: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (AuthException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleLogout() {
        sceneManager.showLoginScene();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}