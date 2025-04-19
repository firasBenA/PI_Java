package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import tn.esprit.models.Prescription;
import tn.esprit.services.ServicePrescription;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PrescriptionCardsController implements Initializable {

    @FXML
    private GridPane Container;

    // Track column and row globally
    private int col = 0;
    private int row = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadPrescriptions();
    }

    private void loadPrescriptions() {
        ServicePrescription service = new ServicePrescription();
        List<Prescription> prescList = service.getAll();

        for (Prescription p : prescList) {
            addCardToGrid(p);
        }
    }

    private void addCardToGrid(Prescription prescription) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Prescription.fxml"));
            BorderPane card = loader.load();

            PrescriptionController controller = loader.getController();
            controller.setData(prescription);

            Container.add(card, col, row);

            // Update col and row for next card
            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addNewPrescription(Prescription prescription) {
        addCardToGrid(prescription); // reuses same logic
    }
}
