package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import tn.esprit.interfaces.IService;
import tn.esprit.models.Prescription;
import tn.esprit.services.ServicePrescription;

import java.sql.Date;
import java.util.List;

public class GestionPrescription {
    @FXML
    private TextField titre;
    @FXML
    private TextField contenue;
    @FXML
    private DatePicker datePrescription;
    IService<Prescription> presc = new ServicePrescription();
    @FXML
    private Label lbPrescription;

//////////
    @FXML
    private TableView<Prescription> prescriptionTable;
    @FXML
    private TableColumn<Prescription, String> idCol;
    @FXML
    private TableColumn<Prescription, String> titreCol;
    @FXML
    private TableColumn<Prescription, String> contenueCol;
    @FXML
    private TableColumn<Prescription, Date> dateCol;
    @FXML
    private TableColumn<Prescription, Void> actionsCol;
//////////////
@FXML
public void ajouterPrescription(ActionEvent actionEvent) {
    String errorMessage = "";

    if (titre.getText().isEmpty()) {
        errorMessage += "Le titre est requis.\n";
    }

    if (contenue.getText().isEmpty()) {
        errorMessage += "Le contenu est requis.\n";
    }

    if (datePrescription.getValue() == null) {
        errorMessage += "La date de prescription est requise.\n";
    }

    if (!errorMessage.isEmpty()) {
        lbPrescription.setText(errorMessage);
        lbPrescription.setTextFill(javafx.scene.paint.Color.RED); // Set error color
    } else {
        Prescription p = new Prescription();
        p.setDossier_medical_id(1);
        p.setDiagnostique_id(20);
        p.setMedecin_id(3);
        p.setPatient_id(1);
        p.setDate_prescription(Date.valueOf(datePrescription.getValue()));
        p.setTitre(titre.getText());
        p.setContenue(contenue.getText());

        presc.add(p);

        titre.clear();
        contenue.clear();
        datePrescription.setValue(null);

        List<Prescription> prescriptions = presc.getAll();
        prescriptionTable.getItems().setAll(prescriptions);

        lbPrescription.setText("Prescription ajoutée avec succès!");
        lbPrescription.setTextFill(javafx.scene.paint.Color.GREEN); // Success color
    }
}






    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        contenueCol.setCellValueFactory(new PropertyValueFactory<>("contenue"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date_prescription"));

        addActionButtonsToTable();

        List<Prescription> prescriptions = presc.getAll();
        prescriptionTable.getItems().setAll(prescriptions);
    }


    private void addActionButtonsToTable() {
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button modifyBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox pane = new HBox(10, modifyBtn, deleteBtn);

            {
                modifyBtn.setOnAction(event -> {
                    Prescription selected = getTableView().getItems().get(getIndex());
                });

                deleteBtn.setOnAction(event -> {
                    Prescription p = getTableView().getItems().get(getIndex());
                    presc.delete(p);
                    prescriptionTable.getItems().remove(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }




}
