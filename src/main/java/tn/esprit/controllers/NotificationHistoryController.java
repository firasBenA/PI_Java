package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.List;

public class NotificationHistoryController {

    @FXML private ListView<String> notificationListView;

    public void setNotifications(List<String> notifications) {
        notificationListView.setItems(FXCollections.observableArrayList(notifications));
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) notificationListView.getScene().getWindow();
        stage.close();
    }
}