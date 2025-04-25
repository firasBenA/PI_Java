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

    public SceneManager(Stage primaryStage, AuthService authService) {
        this.primaryStage = primaryStage;
        this.authService = authService;
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


    public void showMedecinDashboard(User user) {
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
        }
    }

    public void showPatientDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PatientDashboard.fxml"));
            Parent root = loader.load();

            PatientDashboardController controller = loader.getController();
            controller.setCurrentUser(user);
            controller.setAuthService(this.authService);
            controller.setSceneManager(this);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Tableau de Bord Patient");
            primaryStage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger le tableau de bord patient: " + e.getMessage(), Alert.AlertType.ERROR);
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