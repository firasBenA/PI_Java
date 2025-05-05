package tn.esprit.test;

import javafx.application.Application;
import javafx.stage.Stage;
import tn.esprit.models.User;
import tn.esprit.repository.UserRepositoryImpl;
import tn.esprit.services.AuthService;
import tn.esprit.services.SessionManager;
import tn.esprit.utils.SceneManager;

public class MainFX extends Application {

    private SceneManager sceneManager;
    private AuthService authService;

    @Override
    public void start(Stage primaryStage) {
        // Initialize dependencies
        UserRepositoryImpl userRepository = new UserRepositoryImpl();
        authService = AuthService.getInstance(userRepository);
        sceneManager = new SceneManager(primaryStage, authService);

        // Set the stage title
        primaryStage.setTitle("Medical Application");

        // Check for existing session
        try {
            String sessionEmail = SessionManager.loadSession();
            System.out.println("Session email: " + sessionEmail);
            if (sessionEmail != null) {
                User currentUser = authService.getCurrentUser();
                if (currentUser != null) {
                    System.out.println("Existing session found for user: " + currentUser.getEmail() + ", type: " + currentUser.getUserType());
                    sceneManager.showHomePage(currentUser);
                    return;
                } else {
                    System.out.println("No valid user found for session email: " + sessionEmail + ", clearing session");
                    if (!SessionManager.clearSession()) {
                        System.err.println("Failed to clear invalid session");
                    }
                }
            } else {
                System.out.println("No active session found");
            }
            // No valid session, show login scene
            sceneManager.showLoginScene();
        } catch (Exception e) {
            System.err.println("Error checking session: " + e.getMessage());
            e.printStackTrace();
            SessionManager.clearSession();
            sceneManager.showLoginScene();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
