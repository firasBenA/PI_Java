package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import tn.esprit.models.Statistiques;

import java.io.IOException;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        // Load home page by default
        loadUI("Home");
    }

    public void goHome() {
        loadUI("Home");
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

    public void goGestionPatient() {
        loadUI("PatientDashboard");
    }

    public void goStatistiques() {
        loadUI("Statistiques");
    }

    public void handleLogout() {
        System.out.println("Déconnexion...");
    }

    private void loadUI(String fxml) {
        try {
            //
            String fxmlPath = "/" + fxml + ".fxml";
            System.out.println("Loading FXML from path: " + fxmlPath);
            java.net.URL fxmlLocation = getClass().getResource(fxmlPath);
            if (fxmlLocation == null) {
                throw new IOException("Cannot find FXML file at " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Node node = loader.load();
            if (contentArea != null) {
                contentArea.getChildren().setAll(node);
            } else {
                System.out.println("Error: contentArea is null. Cannot load UI.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load FXML: " + fxml + ".fxml - " + e.getMessage());
        }
    }

}