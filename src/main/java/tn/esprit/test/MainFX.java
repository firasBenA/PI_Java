package tn.esprit.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/home2.fxml"));
        primaryStage.setScene(new Scene(loader.load(), 800, 600));
        primaryStage.setTitle("Faza");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}