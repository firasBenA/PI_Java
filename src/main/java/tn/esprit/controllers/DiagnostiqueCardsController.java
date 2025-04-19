package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import tn.esprit.models.Diagnostique;
import tn.esprit.services.ServiceDiagnostique;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DiagnostiqueCardsController implements Initializable {

    @FXML
    private GridPane Container;

    private List<Diagnostique> diagnostiques;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ServiceDiagnostique service = new ServiceDiagnostique();
        List<Diagnostique> diagList = service.getPendingDiagnostiques();

        int row = 0;
        int column = 0;

        try {
            for (Diagnostique diag : diagList) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Diagnostique.fxml"));
                VBox pane = fxmlLoader.load();

                DiagnostiqueController controller = fxmlLoader.getController();
                controller.setData(diag);

                if (column == 3) {
                    column = 0;
                    row++;
                }

                Container.add(pane, column++, row);
                GridPane.setMargin(pane, new Insets(10));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Diagnostique> diagnostiques() {
        List<Diagnostique> ls = new ArrayList<>();
        Diagnostique diag = new Diagnostique();
        diag.setNom("test");
        diag.setSelectedSymptoms("test test test");
        diag.setDateDiagnostique(Date.valueOf("2024-04-16"));
        diag.setZoneCorps("test");

        ls.add(diag);


        return ls;
    }
}
