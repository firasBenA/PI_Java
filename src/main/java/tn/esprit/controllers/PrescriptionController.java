package tn.esprit.controllers;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import tn.esprit.models.Diagnostique;
import tn.esprit.models.Prescription;
import tn.esprit.models.User;
import tn.esprit.services.AuthService;
import tn.esprit.services.ServicePrescription;
import tn.esprit.utils.MyDataBase;
import tn.esprit.utils.SceneManager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PrescriptionController {

    private Connection cnx;
    @FXML
    private Button prevButton, nextButton;

    @FXML
    private HBox prescriptionCardContainer;

    @FXML
    private Button prevPrescriptionButton, nextPrescriptionButton;

    private List<Diagnostique> diagnostiques;
    private List<Prescription> prescriptions;
    private int currentDiagnostiquePage = 0;
    private int currentPrescriptionPage = 0;

    private final int ITEMS_PER_PAGE = 3;

    @FXML
    private Label contenue;

    @FXML
    private Label datePrescription;

    @FXML
    private Label nom;

    @FXML
    private Label numTel;

    @FXML
    private Label prenom;

    @FXML
    private Label titre;

    @FXML
    private Button deleteBtn;

    @FXML
    private Button modifyBtn;

    @FXML
    private Button downloadBtn;

    private Prescription currentPrescription;


    private Node cardNode;
    private Pane parentContainer;

    private String userRole;

    private AuthService authService;
    private SceneManager sceneManager;
    private User currentUser;


    @FXML
    private HBox cardContainer;




//    public void setData(Prescription prescription, javafx.scene.Node cardNode, javafx.scene.layout.Pane parentContainer) {
//        this.currentPrescription = prescription;
//        this.cardNode = cardNode;
//        this.parentContainer = parentContainer;
//
//        ServiceUser patientService = new ServiceUser();
//        User patient = patientService.getUserById(prescription.getPatient_id());
//
//        titre.setText(prescription.getTitre());
//        contenue.setText(prescription.getContenue());
//        numTel.setText(patient.getTelephone());
//        datePrescription.setText(prescription.getDate_prescription().toString());
//        nom.setText(patient.getNom());
//        prenom.setText(patient.getPrenom());
//    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public void setCurrentUser(User user) {
        if (user instanceof User) {
            this.currentUser = (User) user;
        } else {
            System.out.println("Erreur Utilisateur invalide pour le tableau de bord Médecin");
        }
    }
    public void setData(Prescription prescription) {


        if (currentUser == null) {
            System.out.println("Current user is NULL!");
            return;
        }

        diagnostiques = loadDiagnostiquesByPatientId(currentUser.getId());
        prescriptions = loadPrescriptionByPatientId(currentUser.getId());
        updateDiagnostiqueCards();
        updatePrescriptionCards();




        System.out.println("Current user roles: " + currentUser.getRoles() + currentUser.getNom());

        if (currentUser.getRoles().contains("ROLE_MEDECIN")) {
            deleteBtn.setVisible(true);
            modifyBtn.setVisible(true);
            downloadBtn.setVisible(false);
        } else if(currentUser.getRoles().contains("ROLE_USER")) {
            downloadBtn.setVisible(true);
            deleteBtn.setVisible(false);
            modifyBtn.setVisible(false);
        }


        this.currentPrescription = prescription;

        titre.setText(prescription.getTitre());
        contenue.setText(prescription.getContenue());
        numTel.setText(currentUser.getTelephone());
        datePrescription.setText(prescription.getDate_prescription().toString());
        nom.setText(currentUser.getNom());
        prenom.setText(currentUser.getPrenom());
    }


    public void refreshData() {
        if (currentUser == null) {
            System.err.println("Error: currentUser is null!");
            return;
        }

        diagnostiques = loadDiagnostiquesByPatientId(currentUser.getId());
        prescriptions = loadPrescriptionByPatientId(currentUser.getId());
        updateDiagnostiqueCards();
        updatePrescriptionCards();
    }


    @FXML
    private void onDeleteClicked() {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation de suppression");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette prescription ?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            ServicePrescription service = new ServicePrescription();
            boolean deleteResult = service.deletePrescriptionById(currentPrescription.getId());

            if (deleteResult) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Suppression réussie");
                successAlert.setHeaderText(null);
                successAlert.setContentText("La prescription a été supprimée avec succès !");
                successAlert.showAndWait();

                if (parentContainer != null && cardNode != null) {
                    parentContainer.getChildren().remove(cardNode);
                }
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur de suppression");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Une erreur est survenue lors de la suppression de la prescription.");
                errorAlert.showAndWait();
            }
        } else {
            // L'utilisateur a annulé
            System.out.println("Suppression annulée par l'utilisateur.");
        }
    }



    @FXML
    private void onModifyClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierPrescription.fxml"));
            Parent root = loader.load();

            ModifierPrescriptionController controller = loader.getController();
            controller.setPrescription(currentPrescription);

            Stage stage = new Stage();
            stage.setTitle("Modifier Prescription");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDownloadPrescription() {
        try {
            String userHome = System.getProperty("user.home");
            String downloadsPath = userHome + File.separator + "Downloads";

            String fileName = "Prescription_" + System.currentTimeMillis() + ".pdf";

            String dest = downloadsPath + File.separator + fileName;

            PdfWriter writer = new PdfWriter(dest);

            PdfDocument pdf = new PdfDocument(writer);

            Document document = new Document(pdf);
            document.add(new Paragraph("Prescription Médicale")
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(20));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("ORDONNANCE MÉDICALE").setTextAlignment(TextAlignment.CENTER).setBold());

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Dr. Mohamed"));
            document.add(new Paragraph("Contact: 25943666"));

            document.add(new Paragraph("Titre: Teste"));
            document.add(new Paragraph("Contenu: Teste"));
            document.add(new Paragraph("Date: 23/02/2025"));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Signature du médecin: ___________________"));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Cette ordonnance est valable uniquement pour une durée limitée."));

            document.close();

            System.out.println("Prescription PDF created successfully: " + dest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private List<Diagnostique> loadDiagnostiquesByPatientId(int patientId) {
        List<Diagnostique> diagnostiques = new ArrayList<>();

        try {
            cnx = MyDataBase.getInstance().getCnx();
            String sql = "SELECT id, date_diagnostique, nom, zone_corps, selected_symptoms, patient_id FROM diagnostique WHERE patient_id = ?";
            PreparedStatement statement = cnx.prepareStatement(sql);
            statement.setInt(1, patientId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Diagnostique diagnostique = new Diagnostique(
                        resultSet.getInt("id"),
                        resultSet.getDate("date_diagnostique"),
                        resultSet.getString("nom"),
                        resultSet.getString("zone_corps"),
                        resultSet.getString("selected_symptoms"),
                        resultSet.getInt("patient_id")
                );
                diagnostiques.add(diagnostique);
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return diagnostiques;
    }

    private List<Prescription> loadPrescriptionByPatientId(int patientId) {
        List<Prescription> prescriptions = new ArrayList<>();
        Connection cnx = null;

        try {
            cnx = MyDataBase.getInstance().getCnx();
            String sql = "SELECT id,dossier_medical_id,diagnostique_id,medecin_id,patient_id,titre,contenue,date_prescription FROM prescription WHERE patient_id = ?";
            PreparedStatement statement = cnx.prepareStatement(sql);
            statement.setInt(1, patientId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Prescription prescription = new Prescription(
                        resultSet.getInt("id"),
                        resultSet.getInt("dossier_medical_id"),
                        resultSet.getInt("diagnostique_id"),
                        resultSet.getInt("medecin_id"),
                        resultSet.getInt("patient_id"),
                        resultSet.getString("titre"),
                        resultSet.getString("contenue"),
                        resultSet.getDate("date_prescription")
                );
                prescriptions.add(prescription);
            }

            resultSet.close();
            statement.close();
            cnx.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return prescriptions;
    }


    private void updateDiagnostiqueCards() {
        cardContainer.getChildren().clear();

        int start = currentDiagnostiquePage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, diagnostiques.size());

        List<Diagnostique> pageItems = diagnostiques.subList(start, end);

        for (Diagnostique diag : pageItems) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Diagnostique.fxml"));
                Node card = loader.load();
                DiagnostiqueController cardController = loader.getController();
                cardController.setData(diag);
                cardContainer.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        prevButton.setDisable(currentDiagnostiquePage == 0);
        nextButton.setDisable(end >= diagnostiques.size());
    }

    private void updatePrescriptionCards() {
        prescriptionCardContainer.getChildren().clear();

        int start = currentPrescriptionPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, prescriptions.size());

        List<Prescription> pageItems = prescriptions.subList(start, end);

        for (Prescription prescription : pageItems) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Prescription.fxml"));
                Node card = loader.load();
                PrescriptionController cardController = loader.getController();
                cardController.setData(prescription);
                prescriptionCardContainer.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        prevPrescriptionButton.setDisable(currentPrescriptionPage == 0);
        nextPrescriptionButton.setDisable(end >= prescriptions.size());
    }

    @FXML
    private void handlePrevDiagnostiquePage() {
        if (currentDiagnostiquePage > 0) {
            currentDiagnostiquePage--;
            updateDiagnostiqueCards();
        }
    }

    @FXML
    private void handleNextDiagnostiquePage() {
        if ((currentDiagnostiquePage + 1) * ITEMS_PER_PAGE < diagnostiques.size()) {
            currentDiagnostiquePage++;
            updateDiagnostiqueCards();
        }
    }

    @FXML
    private void handlePrevPrescriptionPage() {
        if (currentPrescriptionPage > 0) {
            currentPrescriptionPage--;
            updatePrescriptionCards();
        }
    }

    @FXML
    private void handleNextPrescriptionPage() {
        if ((currentPrescriptionPage + 1) * ITEMS_PER_PAGE < prescriptions.size()) {
            currentPrescriptionPage++;
            updatePrescriptionCards();
        }
    }


}