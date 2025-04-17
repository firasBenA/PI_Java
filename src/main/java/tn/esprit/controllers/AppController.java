import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.animation.TranslateTransition;
import java.time.LocalDate;
import javafx.util.Duration;

public class AppController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private StackPane contentPane;

    @FXML
    private StackPane sidebarContainer;

    @FXML
    private VBox sidebar;

    @FXML
    private Label defaultLabel;

    @FXML
    private Button composeButton;

    @FXML
    private Button eventsButton;

    @FXML
    private Button hamburgerButton;

    private VBox eventsSidebar;
    private boolean isSidebarVisible = false;

    // Initialize the controller
    public void initialize() {
        // Ensure default content is visible initially
        defaultLabel.setVisible(true);
        sidebar.setPrefWidth(200); // Set the width of the sidebar
        sidebar.setVisible(true); // Ensure the sidebar content is visible
        sidebarContainer.setPrefWidth(200); // Ensure the container has the correct width
    }

    // Toggle the sidebar visibility with animation
    @FXML
    private void toggleSidebar() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), sidebar);
        if (isSidebarVisible) {
            transition.setToX(-200); // Hide sidebar
            hamburgerButton.setText("☰");
        } else {
            transition.setToX(0); // Show sidebar
            hamburgerButton.setText("✖"); // Change to close icon
        }
        transition.play();
        isSidebarVisible = !isSidebarVisible;
    }

    // Methods for each member's content (placeholders for now)
    @FXML
    private void showMember1Content() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(new Label("Member 1's Work"));
        hideEventsSidebar();
        if (isSidebarVisible) {
            toggleSidebar(); // Hide sidebar only if it's currently visible
        }
    }

    @FXML
    private void showMember2Content() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(new Label("Member 2's Work"));
        hideEventsSidebar();
        if (isSidebarVisible) {
            toggleSidebar();
        }
    }

    @FXML
    private void showMember3Content() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(new Label("Member 3's Work"));
        hideEventsSidebar();
        if (isSidebarVisible) {
            toggleSidebar();
        }
    }

    @FXML
    private void showMember5Content() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(new Label("Member 5's Work"));
        hideEventsSidebar();
        if (isSidebarVisible) {
            toggleSidebar();
        }
    }

    @FXML
    private void showMember6Content() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(new Label("Member6's Work"));
        hideEventsSidebar();
        if (isSidebarVisible) {
            toggleSidebar();
        }
    }

    // Show the Events Sidebar
    @FXML
    private void showEventsSidebar() {
        if (eventsSidebar == null) {
            eventsSidebar = new VBox(10);
            eventsSidebar.setStyle("-fx-padding: 10;");

            Button eventsOption = new Button("Events");
            eventsOption.setPrefWidth(150);
            eventsOption.setOnAction(e -> showCreateEventForm());

            Button articlesOption = new Button("Articles");
            articlesOption.setPrefWidth(150);
            articlesOption.setOnAction(e -> {
                contentPane.getChildren().clear();
                contentPane.getChildren().add(new Label("Articles Section"));
            });

            Button calendarOption = new Button("Calendar");
            calendarOption.setPrefWidth(150);
            calendarOption.setOnAction(e -> {
                contentPane.getChildren().clear();
                contentPane.getChildren().add(new Label("Calendar Section"));
            });

            Button trashOption = new Button("Trash");
            trashOption.setPrefWidth(150);
            trashOption.setOnAction(e -> {
                contentPane.getChildren().clear();
                contentPane.getChildren().add(new Label("Trash Section"));
            });

            eventsSidebar.getChildren().addAll(eventsOption, articlesOption, calendarOption, trashOption);
        }

        BorderPane borderPane = (BorderPane) contentPane.getParent();
        borderPane.setLeft(eventsSidebar);
        showCreateEventForm(); // Default to showing the "Create New Event" form
        if (isSidebarVisible) {
            toggleSidebar(); // Hide main sidebar after selection
        }
    }

    // Hide the Events Sidebar
    private void hideEventsSidebar() {
        BorderPane borderPane = (BorderPane) contentPane.getParent();
        borderPane.setLeft(null);
    }

    // Show the "Create New Event" form
    private void showCreateEventForm() {
        contentPane.getChildren().clear();

        VBox form = new VBox(10);
        form.setStyle("-fx-padding: 20;");
        form.setMaxWidth(400);

        Label title = new Label("Create new Event");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label date = new Label("24 Jan, 2023");
        date.setStyle("-fx-font-size: 12px;");

        HBox eventNameBox = new HBox(10);
        Label eventNameLabel = new Label("Event name");
        TextField eventNameField = new TextField();
        eventNameField.setPromptText("Example");
        eventNameBox.getChildren().addAll(eventNameLabel, eventNameField);

        HBox deadlineBox = new HBox(10);
        Label deadlineLabel = new Label("Deadline");
        DatePicker deadlinePicker = new DatePicker(LocalDate.of(2023, 1, 29));
        deadlineBox.getChildren().addAll(deadlineLabel, deadlinePicker);

        HBox assignBox = new HBox(10);
        Label assignLabel = new Label("Assign to");
        Button addButton = new Button("+");
        addButton.setStyle("-fx-background-radius: 50%;");
        assignBox.getChildren().addAll(assignLabel, addButton);

        TextField requiredField = new TextField();
        requiredField.setPromptText("Example");

        Button createButton = new Button("Create new to");
        createButton.setStyle("-fx-font-weight: bold; -fx-background-radius: 20;");

        form.getChildren().addAll(title, date, eventNameBox, deadlineBox, assignBox, requiredField, createButton);
        contentPane.getChildren().add(form);
    }
}