package tn.esprit.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.controllers.LoginController;
import tn.esprit.repository.UserRepository;
import tn.esprit.repository.UserRepositoryImpl;
import tn.esprit.services.AuthService;
import tn.esprit.utils.SceneManager;

import java.io.IOException;

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

            // Show the login scene on launch
            sceneManager.showLoginScene();

            // Show the stage
            primaryStage.show();
        }

        public static void main(String[] args) {
            launch(args);
        }
    }