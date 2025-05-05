package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tn.esprit.models.User;
import tn.esprit.services.AuthException;
import tn.esprit.services.AuthService;
import tn.esprit.utils.SceneManager;

import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminDashboard {
    private static final Logger LOGGER = Logger.getLogger(AdminDashboard.class.getName());
    private static final String DEFAULT_PROFILE_IMAGE = "/images/default-profil.jpg";
    private static final String FALLBACK_IMAGE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="; // 1x1 transparent pixel

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
            // Warn about invalid image_profil and certificat values
            allUsers.forEach(user -> {
                String imagePath = user.getImageProfil();
                if (imagePath != null && !imagePath.isEmpty()) {
                    boolean isValid = imagePath.startsWith("/images/") || imagePath.startsWith("http") || new File(imagePath).exists();
                    if (!isValid) {
                        LOGGER.warning("Invalid image_profil for user ID: " + user.getId() +
                                ", email: " + user.getEmail() + ", path: " + imagePath);
                    }
                }
                String certificatPath = user.getCertificat();
                if (certificatPath != null && !certificatPath.isEmpty()) {
                    boolean isValid = certificatPath.endsWith(".pdf") && new File(certificatPath).exists();
                    if (!isValid) {
                        LOGGER.warning("Invalid certificat for user ID: " + user.getId() +
                                ", email: " + user.getEmail() + ", path: " + certificatPath);
                    }
                }
            });
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
        if (filteredUsers.isEmpty()) {
            Label emptyLabel = new Label(getEmptyMessage(userType));
            emptyLabel.getStyleClass().add("empty-label");
            targetCards.setAlignment(Pos.CENTER);
            targetCards.getChildren().add(emptyLabel);
        } else {
            targetCards.setAlignment(Pos.TOP_LEFT);
            for (User user : filteredUsers) {
                targetCards.getChildren().add(createUserCard(user));
            }
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
        if (filteredUsers.isEmpty()) {
            Label emptyLabel = new Label(getEmptyMessage(userType));
            emptyLabel.getStyleClass().add("empty-label");
            targetCards.setAlignment(Pos.CENTER);
            targetCards.getChildren().add(emptyLabel);
        } else {
            targetCards.setAlignment(Pos.TOP_LEFT);
            for (User user : filteredUsers) {
                targetCards.getChildren().add(createUserCard(user));
            }
        }
    }

    private String getEmptyMessage(String userType) {
        return switch (userType) {
            case "PATIENT" -> "Aucun patient trouvé.";
            case "MEDECIN" -> "Aucun médecin trouvé.";
            case "BLOCKED" -> "Aucun utilisateur bloqué trouvé.";
            default -> "Aucune donnée disponible.";
        };
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
        profileImage.setPreserveRatio(false); // Force 80x80 even if image aspect ratio differs

        Image image = loadUserImage(user.getImageProfil());
        if (image != null) {
            profileImage.setImage(image);
        } else {
            profileImage.setImage(new Image(getClass().getResourceAsStream("/images/default-profile.png")));
        }

        // User Info
        VBox infoBox = new VBox(5);
        infoBox.getStyleClass().add("user-info-box");

        // Truncate name to prevent wrapping
        String fullName = user.getPrenom() + " " + user.getNom();
        String displayName = fullName.length() > 20 ? fullName.substring(0, 17) + "..." : fullName;
        Text name = new Text(displayName);
        name.getStyleClass().add("user-name");

        // Truncate email to prevent wrapping
        String displayEmail = user.getEmail().length() > 22 ? user.getEmail().substring(0, 19) + "..." : user.getEmail();
        Text email = new Text("Email: " + displayEmail);

        Text role = new Text("Rôle: " + user.getUserType());

        // Reserve space for specialty (even for patients)
        String specialiteText = user.getUserType().equals("MEDECIN") ?
                "Spécialité: " + (user.getSpecialite() != null && user.getSpecialite().length() > 20 ?
                        user.getSpecialite().substring(0, 17) + "..." : user.getSpecialite() != null ? user.getSpecialite() : "N/A") : "";
        Text specialite = new Text(specialiteText);
        infoBox.getChildren().addAll(name, email, role, specialite);

        content.getChildren().addAll(profileImage, infoBox);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.getStyleClass().add("button-box");
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
                // Try absolute file path
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    image = new Image(imageFile.toURI().toString());
                } else {
                    // Try resource path
                    if (imagePath.startsWith("/")) {
                        InputStream imageStream = getClass().getResourceAsStream(imagePath);
                        if (imageStream != null) {
                            image = new Image(imageStream);
                        } else {
                            LOGGER.warning("Image resource not found: " + imagePath);
                        }
                    } else if (imagePath.startsWith("http")) {
                        image = new Image(imagePath);
                    }
                }
                if (image != null && image.isError()) {
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
                    LOGGER.info("Using base64 fallback image");
                    image = new Image(FALLBACK_IMAGE);
                }
            } catch (Exception e) {
                LOGGER.severe("Failed to load default profile image: " + DEFAULT_PROFILE_IMAGE + ". Error: " + e.getMessage());
                LOGGER.info("Using base64 fallback image");
                image = new Image(FALLBACK_IMAGE);
            }
        }

        return image;
    }
    private void showUserDetails(User user) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de l'Utilisateur");

        // Set dialog owner and make non-resizable
        Stage stage = (Stage) root.getScene().getWindow();
        dialog.initOwner(stage);
        dialog.setResizable(false);

        // Custom header with gradient and close button
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.getStyleClass().add("header-panel");

        Label headerLabel = new Label(user.getPrenom() + " " + user.getNom());
        headerLabel.getStyleClass().add("header-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("✕");
        closeButton.getStyleClass().add("close-button");
        closeButton.setFocusTraversable(true);
        closeButton.setOnAction(e -> {
            LOGGER.info("Close button clicked for user: " + user.getEmail());
            dialog.close();
        });

        header.getChildren().addAll(headerLabel, spacer, closeButton);
        dialog.getDialogPane().setHeader(header);

        // Scrollable content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("dialog-scroll-pane");

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("details-content");

        // Profile Image with Card
        VBox imageCard = new VBox(10);
        imageCard.getStyleClass().add("info-card");
        imageCard.setAlignment(Pos.CENTER);

        ImageView profileImage = new ImageView(loadUserImage(user.getImageProfil()));
        profileImage.getStyleClass().add("profile-image");
        profileImage.setFitWidth(120);
        profileImage.setFitHeight(120);

        Label roleLabel = new Label(user.getRoles().contains("ROLE_BLOCKED") ? "Bloqué" : user.getUserType());
        roleLabel.getStyleClass().add("role-label");

        imageCard.getChildren().addAll(profileImage, roleLabel);

        // Personal Information Card
        VBox personalCard = new VBox(10);
        personalCard.getStyleClass().add("info-card");

        Label personalHeader = new Label("Informations Personnelles");
        personalHeader.getStyleClass().add("section-header");

        GridPane personalGrid = new GridPane();
        personalGrid.setHgap(15);
        personalGrid.setVgap(10);
        personalGrid.getStyleClass().add("details-grid");

        int row = 0;
        personalGrid.add(new Label("Prénom:"), 0, row);
        personalGrid.add(new Label(user.getPrenom()), 1, row++);
        personalGrid.add(new Label("Nom:"), 0, row);
        personalGrid.add(new Label(user.getNom()), 1, row++);
        personalGrid.add(new Label("Email:"), 0, row);
        personalGrid.add(new Label(user.getEmail()), 1, row++);
        personalGrid.add(new Label("Adresse:"), 0, row);
        personalGrid.add(new Label(user.getAdresse() != null ? user.getAdresse() : "N/A"), 1, row++);
        personalGrid.add(new Label("Sexe:"), 0, row);
        personalGrid.add(new Label(user.getSexe() != null ? user.getSexe() : "N/A"), 1, row++);
        personalGrid.add(new Label("Âge:"), 0, row);
        personalGrid.add(new Label(String.valueOf(user.getAge())), 1, row++);

        personalCard.getChildren().addAll(personalHeader, personalGrid);

        // Account Information Card
        VBox accountCard = new VBox(10);
        accountCard.getStyleClass().add("info-card");

        Label accountHeader = new Label("Informations du Compte");
        accountHeader.getStyleClass().add("section-header");

        GridPane accountGrid = new GridPane();
        accountGrid.setHgap(15);
        accountGrid.setVgap(10);
        accountGrid.getStyleClass().add("details-grid");

        row = 0;
        accountGrid.add(new Label("Type:"), 0, row);
        accountGrid.add(new Label(user.getUserType()), 1, row++);
        accountGrid.add(new Label("Rôles:"), 0, row);
        accountGrid.add(new Label(String.join(", ", user.getRoles())), 1, row++);
        accountGrid.add(new Label("Verrouillé jusqu'à:"), 0, row);
        accountGrid.add(new Label(user.getLockUntil() != null ?
                user.getLockUntil().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "N/A"), 1, row++);

        accountCard.getChildren().addAll(accountHeader, accountGrid);

        // Doctor-Specific Information Card (if applicable)
        VBox doctorCard = null;
        if (user.getUserType().equals("MEDECIN")) {
            doctorCard = new VBox(10);
            doctorCard.getStyleClass().add("info-card");

            Label doctorHeader = new Label("Informations du Médecin");
            doctorHeader.getStyleClass().add("section-header");

            GridPane doctorGrid = new GridPane();
            doctorGrid.setHgap(15);
            doctorGrid.setVgap(10);
            doctorGrid.getStyleClass().add("details-grid");

            row = 0;
            doctorGrid.add(new Label("Spécialité:"), 0, row);
            doctorGrid.add(new Label(user.getSpecialite() != null ? user.getSpecialite() : "N/A"), 1, row++);
            doctorGrid.add(new Label("Certificat:"), 0, row);
            Hyperlink certLink = new Hyperlink(user.getCertificat() != null && !user.getCertificat().isEmpty() ?
                    new File(user.getCertificat()).getName() : "N/A");
            certLink.getStyleClass().add("cert-link");
            if (user.getCertificat() != null && !user.getCertificat().isEmpty()) {
                certLink.setOnAction(e -> {
                    try {
                        File certFile = new File(user.getCertificat());
                        if (certFile.exists()) {
                            Desktop.getDesktop().open(certFile);
                        } else {
                            showAlert("Erreur", "Le fichier certificat n'existe pas.", Alert.AlertType.ERROR);
                        }
                    } catch (IOException ex) {
                        showAlert("Erreur", "Impossible d'ouvrir le certificat: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                });
            } else {
                certLink.setDisable(true);
            }
            doctorGrid.add(certLink, 1, row++);

            doctorCard.getChildren().addAll(doctorHeader, doctorGrid);
        }

        // Assemble content
        content.getChildren().add(imageCard);
        content.getChildren().addAll(personalCard, accountCard);
        if (doctorCard != null) {
            content.getChildren().add(doctorCard);
        }

        scrollPane.setContent(content);
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/admin.css").toExternalForm());

        // Set fixed dialog size
        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().setPrefHeight(600);

        // Add close button action
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Button closeDialogButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeDialogButton.setVisible(false); // Hide default close button
        closeDialogButton.setOnAction(e -> {
            LOGGER.info("Default close button action triggered for user: " + user.getEmail());
            dialog.close();
        });

        // Center dialog on screen
        dialog.setOnShowing(e -> {
            double centerX = stage.getX() + stage.getWidth() / 2 - dialog.getWidth() / 2;
            double centerY = stage.getY() + stage.getHeight() / 2 - dialog.getHeight() / 2;
            dialog.setX(centerX);
            dialog.setY(centerY);
        });

        dialog.showAndWait();
    }


    private void toggleUserBlockStatus(User user, Button button) {
        String originalTab = null;
        try {
            LOGGER.info("Toggling block status for user: " + user.getEmail() + ", Current roles: " + user.getRoles());
            List<String> newRoles = new ArrayList<>();
            originalTab = tabPane.getSelectionModel().getSelectedItem().getText();
            String userType = user.getUserType();

            if (user.getRoles().contains("ROLE_BLOCKED")) {
                // Unblock
                if (user.getRoles().contains("ORIGINAL_ROLE_MEDECIN")) {
                    newRoles.add("ROLE_MEDECIN");
                } else if (user.getRoles().contains("ORIGINAL_ROLE_PATIENT")) {
                    newRoles.add("ROLE_PATIENT");
                } else {
                    String restoredRole = userType.equals("PATIENT") ? "ROLE_PATIENT" : "ROLE_MEDECIN";
                    newRoles.add(restoredRole);
                }
                user.setRoles(newRoles);
                user.setLockUntil(null);
                button.setText("Bloquer");
                LOGGER.info("Unblocked user: " + user.getEmail() + ", New roles: " + newRoles);
            } else {
                // Block
                newRoles.add("ROLE_BLOCKED");
                String originalRole = userType.equals("PATIENT") ? "ORIGINAL_ROLE_PATIENT" : "ORIGINAL_ROLE_MEDECIN";
                newRoles.add(originalRole);
                user.setRoles(newRoles);
                user.setLockUntil(LocalDateTime.now().plusHours(24));
                button.setText("Débloquer");
                LOGGER.info("Blocked user: " + user.getEmail() + ", New roles: " + newRoles);
            }

            LOGGER.info("Updating user: " + user.getEmail() + " with roles: " + user.getRoles());
            authService.updateUser(user);
            LOGGER.info("User updated successfully: " + user.getEmail());

            // Refresh user list
            allUsers = authService.getAllUsers();
            LOGGER.info("Refreshed user list, total users: " + allUsers.size());
            allUsers.forEach(u ->
                    LOGGER.info("User in list: " + u.getEmail() + ", Roles: " + u.getRoles() + ", Type: " + u.getUserType()));

            // Display the appropriate tab
            if (user.getRoles().contains("ROLE_BLOCKED")) {
                tabPane.getSelectionModel().select(blockedTab);
                displayUsers("BLOCKED");
            } else {
                String targetTab = userType.equals("PATIENT") ? "PATIENT" : "MEDECIN";
                tabPane.getSelectionModel().select(targetTab.equals("PATIENT") ? patientsTab : medecinsTab);
                displayUsers(targetTab);
            }

            showAlert("Succès", "Statut de l'utilisateur mis à jour.", Alert.AlertType.INFORMATION);
        } catch (AuthException e) {
            LOGGER.severe("Error updating user status: " + user.getEmail() + ", Error: " + e.getMessage());
            showAlert("Erreur", "Erreur lors de la mise à jour du statut: " + e.getMessage(), Alert.AlertType.ERROR);
            // Revert UI changes
            button.setText(user.getRoles().contains("ROLE_BLOCKED") ? "Débloquer" : "Bloquer");
            // Use originalTab to revert to the correct tab
            String revertUserType = originalTab.equals("Patients") ? "PATIENT" :
                    originalTab.equals("Médecins") ? "MEDECIN" : "BLOCKED";
            displayUsers(revertUserType);
        }
    }

    @FXML
    private void debugBlockedUsers() {
        List<User> blockedUsers = allUsers.stream()
                .filter(user -> user.getRoles().contains("ROLE_BLOCKED"))
                .toList();
        LOGGER.info("Blocked users: " + blockedUsers.size());
        blockedUsers.forEach(user -> LOGGER.info("Blocked user: " + user.getEmail() + ", Roles: " + user.getRoles() + ", Type: " + user.getUserType()));
        showAlert("Debug", "Nombre d'utilisateurs bloqués : " + blockedUsers.size(), Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleLogout() {
        try {
            currentUser = null;
            sceneManager.logout();
            LOGGER.info("Logout successful, returned to login scene");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during logout: " + e.getMessage(), e);
            showAlert("Erreur", "Erreur lors de la déconnexion: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void goReponse(ActionEvent event) {
        try {
            // Load the FXML file for GestionReponse screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionReponse.fxml"));
            AnchorPane root = loader.load();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Get the current stage

            // Create a new scene with the loaded FXML
            Scene newScene = new Scene(root);

            // Set the scene for the current stage
            currentStage.setScene(newScene);
            currentStage.show();  // Show the new scene

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}