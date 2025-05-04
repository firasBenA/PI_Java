package tn.esprit.controllers;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import tn.esprit.models.Diagnostique;
import tn.esprit.models.Medecin;
import tn.esprit.models.Prescription;
import tn.esprit.models.User;
import tn.esprit.services.AuthService;
import tn.esprit.utils.MyDataBase;
import tn.esprit.utils.SceneManager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PatientDiagnostiquePrescription {

    private Connection cnx;
    private AuthService authService;

    @FXML
    private HBox cardContainer;

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
    private Button downloadBtn;
    private SceneManager sceneManager;
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public void initialize() {
        if (currentUser == null) {
            System.out.println("CurrentUser is not yet set, skipping initialization...");
            return;
        }
        diagnostiques = loadDiagnostiquesByPatientId(currentUser.getId());
        prescriptions = loadPrescriptionByPatientId(currentUser.getId());
        updateDiagnostiqueCards();
        updatePrescriptionCards();

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





    // Load Diagnostiques
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

    // Load Prescriptions
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

    // Update Diagnostique Cards
    private void updateDiagnostiqueCards() {
        cardContainer.getChildren().clear();

        int start = currentDiagnostiquePage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, diagnostiques.size());

        for (int i = start; i < end; i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Diagnostique.fxml"));
                Node card = loader.load();
                DiagnostiqueController cardController = loader.getController();
                cardController.setData(diagnostiques.get(i));
                cardContainer.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        prevButton.setDisable(currentDiagnostiquePage == 0);
        nextButton.setDisable(end >= diagnostiques.size());
    }

    // Update Prescription Cards
    private void updatePrescriptionCards() {
        prescriptionCardContainer.getChildren().clear();

        int start = currentPrescriptionPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, prescriptions.size());

        for (int i = start; i < end; i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Prescription.fxml"));
                Node card = loader.load();
                PrescriptionController cardController = loader.getController();
//                cardController.setAuthService(authService);
//                cardController.setCurrentUser(authService.getCurrentUser());
                cardController.setData(prescriptions.get(i));
                prescriptionCardContainer.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        prevPrescriptionButton.setDisable(currentPrescriptionPage == 0);
        nextPrescriptionButton.setDisable(end >= prescriptions.size());
    }

    // Handle Prev Page for Diagnostiques
    @FXML
    private void handlePrevDiagnostiquePage() {
        if (currentDiagnostiquePage > 0) {
            currentDiagnostiquePage--;
            updateDiagnostiqueCards();
        }
    }

    // Handle Next Page for Diagnostiques
    @FXML
    private void handleNextDiagnostiquePage() {
        if ((currentDiagnostiquePage + 1) * ITEMS_PER_PAGE < diagnostiques.size()) {
            currentDiagnostiquePage++;
            updateDiagnostiqueCards();
        }
    }

    // Handle Prev Page for Prescriptions
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

}