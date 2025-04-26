package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.models.Reclamation;
import tn.esprit.services.ServiceReclamation;

import java.time.LocalDate;

public class GestionReclamation {

    @FXML
    private DatePicker DPDate;

    @FXML
    private TextField TFdescription;

    @FXML
    private TextField TFetat;

    @FXML
    private TextField TFsujet;

    @FXML
    private TextField TFid;

    @FXML
    private ListView<Reclamation> listViewReclamations;

    private final ServiceReclamation sr = new ServiceReclamation();
    private ObservableList<Reclamation> data;

    public GestionReclamation(ListView<Reclamation> listViewReclamations) {
        this.listViewReclamations = listViewReclamations;
    }

    @FXML
    public void initialize() {
        data = FXCollections.observableArrayList(sr.getAll());
        listViewReclamations.setItems(data);

        // Afficher chaque réclamation sous forme de texte simple
        listViewReclamations.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Reclamation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("[" + item.getId() + "] " + item.getSujet());
                }
            }
        });

        // Écoute de sélection pour affichage automatique
        listViewReclamations.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                TFid.setText(String.valueOf(selected.getId()));
                TFsujet.setText(selected.getSujet());
                TFdescription.setText(selected.getDescription());
                DPDate.setValue(selected.getDateDebut());
                TFetat.setText(selected.getEtat());
            }
        });
    }

    @FXML
    void Save(ActionEvent event) {
        try {
            LocalDate date = DPDate.getValue();
            if (date == null) {
                showAlert("Erreur", "Veuillez sélectionner une date !");
                return;
            }

            Reclamation r = new Reclamation(
                    TFsujet.getText(),
                    TFdescription.getText(),
                    date,
                    "en attente",
                    1
            );

            sr.add(r);
            showAlert("Succès", "Réclamation ajoutée !");
            refreshList();
        } catch (Exception e) {
            showAlert("Erreur", "Message : " + e.getMessage());
        }
    }

    @FXML
    void Update(ActionEvent event) {
        try {
            int id = Integer.parseInt(TFid.getText());
            Reclamation r = new Reclamation(
                    id,
                    TFsujet.getText(),
                    TFdescription.getText(),
                    DPDate.getValue(),
                    TFetat.getText(),
                    1
            );

            sr.update(r);
            showAlert("Succès", "Réclamation mise à jour !");
            refreshList();
        } catch (Exception e) {
            showAlert("Erreur", "Message : " + e.getMessage());
        }
    }

    @FXML
    void Delete(ActionEvent event) {
        try {
            int id = Integer.parseInt(TFid.getText());
            sr.delete(new Reclamation(id));
            showAlert("Succès", "Réclamation supprimée !");
            refreshList();
        } catch (Exception e) {
            showAlert("Erreur", "Message : " + e.getMessage());
        }
    }

    private void refreshList() {
        data.setAll(sr.getAll());
        clearFields();
    }

    private void clearFields() {
        TFid.clear();
        TFsujet.clear();
        TFdescription.clear();
        TFetat.clear();
        DPDate.setValue(null);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}