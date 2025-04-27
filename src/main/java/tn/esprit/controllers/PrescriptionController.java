package tn.esprit.controllers;

import com.itextpdf.layout.properties.TextAlignment;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import tn.esprit.models.Prescription;
import tn.esprit.models.User;
import tn.esprit.services.ServicePrescription;
import tn.esprit.services.ServiceUser;

import java.io.IOException;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import javafx.fxml.FXML;
import java.io.File;

public class PrescriptionController {


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

    private Prescription currentPrescription;


    private Node cardNode;
    private Pane parentContainer;

    private String userRole;

    public PrescriptionController() {
        userRole = "patient";
    }


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


    public void setData(Prescription prescription, javafx.scene.Node cardNode, javafx.scene.layout.Pane parentContainer) {
        if ("patient".equals(userRole)) {
            deleteBtn.setVisible(false);
            modifyBtn.setVisible(false);
        } else {
            deleteBtn.setVisible(true);
            modifyBtn.setVisible(true);
        }


        this.currentPrescription = prescription;
        this.cardNode = cardNode;
        this.parentContainer = parentContainer;

        int staticPatientId = 1;

        ServiceUser patientService = new ServiceUser();
        User patient = patientService.getUserById(staticPatientId);

        titre.setText(prescription.getTitre());
        contenue.setText(prescription.getContenue());
        numTel.setText("27025110");
        datePrescription.setText(prescription.getDate_prescription().toString());
        nom.setText("firas");
        prenom.setText("ba");
    }


    @FXML
    private void onDeleteClicked() {
        ServicePrescription service = new ServicePrescription();
        boolean result = service.deletePrescriptionById(currentPrescription.getId());

        if (result) {
            System.out.println("Prescription supprimée !");
            if (parentContainer != null && cardNode != null) {
                parentContainer.getChildren().remove(cardNode);
            }
        } else {
            System.out.println("Erreur lors de la suppression.");
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

}