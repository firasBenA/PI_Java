package tn.esprit.controllers;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CalendarView extends VBox {
    private YearMonth currentYearMonth;
    private Label monthYearLabel;
    private GridPane calendarGrid;
    private List<CalendarDay> calendarDays;
    private LocalDate selectedDate;

    public CalendarView() {
        currentYearMonth = YearMonth.now();
        calendarDays = new ArrayList<>();
        setupUI();
        populateCalendar();
    }

    private void setupUI() {
        // Header avec navigation
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setSpacing(10);

        Button prevBtn = new Button("<");
        prevBtn.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            populateCalendar();
        });

        monthYearLabel = new Label();
        monthYearLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Button nextBtn = new Button(">");
        nextBtn.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            populateCalendar();
        });

        header.getChildren().addAll(prevBtn, monthYearLabel, nextBtn);

        // Jours de la semaine
        HBox daysOfWeek = new HBox();
        daysOfWeek.setAlignment(Pos.CENTER);
        daysOfWeek.setSpacing(5);
        String[] dayNames = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (String day : dayNames) {
            Label dayLabel = new Label(day);
            dayLabel.setPrefWidth(40);
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setStyle("-fx-font-weight: bold;");
            daysOfWeek.getChildren().add(dayLabel);
        }

        // Grille du calendrier
        calendarGrid = new GridPane();
        calendarGrid.setHgap(5);
        calendarGrid.setVgap(5);
        calendarGrid.setAlignment(Pos.CENTER);

        this.getChildren().addAll(header, daysOfWeek, calendarGrid);
        this.setSpacing(10);
    }

    private void populateCalendar() {
        calendarGrid.getChildren().clear();
        calendarDays.clear();

        // Mettre à jour le label du mois/année
        monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeekValue = firstOfMonth.getDayOfWeek().getValue(); // 1=Lundi, 7=Dimanche

        // Remplir les jours du mois
        for (int day = 1; day <= currentYearMonth.lengthOfMonth(); day++) {
            LocalDate date = currentYearMonth.atDay(day);
            CalendarDay calendarDay = new CalendarDay(date);
            calendarDays.add(calendarDay);

            int row = (day + dayOfWeekValue - 2) / 7;
            int col = (dayOfWeekValue + day - 2) % 7;

            calendarGrid.add(calendarDay, col, row);
        }
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
        calendarDays.forEach(day -> day.setSelected(day.getDate().equals(date)));
    }

    public List<CalendarDay> getCalendarDays() {
        return calendarDays;
    }

    public void setOnDateSelected(CalendarDay.DateSelectionListener listener) {
        calendarDays.forEach(day -> day.setOnDateSelected(date -> {
            this.selectedDate = date;
            listener.onDateSelected(date);
        }));
    }
}