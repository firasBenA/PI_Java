package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import tn.esprit.services.AuthService;
import tn.esprit.utils.SceneManager;
import java.io.IOException;

public class MainController {

    @FXML
    private StackPane contentArea;

    private AuthService authService;
    private SceneManager sceneManager;

    // Setters for dependencies
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    public void initialize() {
        // Load login page by default
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

    public void goGestionEvenements() {
        loadUI("GestionEvenements");
    }

    public void goGestionArticles() {
        loadUI("GestionArticles");
    }

    public void goGestionPatient() {
        loadUI("PatientDashboard");
    }

    public void handleLogout() {
        System.out.println("Déconnexion...");
        loadUI("login");
    }

    private void loadUI(String fxml) {
        try {
            System.out.println("Loading FXML: /" + fxml + ".fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml + ".fxml"));
            Node node = loader.load();

            // Initialize LoginController for login.fxml
            if ("login".equals(fxml)) {
                LoginController controller = loader.getController();
                if (controller == null) {
                    System.err.println("LoginController is null. Check login.fxml controller binding.");
                    return;
                }
                if (authService == null || sceneManager == null) {
                    System.err.println("authService or sceneManager is null in MainController.");
                    return;
                }
                System.out.println("Setting authService and sceneManager for LoginController");
                controller.setAuthService(authService);
                controller.setSceneManager(sceneManager);
            }

            if (contentArea == null) {
                System.err.println("contentArea is null in MainController.");
                return;
            }
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            System.err.println("Error loading FXML: /" + fxml + ".fxml");
            e.printStackTrace();
        }
    }
}