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

    public void goGestionEvenements() {loadUI("GestionEvenements");}

    public void goGererEvenements() {
        loadUI("Gerer");
    }


    public void goGestionArticles() {
        loadUI("GestionArticles");
    }

    public void goGestionPatient() {
        loadUI("PatientDashboard");
    }
    public void goGestionRdv() {
        loadUI("listrdv");
    }
    public void goGestionStatDoc() {
        loadUI("DoctorStats");
    }



    public void goStatistiques() {
        loadUI("Statistiques");
    }

    public void goPatientDiagnostique() {
        loadUI("PatientDiagnostique");
    }




    private void loadUI(String fxml) {
        try {
            System.out.println("Loading FXML: /" + fxml + ".fxml");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml + ".fxml"));
            Node node = loader.load();

            Object controller = loader.getController();
            if (controller != null) {
                // Pass currentUser
                try {
                    controller.getClass().getMethod("setCurrentUser", User.class).invoke(controller, currentUser);
                    System.out.println("Passed current user to controller " + controller.getClass().getSimpleName() + ": " + currentUser.getNom());
                } catch (NoSuchMethodException ignored) {}

                // Inject authService if method exists
                try {
                    controller.getClass().getMethod("setAuthService", AuthService.class).invoke(controller, authService);
                    System.out.println("Injected AuthService into " + controller.getClass().getSimpleName());
                } catch (NoSuchMethodException ignored) {}

                // Inject sceneManager if method exists
                try {
                    controller.getClass().getMethod("setSceneManager", SceneManager.class).invoke(controller, sceneManager);
                    System.out.println("Injected SceneManager into " + controller.getClass().getSimpleName());
                } catch (NoSuchMethodException ignored) {}

                // Optional: call refreshData if it exists
                try {
                    controller.getClass().getMethod("refreshData").invoke(controller);
                } catch (NoSuchMethodException ignored) {}
            }

            contentArea.getChildren().setAll(node);
        } catch (IOException | ReflectiveOperationException e) {
            System.err.println("Error loading FXML: /" + fxml + ".fxml - " + e.getMessage());
            e.printStackTrace();
        }
    }



    public void handleLogout() {
        System.out.println("Déconnexion...");
        sceneManager.logout();
    }
}