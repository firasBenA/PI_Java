package tn.esprit.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.controllers.*;
import tn.esprit.models.User;
import tn.esprit.services.AuthService;
import tn.esprit.services.SessionManager;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import java.io.IOException;

public class SceneManager {
    private final Stage primaryStage;
    private final AuthService authService;
    private LoginController currentLoginController;

    public SceneManager(Stage primaryStage, AuthService authService) {
        this.primaryStage = primaryStage;
        this.authService = authService;
        this.currentLoginController = null;
    }

    public void showMainScene() {
        try {
            cleanup();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Ajouter Réclamation");
            primaryStage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page principale : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
            showLoginScene();
        }
    }

    public void showLoginScene() {
        try {
            cleanup();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setAuthService(authService);
            controller.setSceneManager(this);
            currentLoginController = controller; // Set the current LoginController

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Connexion");
            primaryStage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page de connexion : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void showRegisterScene() {
        try {
            cleanup();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/register.fxml"));
            Parent root = loader.load();

            RegisterController controller = loader.getController();
            controller.setAuthService(authService);
            controller.setSceneManager(this);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle("Inscription");
            primaryStage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page d'inscription : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
            showLoginScene();
        }
    }

    public void showForgotPasswordScene() {
        cleanup();

    }

    public void showAdminDashboard(User user) {
        try {
            cleanup();
            if (!validateUser(user, "ROLE_ADMIN")) {
                showAlert("Erreur", "Accès non autorisé. Veuillez vous connecter en tant qu'administrateur.", Alert.AlertType.ERROR);
                logout();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminDashboard.fxml"));
            Parent root = loader.load();

            AdminDashboard controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setSceneManager(this);
            controller.setAuthService(authService);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Tableau de Bord Admin");
            primaryStage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger le tableau de bord admin : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
            logout();
        }
    }

    public void showPatientDashboard(User user) {
        try {
            cleanup();
            if (!validateUser(user, "ROLE_PATIENT")) {
                showAlert("Erreur", "Accès non autorisé. Veuillez vous connecter en tant que patient.", Alert.AlertType.ERROR);
                logout();
                return;
            }

            System.out.println("Showing Patient Dashboard for user: " + user.getNom());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setAuthService(authService);
            controller.setSceneManager(this);
            controller.initUserData();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Tableau de Bord Patient");
            primaryStage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger le tableau de bord patient : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
            logout();
        }
    }

    public void showMedecinDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainM.fxml"));
            Parent root = loader.load();

            MainMController controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setAuthService(this.authService);
            controller.setSceneManager(this);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Tableau de Bord Médecin");
            primaryStage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger le tableau de bord médecin: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    public void showMedecinProfile(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MedecinDashboard.fxml"));
            Parent root = loader.load();

            MedecinDashboardController controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setAuthService(this.authService);
            controller.setSceneManager(this);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Tableau de Bord Médecin");
            primaryStage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger le tableau de bord médecin: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }    }

    public void showHomePage(User user) {
        if (user == null || !validateSession()) {
            logout();
            return;
        }
        switch (user.getUserType()) {
            case "ADMIN":
                showAdminDashboard(user);
                break;
            case "MEDECIN":
                showMedecinDashboard(user);
                break;
            case "PATIENT":
                showPatientDashboard(user);
                break;
            default:
                showAlert("Erreur", "Type d'utilisateur non supporté : " + user.getUserType(), Alert.AlertType.ERROR);
                logout();
        }
    }

    public void logout() {
        try {
            System.out.println("Logging out: Clearing session and user");
            authService.setCurrentUser(null);
            boolean sessionCleared = SessionManager.clearSession();
            if (sessionCleared) {
                System.out.println("Session cleared successfully");
            } else {
                System.err.println("Failed to clear session");
            }
            cleanup();
            showAlert("Succès", "Déconnexion réussie.", Alert.AlertType.INFORMATION);
            showLoginScene();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la déconnexion : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
            showLoginScene();
        }
    }

    private boolean validateUser(User user, String requiredRole) {
        if (user == null) {
            System.err.println("User is null");
            return false;
        }
        if (!validateSession()) {
            System.err.println("Invalid session for user: " + user.getEmail());
            return false;
        }
        if (!user.getRoles().contains(requiredRole)) {
            System.err.println("User " + user.getEmail() + " lacks required role: " + requiredRole);
            return false;
        }
        return true;
    }

    private boolean validateSession() {
        String sessionEmail = SessionManager.loadSession();
        if (sessionEmail == null) {
            System.out.println("No active session found");
            return false;
        }
        User currentUser = authService.getCurrentUser();
        if (currentUser == null || !currentUser.getEmail().equals(sessionEmail)) {
            System.out.println("Session mismatch or invalid user");
            SessionManager.clearSession();
            return false;
        }
        return true;
    }

    public void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void cleanup() {
        if (currentLoginController != null) {
            System.out.println("Cleaning up current LoginController");
            currentLoginController.cleanup();
            currentLoginController = null;
        } else {
            System.out.println("No LoginController to clean up");
        }
    }
}