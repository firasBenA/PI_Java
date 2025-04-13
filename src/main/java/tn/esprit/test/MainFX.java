package tn.esprit.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.repository.UserRepository;
import tn.esprit.repository.UserRepositoryImpl;
import tn.esprit.services.AuthService;
import tn.esprit.utils.MyDataBase;
import tn.esprit.utils.SceneManager;

import java.io.IOException;

public class MainFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Initialize database connection
        MyDataBase.getInstance();

        // Initialize services with database repository
        UserRepository userRepository = new UserRepositoryImpl();
        AuthService authService = new AuthService(userRepository);

        // Set up scene manager
        SceneManager sceneManager = new SceneManager(primaryStage, authService);

        // Show login scene first
        sceneManager.showLoginScene();

        primaryStage.setTitle("Medical Application");
        primaryStage.show();
    }


}
