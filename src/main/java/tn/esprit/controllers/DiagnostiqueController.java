package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.models.Diagnostique;
import tn.esprit.models.Medecin;
import tn.esprit.models.User;
import tn.esprit.services.AuthService;
import tn.esprit.services.ServiceUser;
import tn.esprit.utils.SceneManager;

import javax.swing.text.html.ImageView;
import java.io.IOException;

public class DiagnostiqueController {

    @FXML
    private Label dateDiagnostique;

    @FXML
    private Label nomDiagnostique;

    @FXML
    private Label selectedSymptomes;

    @FXML
    private ImageView userImg;

    @FXML
    private Label nom;

    @FXML
    private Label prenom;

    @FXML
    private Label zoneCorps;

    @FXML
    private Button addBtn;

    private String userRole;
    private Diagnostique diagnostiquePassedToThisCard;

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

    public void setData(Diagnostique diagnostique) {
        this.diagnostiquePassedToThisCard = diagnostique;

        if (currentUser == null) {
            System.out.println("Current user is NULL!");
            return;
        }

        System.out.println("Current user roles: " + currentUser.getRoles());

        // Check if the current user is a doctor and filter diagnostiques accordingly
        if (currentUser.getRoles().contains("ROLE_MEDECIN")) {
            // Display the button if the current user is a doctor
            addBtn.setVisible(true);
        } else {
            addBtn.setVisible(false);
        }

        // Check if the diagnostique is relevant to the current user
        if (diagnostique.getMedecinId() == currentUser.getId()) {
            // Display diagnostique details if the current user is related to this diagnostique
            selectedSymptomes.setText(diagnostique.getSelectedSymptoms());
            zoneCorps.setText(diagnostique.getZoneCorps());
            nomDiagnostique.setText(diagnostique.getNom());
            dateDiagnostique.setText(diagnostique.getDateDiagnostique().toString());

            // Fetch doctor info by medecin_id (in case of another doctor, fetch their details)
            ServiceUser serviceUser = new ServiceUser();
            User medecin = serviceUser.getUserById(diagnostique.getMedecinId());

            if (medecin != null) {
                nom.setText(medecin.getNom());
                prenom.setText(medecin.getPrenom());
            } else {
                nom.setText("Inconnu");
                prenom.setText("Inconnu");
            }
        } else {
            // If the diagnostique is not related to the current user, you can choose to hide it or show a message
            System.out.println("This diagnostique is not related to the current user.");
        }
    }


    private void showAddPrescriptionForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterPrescriptionDepuisDiag.fxml"));
            Parent root = loader.load();

            AjouterPrescriptionDepuisDiag controller = loader.getController();
            controller.setAuthService(authService);
            controller.setCurrentUser(authService.getCurrentUser());

            controller.setDiagnostique(this.diagnostiquePassedToThisCard);

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Ordonnance");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddPrescription(javafx.event.ActionEvent actionEvent) {
        System.out.println("Add Prescription button clicked");
        showAddPrescriptionForm();
    }
}
