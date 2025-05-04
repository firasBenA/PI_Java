package tn.esprit.test;

import javafx.application.Application;
import javafx.stage.Stage;
import tn.esprit.models.User;
import tn.esprit.repository.UserRepository;
import tn.esprit.repository.UserRepositoryImpl;
import tn.esprit.services.AuthException;
import tn.esprit.services.AuthService;
import tn.esprit.utils.SceneManager;

public class MainFX extends Application {

    private SceneManager sceneManager;
    private AuthService authService;

    @Override
    public void start(Stage primaryStage) {
        // Initialize dependencies
        UserRepository userRepository = new UserRepositoryImpl();
        authService = new AuthService(userRepository);
        sceneManager = new SceneManager(primaryStage, authService);

        // Set the stage title
        primaryStage.setTitle("Medical Application");

        // Check for existing session
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                System.out.println("Existing session found for user: " + currentUser.getEmail());
                // Navigate to the appropriate dashboard based on user type
                switch (currentUser.getUserType()) {
                    case "ADMIN":
                        sceneManager.showAdminDashboard(currentUser);
                        break;
                    case "MEDECIN":
                        sceneManager.showMedecinDashboard(currentUser);
                        break;
                    case "PATIENT":
                        sceneManager.showPatientDashboard(currentUser);
                        break;
                    default:
                        System.err.println("Unknown user type: " + currentUser.getUserType());
                        sceneManager.showLoginScene();
                }
            } else {
                // No session exists, show login scene
                sceneManager.showLoginScene();
            }
        } catch (Exception e) {
            System.err.println("Error checking session: " + e.getMessage());
            sceneManager.showLoginScene();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}