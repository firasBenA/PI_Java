package tn.esprit.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.models.User;
import tn.esprit.repository.UserRepositoryImpl;
import tn.esprit.services.AuthException;
import tn.esprit.services.AuthService;
import tn.esprit.utils.SceneManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AdminDashboard {
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nomColumn;
    @FXML private TableColumn<User, String> prenomColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private TableColumn<User, Void> actionsColumn;

    @FXML private Tab patientsTab;
    @FXML private Tab medecinsTab;

    private final AuthService authService = new AuthService(new UserRepositoryImpl());
    private ObservableList<User> allUsers;
    private User currentUser;
    private SceneManager sceneManager;

    @FXML
    public void initialize() {
        configureTableColumns();
        loadUsers();
        setupTabListeners();
    }

    private void configureTableColumns() {
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        nomColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNom()));
        prenomColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPrenom()));
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));

        roleColumn.setCellValueFactory(cellData -> {
            String roles = String.join(", ", cellData.getValue().getRoles())
                    .replace("ROLE_", "");
            return new SimpleStringProperty(roles);
        });

        statusColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            boolean isBlocked = user.getLockUntil() != null &&
                    user.getLockUntil().isAfter(LocalDateTime.now());
            return new SimpleStringProperty(isBlocked ? "Bloqué" : "Actif");
        });

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button toggleBtn = new Button();
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox pane = new HBox(5, toggleBtn, editBtn, deleteBtn);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    if (user.getLockUntil() != null &&
                            user.getLockUntil().isAfter(LocalDateTime.now())) {
                        toggleBtn.setText("Débloquer");
                    } else {
                        toggleBtn.setText("Bloquer");
                    }
                    toggleBtn.setOnAction(e -> {
                        try {
                            toggleUserStatus(user);
                        } catch (AuthException ex) {
                            showAlert("Erreur", ex.getMessage(), Alert.AlertType.ERROR);
                        }
                    });
                    editBtn.setOnAction(e -> editUser(user));
                    deleteBtn.setOnAction(e -> deleteUser(user));
                    setGraphic(pane);
                }
            }
        });
    }

    private void loadUsers() {
        try {
            allUsers = FXCollections.observableArrayList(authService.getAllUsers());
            filterUsersByRole("ROLE_PATIENT"); // Par défaut afficher les patients
        } catch (AuthException e) {
            showAlert("Erreur", "Impossible de charger les utilisateurs : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void setupTabListeners() {
        patientsTab.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) filterUsersByRole("ROLE_PATIENT");
        });

        medecinsTab.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) filterUsersByRole("ROLE_MEDECIN");
        });
    }

    private void filterUsersByRole(String role) {
        if (allUsers != null) {
            List<User> filtered = allUsers.stream()
                    .filter(user -> user.getRoles().contains(role))
                    .toList();
            usersTable.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    @FXML
    private void handleAddUser() {
        showUserForm(null);
    }

    private void showUserForm(User user) {
        // TODO: Implement user form for adding/editing users
        showAlert("Information", "Formulaire d'ajout/modification non implémenté.", Alert.AlertType.INFORMATION);
    }

    private void editUser(User user) {
        showUserForm(user);
    }

    private void toggleUserStatus(User user) throws AuthException {
        authService.toggleUserStatus(user.getId());
        loadUsers();
        showAlert("Succès", "Statut utilisateur mis à jour", Alert.AlertType.INFORMATION);
    }

    private void deleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'utilisateur");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer " + user.getNom() + " " + user.getPrenom() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                authService.deleteUser(user.getId());
                loadUsers();
                showAlert("Succès", "Utilisateur supprimé avec succès", Alert.AlertType.INFORMATION);
            } catch (AuthException e) {
                showAlert("Erreur", "Échec de la suppression : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        if (sceneManager != null) {
            sceneManager.showLoginScene();
        } else {
            try {
                Stage currentStage = (Stage) usersTable.getScene().getWindow();
                currentStage.close();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/views/Login.fxml"));
                Parent root = loader.load();
                Stage loginStage = new Stage();
                loginStage.setScene(new Scene(root));
                loginStage.setTitle("Connexion");
                loginStage.show();
            } catch (Exception e) {
                showAlert("Erreur", "Échec de la déconnexion : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setAuthService(AuthService authService) {
        // Note: authService is instantiated directly in this class
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }
}