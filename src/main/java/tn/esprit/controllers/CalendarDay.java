package tn.esprit.controllers;

import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import java.time.LocalDate;

public class CalendarDay extends StackPane {
    public interface DateSelectionListener {
        void onDateSelected(LocalDate date);
    }

    private LocalDate date;
    private Button dayButton;
    private DateSelectionListener listener;

    public CalendarDay(LocalDate date) {
        this.date = date;
        setupUI();
    }

    private void setupUI() {
        this.setPrefSize(40, 40);

        dayButton = new Button(String.valueOf(date.getDayOfMonth()));
        dayButton.setStyle("-fx-background-radius: 20; -fx-min-width: 30; -fx-min-height: 30;");

        // Style différent pour aujourd'hui
        if (date.equals(LocalDate.now())) {
            dayButton.setStyle("-fx-background-color: #14967f; -fx-text-fill: white; -fx-background-radius: 20;");
        }

        dayButton.setOnAction(e -> {
            if (listener != null) {
                listener.onDateSelected(date);
            }
        });

        this.getChildren().add(dayButton);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setSelected(boolean selected) {
        if (selected) {
            dayButton.setStyle("-fx-background-color: #095d7e; -fx-text-fill: white; -fx-background-radius: 20;");
        } else if (date.equals(LocalDate.now())) {
            dayButton.setStyle("-fx-background-color: #14967f; -fx-text-fill: white; -fx-background-radius: 20;");
        } else {
            dayButton.setStyle("-fx-background-radius: 20;");
        }
    }

    public void setOnDateSelected(DateSelectionListener listener) {
        this.listener = listener;
    }
}