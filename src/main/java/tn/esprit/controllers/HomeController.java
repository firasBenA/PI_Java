package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import tn.esprit.models.Medecin;
import tn.esprit.models.User;
import tn.esprit.services.AuthService;
import tn.esprit.utils.SceneManager;

import java.io.IOException;

public class HomeController {

    private AuthService authService;

    private SceneManager sceneManager;
    private Medecin currentUser;

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public void setCurrentUser(User user) {
        if (user instanceof Medecin) {
            this.currentUser = (Medecin) user;
        } else {
            System.out.println("Erreur Utilisateur invalide pour le tableau de bord Médecin");
        }
    }
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

            // Get the controller
            DiagnostiqueCardsController diagnostiqueCardsController = loader.getController();

            // Set the AuthService for the controller
            diagnostiqueCardsController.setAuthService(authService);
            diagnostiqueCardsController.setCurrentUser(authService.getCurrentUser());

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
