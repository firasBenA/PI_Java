package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import tn.esprit.models.Diagnostique;
import tn.esprit.models.Medecin;
import tn.esprit.models.User;
import tn.esprit.repository.UserRepository;
import tn.esprit.repository.UserRepositoryImpl;
import tn.esprit.services.AuthService;
import tn.esprit.services.ServiceDiagnostique;
import tn.esprit.utils.SceneManager;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DiagnostiqueCardsController implements Initializable {

    private AuthService authService;
    @FXML
    private TextField searchField;
    @FXML
    private GridPane Container;

    private List<Node> allDiagnostiqueCards = new ArrayList<>();


    private SceneManager sceneManager;
    private Medecin currentUser;

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public void setCurrentUser(User user) {
        if (user instanceof Medecin) {
            this.currentUser = (Medecin) user;
        } else {
            System.out.println("Erreur Utilisateur invalide pour le tableau de bord Médecin");
        }
    }    public DiagnostiqueCardsController() {
    }

    // Your custom constructor to inject AuthService
    public DiagnostiqueCardsController(AuthService authService) {
        this.authService = authService;
    }

    public void initialize(URL url, ResourceBundle rb) {
        if (authService == null) {
            try {
                UserRepository userRepository = new UserRepositoryImpl();
                authService = AuthService.getInstance(userRepository);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Erreur Impossible d'initialiser le service d'authentification");
            }
        }

        // Fetch all pending diagnostiques
        ServiceDiagnostique service = new ServiceDiagnostique();
        List<Diagnostique> diagList = service.getPendingDiagnostiques();

        // Filter diagnostiques for the logged-in user (e.g., if the user is a doctor, filter by their ID)
        List<Diagnostique> relevantDiagnostiques = new ArrayList<>();
        if (authService.getCurrentUser() != null) {
            User currentUser = authService.getCurrentUser();

            for (Diagnostique diag : diagList) {
                // If the user is a doctor, filter diagnostiques related to the doctor
                if (currentUser.getRoles().contains("ROLE_MEDECIN") && diag.getMedecinId() == currentUser.getId()) {
                    relevantDiagnostiques.add(diag);
                }

            }
        }

        try {
            for (Diagnostique diag : relevantDiagnostiques) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Diagnostique.fxml"));
                VBox pane = fxmlLoader.load();

                DiagnostiqueController controller = fxmlLoader.getController();

                // Ensure authService is set before calling the controller's methods
                if (authService != null) {
                    controller.setAuthService(authService);
                    controller.setCurrentUser(authService.getCurrentUser());
                }

                controller.setData(diag);

                // Store each pane in the list
                allDiagnostiqueCards.add(pane);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Show only the relevant diagnostiques
        displayDiagnostiques(allDiagnostiqueCards);

        // Add listener for search
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterDiagnostiques(newValue);
        });
    }


    private void displayDiagnostiques(List<Node> diagnostiquesToShow) {
        Container.getChildren().clear();
        int row = 0;
        int column = 0;

        for (Node card : diagnostiquesToShow) {
            if (column == 3) {
                column = 0;
                row++;
            }
            Container.add(card, column++, row);
            GridPane.setMargin(card, new Insets(10));
        }
    }

    private void filterDiagnostiques(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            displayDiagnostiques(allDiagnostiqueCards);
            return;
        }

        List<Node> filteredCards = new ArrayList<>();

        for (Node card : allDiagnostiqueCards) {
            if (card instanceof VBox) {
                VBox vbox = (VBox) card;
                for (Node child : vbox.getChildren()) {
                    if (child instanceof Label) {
                        Label nameLabel = (Label) child;
                        if (nameLabel.getText().toLowerCase().contains(searchText.toLowerCase())) {
                            filteredCards.add(card);
                            break;
                        }
                    }
                }
            }
        }

        displayDiagnostiques(filteredCards);
    }
}
