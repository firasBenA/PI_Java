package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import tn.esprit.models.Prescription;
import tn.esprit.models.User;
import tn.esprit.repository.UserRepository;
import tn.esprit.repository.UserRepositoryImpl;
import tn.esprit.services.AuthService;
import tn.esprit.services.ServicePrescription;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PrescriptionCardsController implements Initializable {

    @FXML
    private GridPane Container;
    private User currentUser;
    @FXML
    private TextField searchField;

    private AuthService authService;
    private int col = 0;
    private int row = 0;

    private List<Node> allPrescriptionCards = new ArrayList<>();
    private static final double CARD_MARGIN = 10.0;

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (authService == null) {
            try {
                UserRepository userRepository = new UserRepositoryImpl();
                authService = AuthService.getInstance(userRepository);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible d'initialiser le service d'authentification", Alert.AlertType.ERROR);
            }
        }

        if (Container == null) {
            showAlert("Erreur", "Conteneur de prescriptions non initialisé", Alert.AlertType.ERROR);
            return;
        }

        loadPrescriptions();

        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterPrescriptions(newValue);
            });
        }
    }

    public void loadPrescriptions() {
        User currentUser = authService.getCurrentUser();

        if (currentUser == null) {
            System.out.println("No user logged in.");
            return;
        }

        ServicePrescription service = new ServicePrescription();
        List<Prescription> allPrescriptions = service.getAll();
        List<Prescription> myPrescriptions = new ArrayList<>();

        for (Prescription p : allPrescriptions) {
            if (p.getMedecin_id() == currentUser.getId()) {
                myPrescriptions.add(p);
            }
        }

        Container.getChildren().clear();
        col = 0;
        row = 0;

        for (Prescription p : myPrescriptions) {
            addCardToGrid(p);
        }
    }

    private void addCardToGrid(Prescription prescription) {
        try {
            if (authService == null) {
                showAlert("Erreur", "Service d'authentification non initialisé", Alert.AlertType.ERROR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Prescription.fxml"));
            BorderPane card = loader.load();

            PrescriptionController controller = loader.getController();

            controller.setAuthService(authService);
            controller.setCurrentUser(authService.getCurrentUser());

            controller.setData(prescription);

            allPrescriptionCards.add(card);

            Container.add(card, col, row);
            GridPane.setMargin(card, new Insets(CARD_MARGIN));

            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la carte de prescription: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void addNewPrescription(Prescription prescription) {
        if (prescription != null) {
            addCardToGrid(prescription);
        }
    }

    private void displayPrescriptions(List<Node> prescriptionsToShow) {
        Container.getChildren().clear();
        col = 0;
        row = 0;

        for (Node card : prescriptionsToShow) {
            if (col == 3) {
                col = 0;
                row++;
            }
            Container.add(card, col++, row);
            GridPane.setMargin(card, new Insets(CARD_MARGIN));
        }
    }

    private void filterPrescriptions(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            displayPrescriptions(allPrescriptionCards);
            return;
        }

        List<Node> filteredCards = new ArrayList<>();

        for (Node card : allPrescriptionCards) {
            if (card instanceof BorderPane) {
                BorderPane borderPane = (BorderPane) card;

                Label titreLabel = (Label) borderPane.lookup("#titre");

                if (titreLabel != null && titreLabel.getText() != null &&
                        titreLabel.getText().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredCards.add(card);
                }
            }
        }

        displayPrescriptions(filteredCards);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}