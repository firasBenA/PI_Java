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

import static javafx.application.Application.launch;

public class TestMain  {

    public static void main(String[] args) {
        launch(args);
    }
}