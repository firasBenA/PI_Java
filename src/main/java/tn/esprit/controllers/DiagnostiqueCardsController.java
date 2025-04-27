package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import tn.esprit.models.Diagnostique;
import tn.esprit.services.ServiceDiagnostique;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DiagnostiqueCardsController implements Initializable {

    @FXML
    private TextField searchField;
    @FXML
    private GridPane Container;

    private List<Node> allDiagnostiqueCards = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ServiceDiagnostique service = new ServiceDiagnostique();
        List<Diagnostique> diagList = service.getPendingDiagnostiques();

        try {
            for (Diagnostique diag : diagList) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Diagnostique.fxml"));
                VBox pane = fxmlLoader.load();

                DiagnostiqueController controller = fxmlLoader.getController();
                controller.setData(diag);

                // Store each pane in the list
                allDiagnostiqueCards.add(pane);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Show all cards initially
        displayDiagnostiques(allDiagnostiqueCards);

        // Add listener for search
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterDiagnostiques(newValue);
        });
    }

    private void displayDiagnostiques(List<Node> diagnostiquesToShow) {
        Container.getChildren().clear();
        int row = 0;
        int column = 0;

        for (Node card : diagnostiquesToShow) {
            if (column == 3) {
                column = 0;
                row++;
            }
            Container.add(card, column++, row);
            GridPane.setMargin(card, new Insets(10));
        }
    }

    private void filterDiagnostiques(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            displayDiagnostiques(allDiagnostiqueCards);
            return;
        }

        List<Node> filteredCards = new ArrayList<>();

        for (Node card : allDiagnostiqueCards) {
            if (card instanceof VBox) {
                VBox vbox = (VBox) card;
                for (Node child : vbox.getChildren()) {
                    if (child instanceof Label) {
                        Label nameLabel = (Label) child;
                        if (nameLabel.getText().toLowerCase().contains(searchText.toLowerCase())) {
                            filteredCards.add(card);
                            break;
                        }
                    }
                }
            }
        }

        displayDiagnostiques(filteredCards);
    }
}
