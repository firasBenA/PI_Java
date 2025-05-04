package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import tn.esprit.models.User;
import tn.esprit.services.AuthService;
import tn.esprit.utils.SceneManager;

import java.io.IOException;

public class MainController {

    @FXML
    private StackPane contentArea;

    private AuthService authService;
    private SceneManager sceneManager;
    private LoginController loginController;
    private User currentUser;


    public void setCurrentUser(User user) {
        System.out.println("Setting current user: " + (user != null ? user.getNom() : "null"));
        this.currentUser = user;
    }


    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }
    @FXML
    public void initialize() {
        if (authService != null && sceneManager != null && contentArea != null) {
            System.out.println("Initializing UI...");
            if (currentUser != null) {
                System.out.println("Current user found: " + currentUser.getNom());
            } else {
                System.out.println("Current user is null during initialization.");
            }
            initializeUI();
            initUserData();
        }
    }



    public void initUserData() {
        if (currentUser != null) {
            System.out.println("Utilisateur connecté : " + currentUser.getNom()); // Print the user's name
        } else {
            System.out.println("Current user is NULL!");
        }
    }
    private void initializeUI() {

    }

    public void goHome() {
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

    public void goStatistiques() {
        loadUI("Statistiques");
    }

    public void goPatientDiagnostique() {
        loadUI("PatientDiagnostique");
    }
    public void goGestionEvenements() {
        loadUI("Statistiques");
    }
    public void goGestionArticles() {
        loadUI("Statistiques");
    }


    public void handleLogout() {
        System.out.println("Déconnexion...");
        stop();
        try {
            Thread.sleep(5000);
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

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml + ".fxml"));
            Node node = loader.load();

            Object controller = loader.getController();
            if (controller != null) {
                try {
                    controller.getClass().getMethod("setCurrentUser", User.class).invoke(controller, currentUser);
                    System.out.println("Passed current user to controller " + controller.getClass().getSimpleName() + ": " + currentUser.getNom());

                    // Check if the controller has a refreshData() method and call it
                    try {
                        controller.getClass().getMethod("refreshData").invoke(controller);
                        System.out.println("Called refreshData on controller " + controller.getClass().getSimpleName());
                    } catch (NoSuchMethodException e) {
                        System.out.println("Controller " + controller.getClass().getSimpleName() + " does not have refreshData(). Skipping...");
                    }

                } catch (NoSuchMethodException e) {
                    System.out.println("Controller " + controller.getClass().getSimpleName() + " does not have setCurrentUser(User user). Skipping...");
                }
            }

            if (contentArea == null) {
                System.err.println("contentArea is null in MainController.");
                return;
            }
            contentArea.getChildren().setAll(node);
        } catch (IOException | ReflectiveOperationException e) {
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