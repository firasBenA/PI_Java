package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import tn.esprit.models.User;
import tn.esprit.services.AuthException;
import tn.esprit.services.AuthService;
import tn.esprit.utils.SceneManager;

import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AdminDashboard {
    private static final Logger LOGGER = Logger.getLogger(AdminDashboard.class.getName());
    private static final String DEFAULT_PROFILE_IMAGE = "/images/default-profil.jpg";

    @FXML private VBox root;
    @FXML private Label welcomeLabel;
    @FXML private TextField searchField;
    @FXML private TabPane tabPane;
    @FXML private Tab patientsTab;
    @FXML private Tab medecinsTab;
    @FXML private Tab blockedTab;
    @FXML private FlowPane patientUserCards;
    @FXML private FlowPane medecinUserCards;
    @FXML private FlowPane blockedUserCards;

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
            // Debug: Verify default image
            InputStream defaultImageStream = getClass().getResourceAsStream(DEFAULT_PROFILE_IMAGE);
            LOGGER.info("Default image stream: " + (defaultImageStream != null ? "Found" : "Not found"));
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
            } else if (newTab == blockedTab) {
                displayUsers("BLOCKED");
            }
        });
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            filterUsers(newValue);
        });
    }

    private void displayUsers(String userType) {
        FlowPane targetCards = getTargetCards(userType);
        targetCards.getChildren().clear();
        List<User> filteredUsers = getFilteredUsers(userType);

        LOGGER.info("Displaying " + filteredUsers.size() + " users for type: " + userType);
        for (User user : filteredUsers) {
            targetCards.getChildren().add(createUserCard(user));
        }
    }

    private void filterUsers(String keyword) {
        String selectedTab = tabPane.getSelectionModel().getSelectedItem().getText();
        String userType = selectedTab.equals("Patients") ? "PATIENT" :
                selectedTab.equals("Médecins") ? "MEDECIN" : "BLOCKED";
        FlowPane targetCards = getTargetCards(userType);
        targetCards.getChildren().clear();

        List<User> filteredUsers = getFilteredUsers(userType).stream()
                .filter(user -> user.getNom().toLowerCase().contains(keyword.toLowerCase()) ||
                        user.getPrenom().toLowerCase().contains(keyword.toLowerCase()) ||
                        user.getEmail().toLowerCase().contains(keyword.toLowerCase()))
                .toList();

        LOGGER.info("Filtered " + filteredUsers.size() + " users for type: " + userType + " with keyword: " + keyword);
        for (User user : filteredUsers) {
            targetCards.getChildren().add(createUserCard(user));
        }
    }

    private FlowPane getTargetCards(String userType) {
        return switch (userType) {
            case "PATIENT" -> patientUserCards;
            case "MEDECIN" -> medecinUserCards;
            case "BLOCKED" -> blockedUserCards;
            default -> throw new IllegalArgumentException("Invalid user type: " + userType);
        };
    }

    private List<User> getFilteredUsers(String userType) {
        return allUsers.stream()
                .filter(user -> {
                    if (userType.equals("BLOCKED")) {
                        return user.getRoles().contains("ROLE_BLOCKED");
                    }
                    return user.getUserType().equals(userType) && !user.getRoles().contains("ROLE_BLOCKED");
                })
                .toList();
    }

    private VBox createUserCard(User user) {
        VBox card = new VBox(10);
        card.getStyleClass().add("user-card");

        // Image and Info in HBox
        HBox content = new HBox(15);
        content.getStyleClass().add("card-content");

        // Profile Image
        ImageView profileImage = new ImageView();
        profileImage.getStyleClass().add("user-image");
        profileImage.setFitWidth(80);
        profileImage.setFitHeight(80);

        Image image = loadUserImage(user.getImageProfil());
        profileImage.setImage(image);

        // User Info
        VBox infoBox = new VBox(5);
        Text name = new Text(user.getPrenom() + " " + user.getNom());
        name.getStyleClass().add("user-name");

        Text email = new Text("Email: " + user.getEmail());
        Text role = new Text("Rôle: " + user.getUserType());

        infoBox.getChildren().addAll(name, email, role);
        if (user.getUserType().equals("MEDECIN")) {
            Text specialite = new Text("Spécialité: " + (user.getSpecialite() != null ? user.getSpecialite() : "N/A"));
            infoBox.getChildren().add(specialite);
        }

        content.getChildren().addAll(profileImage, infoBox);

        // Buttons
        HBox buttonBox = new HBox(10);
        Button blockButton = new Button(user.getRoles().contains("ROLE_BLOCKED") ? "Débloquer" : "Bloquer");
        blockButton.getStyleClass().add("block-button");
        blockButton.setOnAction(e -> toggleUserBlockStatus(user, blockButton));

        Button detailsButton = new Button("Afficher Plus de Détails");
        detailsButton.getStyleClass().add("details-button");
        detailsButton.setOnAction(e -> showUserDetails(user));

        buttonBox.getChildren().addAll(blockButton, detailsButton);

        card.getChildren().addAll(content, buttonBox);
        return card;
    }

    private Image loadUserImage(String imagePath) {
        Image image = null;
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                if (imagePath.startsWith("/")) {
                    InputStream imageStream = getClass().getResourceAsStream(imagePath);
                    if (imageStream != null) {
                        image = new Image(imageStream);
                    } else {
                        LOGGER.warning("Image resource not found: " + imagePath);
                    }
                } else {
                    image = new Image(imagePath);
                }
                if (image.isError()) {
                    LOGGER.warning("Failed to load image: " + imagePath + ". Image error: " + image.getException());
                    image = null;
                }
            } catch (Exception e) {
                LOGGER.warning("Failed to load image: " + imagePath + ". Error: " + e.getMessage());
            }
        }

        // Load default image if user image is invalid or missing
        if (image == null || image.isError()) {
            try {
                InputStream defaultStream = getClass().getResourceAsStream(DEFAULT_PROFILE_IMAGE);
                if (defaultStream != null) {
                    image = new Image(defaultStream);
                    if (image.isError()) {
                        LOGGER.severe("Default image error: " + image.getException());
                        image = null;
                    }
                } else {
                    LOGGER.severe("Default profile image not found: " + DEFAULT_PROFILE_IMAGE);
                    // Fallback to a base64-encoded placeholder (1x1 transparent pixel)
                    image = new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=");
                }
            } catch (Exception e) {
                LOGGER.severe("Failed to load default profile image: " + DEFAULT_PROFILE_IMAGE + ". Error: " + e.getMessage());
                image = new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=");
            }
        }

        return image;
    }

    private void showUserDetails(User user) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de l'Utilisateur");
        dialog.setHeaderText(user.getPrenom() + " " + user.getNom());
        dialog.getDialogPane().getStyleClass().add("user-details-dialog");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("details-content");

        // Personal Information Section
        Label personalHeader = new Label("Informations Personnelles");
        personalHeader.getStyleClass().add("section-header");

        GridPane personalGrid = new GridPane();
        personalGrid.setHgap(10);
        personalGrid.setVgap(8);
        personalGrid.getStyleClass().add("details-grid");

        int row = 0;
        personalGrid.add(new Label(String.valueOf(user.getId())), 1, row++);
        personalGrid.add(new Label("Prénom:"), 0, row);
        personalGrid.add(new Label(user.getPrenom()), 1, row++);
        personalGrid.add(new Label("Nom:"), 0, row);
        personalGrid.add(new Label(user.getNom()), 1, row++);
        personalGrid.add(new Label("Email:"), 0, row);
        personalGrid.add(new Label(user.getEmail()), 1, row++);
        personalGrid.add(new Label("Adresse:"), 0, row);
        personalGrid.add(new Label(user.getAdresse()), 1, row++);
        personalGrid.add(new Label("Sexe:"), 0, row);
        personalGrid.add(new Label(user.getSexe()), 1, row++);
        personalGrid.add(new Label("Âge:"), 0, row);
        personalGrid.add(new Label(String.valueOf(user.getAge())), 1, row++);

        // Account Information Section
        Label accountHeader = new Label("Informations du Compte");
        accountHeader.getStyleClass().add("section-header");

        GridPane accountGrid = new GridPane();
        accountGrid.setHgap(10);
        accountGrid.setVgap(8);
        accountGrid.getStyleClass().add("details-grid");

        row = 0;
        accountGrid.add(new Label("Type:"), 0, row);
        accountGrid.add(new Label(user.getUserType()), 1, row++);
        accountGrid.add(new Label("Rôles:"), 0, row);
        accountGrid.add(new Label(String.join(", ", user.getRoles())), 1, row++);
        accountGrid.add(new Label("Verrouillé jusqu'à:"), 0, row);
        accountGrid.add(new Label(user.getLockUntil() != null ?
                user.getLockUntil().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "N/A"), 1, row++);

        // Doctor-Specific Information (if applicable)
        GridPane doctorGrid = null;
        if (user.getUserType().equals("MEDECIN")) {
            Label doctorHeader = new Label("Informations du Médecin");
            doctorHeader.getStyleClass().add("section-header");

            doctorGrid = new GridPane();
            doctorGrid.setHgap(10);
            doctorGrid.setVgap(8);
            doctorGrid.getStyleClass().add("details-grid");

            row = 0;
            doctorGrid.add(new Label("Spécialité:"), 0, row);
            doctorGrid.add(new Label(user.getSpecialite() != null ? user.getSpecialite() : "N/A"), 1, row++);
            doctorGrid.add(new Label("Certificat:"), 0, row);
            doctorGrid.add(new Label(user.getCertificat() != null ? user.getCertificat() : "N/A"), 1, row++);
        }

        // Assemble content
        content.getChildren().addAll(personalHeader, personalGrid, accountHeader, accountGrid);
        if (doctorGrid != null) {
            content.getChildren().addAll(new Label(""), doctorGrid);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/admin.css").toExternalForm());
        dialog.showAndWait();
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
            // Refresh all tabs
            allUsers = authService.getAllUsers();
            String selectedTab = tabPane.getSelectionModel().getSelectedItem().getText();
            String userType = selectedTab.equals("Patients") ? "PATIENT" :
                    selectedTab.equals("Médecins") ? "MEDECIN" : "BLOCKED";
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