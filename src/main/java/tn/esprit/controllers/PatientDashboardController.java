package tn.esprit.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.models.Prescription;
import tn.esprit.services.ServicePrescription;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import javafx.scene.control.Button;


public class PatientDashboardController implements Initializable {

    @FXML private TableView<Prescription> prescriptionTable;
    @FXML private TableColumn<Prescription, Integer> idCol;
    @FXML private TableColumn<Prescription, String> titreCol;
    @FXML private TableColumn<Prescription, String> contenueCol;
    @FXML private TableColumn<Prescription, LocalDate> dateCol;
    @FXML private TableColumn<Prescription, Void> downloadCol;

    private ObservableList<Prescription> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadPrescriptions();
    }

    private void setupTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        contenueCol.setCellValueFactory(new PropertyValueFactory<>("contenue"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date_prescription"));

        downloadCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Télécharger");
            {
                btn.setOnAction(e -> {
                    Prescription p = getTableView().getItems().get(getIndex());
                    downloadPdf(p.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    private final ServicePrescription servicePrescription = new ServicePrescription();

    private void loadPrescriptions() {
        List<Prescription> prescriptions = servicePrescription.getAll();
        masterData.setAll(prescriptions);
        prescriptionTable.setItems(masterData);
    }

    private void downloadPdf(int prescriptionId) {
        try {
            File file = ServicePrescription.downloadPDF(prescriptionId);
            if (file != null) {
                Desktop.getDesktop().open(file); // or save elsewhere
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "PDF not found!");
                alert.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


