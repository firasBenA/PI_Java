package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.services.ServicePanier;
import tn.esprit.models.Panier;

import java.sql.SQLException;

public class PanierController {

    @FXML private TableView<Panier> tablePanier;
    @FXML private TableColumn<Panier, Integer> colId;
    @FXML private TableColumn<Panier, Integer> colUserId;
    @FXML private TextField tfUserId;

    private final PanierDAO panierDAO = new PanierDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colUserId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getUserId()).asObject());

        refreshTable();
    }

    private void refreshTable() {
        try {
            ObservableList<Panier> list = FXCollections.observableArrayList(panierDAO.getAllPaniers());
            tablePanier.setItems(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addPanier() {
        try {
            int userId = Integer.parseInt(tfUserId.getText());
            panierDAO.addPanier(new Panier(userId));
            refreshTable();
            tfUserId.clear();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void updatePanier() {
        Panier selected = tablePanier.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                selected.setUserId(Integer.parseInt(tfUserId.getText()));
                panierDAO.updatePanier(selected);
                refreshTable();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void deletePanier() {
        Panier selected = tablePanier.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                panierDAO.deletePanier(selected.getId());
                refreshTable();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
