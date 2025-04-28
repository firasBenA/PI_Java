package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import tn.esprit.services.AuthService;
import tn.esprit.utils.SceneManager;

import java.io.IOException;

public class MainController {

    @FXML
    private StackPane contentArea;

    private AuthService authService;
    private SceneManager sceneManager;
    private LoginController loginController;

    public void setAuthService(AuthService authService) {
        this.authService = authService;
        if (authService != null && sceneManager != null && contentArea != null) {
            initializeUI();
        }
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        if (authService != null && sceneManager != null && contentArea != null) {
            initializeUI();
        }
    }

    @FXML
    public void initialize() {
        if (authService != null && sceneManager != null && contentArea != null) {
            initializeUI();
        }
    }

    private void initializeUI() {
        loadUI("login");
    }

    public void goHome() {
        loadUI("login");
    }

    public void goDiagnostique() {
        loadUI("GestionDiagnostique");
    }

    public void goPrescription() {
        loadUI("GestionPrescription");
    }

    public void goGestionMedecin() {
        loadUI("MedecinPrescDiag");
    }

    public void goGestionReclamation() {
        loadUI("ListeReclamation");
    }

    public void goGestionReponse() {
        loadUI("GestionReponse");
    }

    public void goGestionPatient() {
        loadUI("PatientDashboard");
    }

    public void goGestionPanier() {
        loadUI("panier"); // NEW: loads GestionPanier.fxml
    }

    public void goGestionProduit() {
        loadUI("produit"); // NEW: loads GestionProduit.fxml
    }

    public void handleLogout() {
        System.out.println("Déconnexion...");
        stop();
        try {
            Thread.sleep(5000); // Increased to 5000ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (authService == null || sceneManager == null) {
            System.err.println("Cannot load login page: authService or sceneManager is null");
            return;
        }
        Platform.runLater(() -> initializeUI());
    }

    private void loadUI(String fxml) {
        try {
            System.out.println("Loading FXML: /" + fxml + ".fxml");
            stop(); // Stop server before any UI load
            if (loginController != null && !fxml.equals("login")) {
                System.out.println("Stopping LoginController server before loading new UI");
                loginController.cleanup();
                loginController = null;
                Thread.sleep(5000); // Increased to 5000ms
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml + ".fxml"));
            Node node = loader.load();

            if ("login".equals(fxml)) {
                loginController = loader.getController();
                if (loginController == null) {
                    System.err.println("LoginController is null. Check login.fxml controller binding.");
                    return;
                }
                if (authService == null || sceneManager == null) {
                    System.err.println("authService or sceneManager is null in MainController.");
                    return;
                }
                System.out.println("Setting authService and sceneManager for LoginController");
                loginController.setAuthService(authService);
                loginController.setSceneManager(sceneManager);
            }

            if (contentArea == null) {
                System.err.println("contentArea is null in MainController.");
                return;
            }
            contentArea.getChildren().setAll(node);
        } catch (IOException | InterruptedException e) {
            System.err.println("Error loading FXML: /" + fxml + ".fxml - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        if (loginController != null) {
            System.out.println("Stopping LoginController server from MainController");
            loginController.cleanup();
            loginController = null;
        } else {
            System.out.println("No LoginController to stop");
        }
    }
}