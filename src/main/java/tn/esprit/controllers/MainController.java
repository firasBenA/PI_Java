package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
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
        loadUI("GestionDiagnostique"); // Assuming you have this FXML already
    }
    public void goPrescription() {
        loadUI("GestionPrescription"); // Assuming you have this FXML already
    }

    public void goGestionMedecin() {
        loadUI("MedecinPrescDiag"); // Assuming you have this FXML already
    }

    public void goGestionPatient() {
        loadUI("PatientDashboard"); // Assuming you have this FXML already
    }

    public void goGestionReclamation() {
        loadUI("MedecinPrescDiag");
    }
    public void goGestionReponse() {
        loadUI("MedecinPrescDiag");
    }
    public void handleLogout() {
        System.out.println("Déconnexion...");
    }


    private void loadUI(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml + ".fxml"));
            Node node = loader.load();
            if (contentArea != null) {
                contentArea.getChildren().setAll(node);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
