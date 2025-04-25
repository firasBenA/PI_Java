package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class HomeController {

    private StackPane findContentArea(Node source) {
        Node current = source;
        while (current.getParent() != null) {
            current = current.getParent();
            if (current instanceof StackPane && "contentArea".equals(current.getId())) {
                return (StackPane) current;
            }
        }
        return null;
    }

    public void handleStartDiagnostique(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionDiagnostique.fxml"));
            Parent diagnostiquePage = loader.load();

            StackPane contentArea = findContentArea((Node) event.getSource());
            if (contentArea != null) {
                contentArea.getChildren().setAll(diagnostiquePage);
            } else {
                System.err.println("contentArea not found.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
