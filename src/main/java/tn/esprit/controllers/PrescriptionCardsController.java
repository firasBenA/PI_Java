package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import tn.esprit.models.Prescription;
import tn.esprit.services.ServicePrescription;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PrescriptionCardsController implements Initializable {

    @FXML
    private GridPane Container;

    @FXML
    private TextField searchField;

    private int col = 0;
    private int row = 0;

    private List<Node> allPrescriptionCards = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadPrescriptions();

        // Add search listener
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterPrescriptions(newValue);
            });
        }
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
            controller.setData(prescription, card, Container);

            allPrescriptionCards.add(card); // Save every created card

            Container.add(card, col, row);
            GridPane.setMargin(card, new Insets(10));

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
        addCardToGrid(prescription); // reuse same logic
    }

    private void displayPrescriptions(List<Node> prescriptionsToShow) {
        Container.getChildren().clear();
        col = 0;
        row = 0;

        for (Node card : prescriptionsToShow) {
            if (col == 3) {
                col = 0;
                row++;
            }
            Container.add(card, col++, row);
            GridPane.setMargin(card, new Insets(10));
        }
    }

    private void filterPrescriptions(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            displayPrescriptions(allPrescriptionCards);
            return;
        }

        List<Node> filteredCards = new ArrayList<>();

        for (Node card : allPrescriptionCards) {
            if (card instanceof BorderPane) {
                BorderPane borderPane = (BorderPane) card;

                // Get the prescription title Label by fx:id="titre"
                Label titreLabel = (Label) borderPane.lookup("#titre");

                if (titreLabel != null && titreLabel.getText().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredCards.add(card);
                }
            }
        }

        displayPrescriptions(filteredCards);
    }


}
