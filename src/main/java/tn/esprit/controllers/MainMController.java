package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class MainMController {


    @FXML
    private Label prescriptionLabel;

    @FXML
    private Label diagnostiqueLabel;
    @FXML
    private BorderPane rootPane;

    @FXML
    public void handleDiagnostiqueClick(MouseEvent event) {
        setCenterContent("/DiagnostiqueM.fxml");

        // Add 'selected' style to Diagnostique
        diagnostiqueLabel.getStyleClass().add("selected");

        // Remove 'selected' style from Prescription
        prescriptionLabel.getStyleClass().remove("selected");

    }

    public void setCenterContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Pane newLoadedPane = loader.load();
            rootPane.setCenter(newLoadedPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePrescriptionClick(MouseEvent event) {
        setCenterContent("/PrescriptionM.fxml");

        // Add 'selected' style to Prescription
        prescriptionLabel.getStyleClass().add("selected");

        // Remove 'selected' style from Diagnostique
        diagnostiqueLabel.getStyleClass().remove("selected");
    }

    @FXML
    public void initialize() {
        if (rootPane == null) {
            System.out.println("Error: rootPane is null at initialize.");
        }
    }
}
