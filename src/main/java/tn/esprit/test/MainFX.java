package tn.esprit.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.controllers.MainController;
import tn.esprit.repository.UserRepositoryImpl;
import tn.esprit.services.AuthService;
import tn.esprit.utils.SceneManager;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize AuthService
        AuthService authService = new AuthService(new UserRepositoryImpl());

        // Initialize SceneManager
        SceneManager sceneManager = new SceneManager(primaryStage, authService);

        // Load Main.fxml and initialize MainController
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main.fxml"));
        Parent root = loader.load();

        // Initialize MainController with dependencies
        MainController controller = loader.getController();
        if (controller == null) {
            throw new IllegalStateException("MainController is null. Check Main.fxml controller binding.");
        }
        controller.setAuthService(authService);
        controller.setSceneManager(sceneManager);

        primaryStage.setTitle("Ajouter Réclamation");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
