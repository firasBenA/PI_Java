package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.services.ServicePanier;
import tn.esprit.models.Panier;

import java.sql.SQLException;
import java.sql.Timestamp;

public class PanierController {

    @FXML private TableView<Panier> tablePanier;
    @FXML private TableColumn<Panier, Integer> colId;
    @FXML private TableColumn<Panier, Integer> colUserId;
    @FXML private TextField tfUserId;

    private final ServicePanier servicePanier = new ServicePanier();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colUserId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getUserId()).asObject());

        refreshTable();
    }

    private void refreshTable() {
        ObservableList<Panier> list = FXCollections.observableArrayList(servicePanier.getAll());
        tablePanier.setItems(list);
    }

    @FXML
    private void addPanier() {
        try {
            int userId = Integer.parseInt(tfUserId.getText());
            Timestamp now = new Timestamp(System.currentTimeMillis());
            Panier newPanier = new Panier(userId, now, now); // Create a full Panier
            servicePanier.add(newPanier);
            refreshTable();
            tfUserId.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void updatePanier() {
        Panier selected = tablePanier.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setUserId(Integer.parseInt(tfUserId.getText()));
            selected.setMajLe(new Timestamp(System.currentTimeMillis())); // Update last modified
            servicePanier.update(selected);
            refreshTable();
        }
    }

    @FXML
    private void deletePanier() {
        Panier selected = tablePanier.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                servicePanier.delete(selected); // Pass the full Panier, not ID
                refreshTable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
