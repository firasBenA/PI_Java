package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import tn.esprit.models.Reclamation;
import tn.esprit.models.Reponse;
import tn.esprit.services.MailService;
import tn.esprit.services.ServiceReclamation;
import tn.esprit.services.ServiceReponse;
import tn.esprit.utils.MyDataBase;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class GestionReponse {

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private HBox cardPane;

    @FXML
    private TextArea TAContenu;

    @FXML
    private ComboBox<String> filterEtatComboBox;

    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    @FXML
    private Label pageLabel;

    private final MailService mailService = new MailService();
    private final ServiceReclamation serviceReclamation = new ServiceReclamation();
    private final ServiceReponse serviceReponse = new ServiceReponse();
    private ObservableList<Reclamation> observableReclamations;
    private Reclamation selectedReclamation;
    private VBox selectedCard;
    private int currentPage = 1;
    private final int itemsPerPage = 3;
    private static final int DESCRIPTION_DISPLAY_LIMIT = 110;
    private static final String STATIC_RECIPIENT_EMAIL = "mechket.mlayeh@gmail.com"; // Static email for testing

    @FXML
    public void initialize() {
        // Initialize ComboBox with filter options
        filterEtatComboBox.setItems(FXCollections.observableArrayList("Tous", "En Attente", "Traité"));
        filterEtatComboBox.setValue("Tous");
        filterEtatComboBox.setOnAction(event -> {
            currentPage = 1;
            loadReclamations();
        });

        loadReclamations();
    }
//
    private void loadReclamations() {
        String selectedEtat = filterEtatComboBox.getValue();
        List<Reclamation> reclamationList;

        if (selectedEtat == null || selectedEtat.equals("Tous")) {
            reclamationList = serviceReclamation.getAll();
        } else {
            reclamationList = serviceReclamation.getByEtat(selectedEtat);
        }

        observableReclamations = FXCollections.observableArrayList(reclamationList);
        updateCardPane();
    }

    private void updateCardPane() {
        cardPane.getChildren().clear();
        selectedReclamation = null;
        selectedCard = null;

        int totalItems = observableReclamations.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);

        // Ensure currentPage is valid
        if (currentPage < 1) {
            currentPage = 1;
        } else if (currentPage > totalPages && totalPages > 0) {
            currentPage = totalPages;
        }

        // Update pagination controls
        pageLabel.setText("Page " + currentPage + " of " + (totalPages == 0 ? 1 : totalPages));
        prevButton.setDisable(currentPage <= 1);
        nextButton.setDisable(currentPage >= totalPages);

        // Calculate start and end indices for the current page
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalItems);

        // Add cards for the current page
        for (int i = startIndex; i < endIndex; i++) {
            Reclamation reclamation = observableReclamations.get(i);
            VBox card = createReclamationCard(reclamation);
            cardPane.getChildren().add(card);
        }
    }

    private VBox createReclamationCard(Reclamation reclamation) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(240);
        card.setPrefHeight(280);
        card.setMinHeight(280);

        Region imagePlaceholder = new Region();
        imagePlaceholder.getStyleClass().add("image-placeholder");
        imagePlaceholder.setPrefHeight(75);
        imagePlaceholder.setPrefWidth(75);

        Text sujetTitle = new Text(reclamation.getSujet());
        sujetTitle.getStyleClass().add("card-title");
        sujetTitle.setWrappingWidth(210);

        String description = reclamation.getDescription();
        Text descriptionText = new Text();
        descriptionText.getStyleClass().add("card-subtitle");
        descriptionText.setWrappingWidth(210);
        descriptionText.setText(description.length() > DESCRIPTION_DISPLAY_LIMIT ?
                description.substring(0, DESCRIPTION_DISPLAY_LIMIT) : description);

        Text dateLabel = new Text("Date: ");
        dateLabel.getStyleClass().add("card-label");
        Text dateText = new Text(reclamation.getDateDebut().toString());
        dateText.getStyleClass().add("card-text");

        Text etatText = new Text(reclamation.getEtat());
        etatText.getStyleClass().add("etat-badge");

        HBox dateBox = new HBox(5, dateLabel, dateText);
        HBox etatBox = new HBox(etatText);
        etatBox.getStyleClass().add("etat-container");

        VBox detailsBox = new VBox(3);
        detailsBox.getStyleClass().add("card-details");
        detailsBox.getChildren().addAll(dateBox, etatBox);

        VBox descriptionContainer = new VBox(5);
        descriptionContainer.getChildren().add(descriptionText);
        if (description.length() > DESCRIPTION_DISPLAY_LIMIT) {
            Button seeMoreButton = new Button("Voir plus");
            seeMoreButton.getStyleClass().add("button-see-more");
            seeMoreButton.setOnAction(event -> showFullDescription(reclamation.getDescription()));
            descriptionContainer.getChildren().add(seeMoreButton);
        }

        card.getChildren().addAll(imagePlaceholder, sujetTitle, descriptionContainer, detailsBox);

        card.setOnMouseClicked(event -> {
            if (selectedReclamation != null) {
                cardPane.getChildren().stream()
                        .filter(node -> node instanceof VBox)
                        .map(node -> (VBox) node)
                        .forEach(vbox -> vbox.getStyleClass().remove("selected"));
            }
            selectedReclamation = reclamation;
            card.getStyleClass().add("selected");

        });

        return card;
    }
    private void showFullDescription(String fullDescription) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Description complète");

        VBox dialogContent = new VBox(15);
        dialogContent.setPadding(new javafx.geometry.Insets(20));
        dialogContent.getStyleClass().add("modal-dialog");

        Text dialogTitle = new Text("Détails de la Réclamation");
        dialogTitle.getStyleClass().add("modal-title");

        Text descriptionText = new Text(fullDescription);
        descriptionText.getStyleClass().add("modal-text");
        descriptionText.setWrappingWidth(400);

        Button closeButton = new Button("Fermer");
        closeButton.getStyleClass().add("modal-close-button");
        closeButton.setOnAction(event -> dialog.close());

        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);
        buttonContainer.getChildren().add(closeButton);

        dialogContent.getChildren().addAll(dialogTitle, descriptionText, buttonContainer);

        Scene dialogScene = new Scene(dialogContent, 500, 350);
        java.net.URL cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) {
            dialogScene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Error: CSS file not found for modal dialog at /tn/esprit/styles/styles.css");
        }

        FadeTransition fade = new FadeTransition(Duration.millis(300), dialogContent);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();

        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }


    @FXML
    public void goToPreviousPage(ActionEvent event) {
        if (currentPage > 1) {
            currentPage--;
            updateCardPane();
        }
    }

    @FXML
    public void goToNextPage(ActionEvent event) {
        int totalPages = (int) Math.ceil((double) observableReclamations.size() / itemsPerPage);
        if (currentPage < totalPages) {
            currentPage++;
            updateCardPane();
        }
    }

    @FXML
    public void Save(ActionEvent actionEvent) {
        try {
            String contenu = TAContenu.getText();
            LocalDate date = LocalDate.now();

            if (selectedReclamation == null) {
                showAlert("Erreur", "Veuillez sélectionner une réclamation !");
                return;
            }
            if (contenu.isEmpty()) {
                showAlert("Erreur", "Le contenu de la réponse ne peut pas être vide !");
                return;
            }

            Reponse reponse = new Reponse(contenu, date, selectedReclamation.getId());

            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirmation");
            confirmationAlert.setHeaderText("Ajouter une réponse");
            confirmationAlert.setContentText("Voulez-vous vraiment ajouter cette réponse ?\n\n" +
                    "Sujet de la réclamation : " + selectedReclamation.getSujet() + "\n" +
                    "Contenu : " + contenu + "\n" +
                    "Date : " + date);

            DialogPane dialogPane = confirmationAlert.getDialogPane();
            java.net.URL cssUrl = getClass().getResource("/styles.css");
            if (cssUrl == null) {
                System.out.println("Warning: CSS file not found at /tn/esprit/styles/styles.css. Proceeding without styling.");
            } else {
                dialogPane.getStylesheets().add(cssUrl.toExternalForm());
                dialogPane.getStyleClass().add("sweet-alert");
            }

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Connection cnx = MyDataBase.getInstance().getCnx();
                cnx.setAutoCommit(false);
                try {
                    serviceReponse.add(reponse);
                    selectedReclamation.setEtat("Traité");
                    serviceReclamation.update(selectedReclamation);
                    cnx.commit();
                    // Send email notification
                    mailService.sendEmail(
                            STATIC_RECIPIENT_EMAIL,
                            selectedReclamation.getSujet(),
                            selectedReclamation.getSujet(),
                            contenu,
                            date.toString()
                    );

                    showAlert("Succès", "Réponse ajoutée avec la date : " + date.toString() + " ! La réclamation est maintenant marquée comme Traité.");
                    clearFields();
                    loadReclamations();
                } catch (Exception e) {
                    cnx.rollback();
                    throw e;
                } finally {
                    cnx.setAutoCommit(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Problème : " + e.getMessage());
        }
    }

    @FXML
    public void downloadExcel(ActionEvent actionEvent) {
        try {
            String selectedEtat = filterEtatComboBox.getValue();
            List<Reclamation> reclamationList = (selectedEtat == null || selectedEtat.equals("Tous"))
                    ? serviceReclamation.getAll()
                    : serviceReclamation.getByEtat(selectedEtat);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Reclamations and Responses");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Reclamation ID", "Sujet", "Description", "Date", "État", "Response Contenu", "Response Date"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Reclamation reclamation : reclamationList) {
                List<Reponse> responses = serviceReponse.getByReclamationId(reclamation.getId());
                if (responses.isEmpty()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(reclamation.getId());
                    row.createCell(1).setCellValue(reclamation.getSujet());
                    row.createCell(2).setCellValue(reclamation.getDescription());
                    row.createCell(3).setCellValue(reclamation.getDateDebut().toString());
                    row.createCell(4).setCellValue(reclamation.getEtat());
                } else {
                    for (Reponse response : responses) {
                        Row row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(reclamation.getId());
                        row.createCell(1).setCellValue(reclamation.getSujet());
                        row.createCell(2).setCellValue(reclamation.getDescription());
                        row.createCell(3).setCellValue(reclamation.getDateDebut().toString());
                        row.createCell(4).setCellValue(reclamation.getEtat());
                        row.createCell(5).setCellValue(response.getContenu());
                        row.createCell(6).setCellValue(response.getDateReponse().toString());
                    }
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Excel File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
            fileChooser.setInitialFileName("Reclamations_Responses.xlsx");
            File file = fileChooser.showSaveDialog(scrollPane.getScene().getWindow());

            if (file != null) {
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                    showAlert("Succès", "Fichier Excel exporté avec succès à : " + file.getAbsolutePath());
                }
            }

            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Problème lors de l'exportation Excel : " + e.getMessage());
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);

        DialogPane dialogPane = alert.getDialogPane();
        java.net.URL cssUrl = getClass().getResource("/styles.css");
        if (cssUrl == null) {
            System.out.println("Warning: CSS file not found at /tn/esprit/styles/styles.css. Proceeding without styling.");
        } else {
            dialogPane.getStylesheets().add(cssUrl.toExternalForm());
            dialogPane.getStyleClass().add("sweet-alert");
        }

        alert.showAndWait();
    }

    private void clearFields() {
        TAContenu.clear();
        if (selectedCard != null) {
            selectedCard.getStyleClass().remove("selected-card");
        }
        selectedReclamation = null;
        selectedCard = null;
    }
}