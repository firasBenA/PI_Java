package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;

public class NotificationHistoryController {

    @FXML private VBox notificationsContainer;

    public void setNotifications(List<String> notifications) {
        notificationsContainer.getChildren().clear();

        for (String notification : notifications) {
            // Create card for each notification
            StackPane card = new StackPane();
            card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

            Label notificationLabel = new Label(notification);
            notificationLabel.setFont(Font.font("System", 14));
            notificationLabel.setTextFill(Color.web("#34495e"));
            notificationLabel.setWrapText(true);
            notificationLabel.setMaxWidth(600);

            card.getChildren().add(notificationLabel);
            notificationsContainer.getChildren().add(card);
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) notificationsContainer.getScene().getWindow();
        stage.close();
    }
}