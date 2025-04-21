package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.services.ServicePanier;
import tn.esprit.models.Produit;


import java.sql.SQLException;

public class ProduitController {

    @FXML private TableView<Produit> tableProduit;
    @FXML private TableColumn<Produit, Integer> colId;
    @FXML private TableColumn<Produit, String> colName;
    @FXML private TableColumn<Produit, Double> colPrice;
    @FXML private TextField tfName;
    @FXML private TextField tfPrice;
    @FXML private TextField tfNom, tfPrix, tfStock, tfImage;
    @FXML private TextArea taDescription;


    private final ProduitDAO produitDAO = new ProduitDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        colPrice.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());

        refreshTable();
    }

    private void refreshTable() {
        try {
            ObservableList<Produit> list = FXCollections.observableArrayList(produitDAO.getAllProduits());
            tableProduit.setItems(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addProduit() {
        try {
        String nom = tfNom.getText();
        String desc = taDescription.getText();
        double prix = Double.parseDouble(tfPrix.getText());
        int stock = Integer.parseInt(tfStock.getText());
        String image = tfImage.getText();

        Produit produit = new Produit(nom, desc, prix, stock, image);
        produitDAO.addProduit(produit);
        refreshTable();
        clearFields();
        } catch (Exception e) {
        e.printStackTrace();
    }
}


    @FXML
    private void updateProduit() {
        Produit selected = tableProduit.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                selected.setName(tfName.getText());
                selected.setPrice(Double.parseDouble(tfPrice.getText()));
                produitDAO.updateProduit(selected);
                refreshTable();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void deleteProduit() {
        Produit selected = tableProduit.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                produitDAO.deleteProduit(selected.getId());
                refreshTable();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
