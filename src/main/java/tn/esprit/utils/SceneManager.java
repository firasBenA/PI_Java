package tn.esprit.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.controllers.*;
import tn.esprit.models.User;
import tn.esprit.repository.UserRepositoryImpl;
import tn.esprit.services.AuthService;
import javafx.scene.control.Alert;

import java.io.IOException;

public class SceneManager {
    private final Stage primaryStage;
    private final AuthService authService;
    private User currentUser;


    public SceneManager(Stage primaryStage, AuthService authService) {
        this.primaryStage = primaryStage;
        this.authService = authService;
    }

    // Nouvelle méthode pour afficher la page principale (Main.fxml)
    public void showMainScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Ajouter Réclamation");
            primaryStage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger la page principale.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void showLoginScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setAuthService(authService);
            controller.setSceneManager(this);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Connexion");
            primaryStage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger la page de connexion.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    public void showRegisterScene() {
        try {
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
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger la page d'inscription.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void showForgotPasswordScene() {
        // Implement forgot password scene
    }

    public void showAdminDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminDashboard.fxml"));
            Parent root = loader.load();
            AdminDashboard controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setSceneManager(this);
            controller.setAuthService(new AuthService(new UserRepositoryImpl()));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Tableau de Bord Admin");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showPatientDashboard(User user) {
        try {
            if (user == null) {
                System.err.println("User is null when attempting to show Patient Dashboard");
            } else {
                System.out.println("Showing Patient Dashboard for user: " + user.getNom());
            }

            System.out.println("Current user in showPatientDashboard: " + user.getNom());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setAuthService(this.authService);
            controller.setSceneManager(this);

            controller.initUserData();

            this.currentUser = user;
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Tableau de Bord Patient");
            primaryStage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger le tableau de bord patient: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
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
            // Load the FXML for MainM.fxml
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
            // If an error occurs while loading the FXML, display an alert
            showAlert("Erreur", "Impossible de charger le tableau de bord médecin: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void showHomePage(User user) {
        // Implement home page
        showAlert("Connexion réussie", "Bienvenue", Alert.AlertType.INFORMATION);
    }

    public void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}