package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import tn.esprit.models.Reclamation;
import tn.esprit.models.Reponse;
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
    private ListView<Reclamation> listViewReclamations;

    @FXML
    private TextField TFReclamationId, TFReclamationSujet;

    @FXML
    private TextArea TAContenu;

    @FXML
    private ComboBox<String> filterEtatComboBox;

    private final ServiceReclamation serviceReclamation = new ServiceReclamation();
    private final ServiceReponse serviceReponse = new ServiceReponse();
    private ObservableList<Reclamation> observableReclamations;

    @FXML
    public void initialize() {
        // Initialize ComboBox with filter options
        filterEtatComboBox.setItems(FXCollections.observableArrayList("Tous", "En Attente", "Traité"));
        filterEtatComboBox.setValue("Tous");
        filterEtatComboBox.setOnAction(event -> loadReclamations());

        loadReclamations();

        listViewReclamations.setCellFactory(listView -> new ListCell<Reclamation>() {
            @Override
            protected void updateItem(Reclamation reclamation, boolean empty) {
                super.updateItem(reclamation, empty);
                if (empty || reclamation == null) {
                    setText(null);
                } else {
                    setText("Sujet: " + reclamation.getSujet() + " | Date: " + reclamation.getDateDebut() + " | État: " + reclamation.getEtat());
                }
            }
        });

        // Update form fields when a reclamation is selected
        listViewReclamations.setOnMouseClicked(event -> {
            Reclamation selected = listViewReclamations.getSelectionModel().getSelectedItem();
            if (selected != null) {
                TFReclamationId.setText(String.valueOf(selected.getId()));
                TFReclamationSujet.setText(selected.getSujet());
            }
        });
    }

    private void loadReclamations() {
        String selectedEtat = filterEtatComboBox.getValue();
        List<Reclamation> reclamationList;

        if (selectedEtat == null || selectedEtat.equals("Tous")) {
            reclamationList = serviceReclamation.getAll();
        } else {
            reclamationList = serviceReclamation.getByEtat(selectedEtat);
        }

        observableReclamations = FXCollections.observableArrayList(reclamationList);
        listViewReclamations.setItems(observableReclamations);
    }

    @FXML
    public void Save(ActionEvent actionEvent) {
        try {
            String reclamationIdText = TFReclamationId.getText();
            String contenu = TAContenu.getText();
            LocalDate date = LocalDate.now();

            if (reclamationIdText.isEmpty()) {
                showAlert("Erreur", "Veuillez sélectionner une réclamation !");
                return;
            }
            if (contenu.isEmpty()) {
                showAlert("Erreur", "Le contenu de la réponse ne peut pas être vide !");
                return;
            }

            int reclamationId = Integer.parseInt(reclamationIdText);
            Reclamation selectedReclamation = serviceReclamation.getById(reclamationId);
            if (selectedReclamation == null) {
                showAlert("Erreur", "Réclamation non trouvée !");
                return;
            }

            System.out.println("Date before creating Reponse: " + date);
            Reponse reponse = new Reponse(contenu, date, reclamationId);
            System.out.println("Reponse dateReponse after creation: " + reponse.getDateReponse());
            System.out.println("Reponse reclamationId after creation: " + reponse.getReclamationId());

            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirmation");
            confirmationAlert.setHeaderText("Ajouter une réponse");
            confirmationAlert.setContentText("Voulez-vous vraiment ajouter cette réponse ?\n\n" +
                    "Sujet de la réclamation : " + selectedReclamation.getSujet() + "\n" +
                    "Contenu : " + contenu + "\n" +
                    "Date : " + date);

            DialogPane dialogPane = confirmationAlert.getDialogPane();
            System.out.println("Loading stylesheet for confirmation dialog...");
            java.net.URL cssUrl = getClass().getResource("/tn/esprit/styles/styles.css");
            if (cssUrl == null) {
                System.out.println("Error: CSS file not found at /tn/esprit/styles/styles.css");
            } else {
                System.out.println("CSS file found: " + cssUrl.toExternalForm());
                dialogPane.getStylesheets().add(cssUrl.toExternalForm());
                dialogPane.getStyleClass().add("sweet-alert");
            }

            System.out.println("Showing confirmation dialog...");
            Optional<ButtonType> result = confirmationAlert.showAndWait();
            System.out.println("Confirmation dialog result: " + result);

            if (result.isPresent() && result.get() == ButtonType.OK) {
                Connection cnx = MyDataBase.getInstance().getCnx();
                cnx.setAutoCommit(false);
                try {
                    System.out.println("User confirmed, adding response...");
                    serviceReponse.add(reponse);
                    System.out.println("Response added, updating reclamation status...");

                    selectedReclamation.setEtat("Traité");
                    serviceReclamation.update(selectedReclamation);
                    System.out.println("Reclamation status updated to Traité");

                    cnx.commit();
                    System.out.println("Transaction committed successfully");

                    System.out.println("Response added, showing success alert...");
                    showAlert("Succès", "Réponse ajoutée avec la date : " + date.toString() + " ! La réclamation est maintenant marquée comme Traité.");
                    clearFields();
                    loadReclamations();
                } catch (Exception e) {
                    cnx.rollback();
                    System.out.println("Transaction rolled back due to error: " + e.getMessage());
                    throw e;
                } finally {
                    cnx.setAutoCommit(true);
                }
            } else {
                System.out.println("Ajout de la réponse annulé.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Problème : " + e.getMessage());
        }
    }

    @FXML
    public void downloadExcel(ActionEvent actionEvent) {
        try {
            // Fetch all reclamations based on current filter
            String selectedEtat = filterEtatComboBox.getValue();
            List<Reclamation> reclamationList = (selectedEtat == null || selectedEtat.equals("Tous"))
                    ? serviceReclamation.getAll()
                    : serviceReclamation.getByEtat(selectedEtat);

            // Create Excel workbook and sheet
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Reclamations and Responses");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Reclamation ID", "Sujet", "Description", "Date", "État", "Response Contenu", "Response Date"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                // Optional: Style header
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            int rowNum = 1;
            for (Reclamation reclamation : reclamationList) {
                List<Reponse> responses = serviceReponse.getByReclamationId(reclamation.getId());
                if (responses.isEmpty()) {
                    // Reclamation without responses
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(reclamation.getId());
                    row.createCell(1).setCellValue(reclamation.getSujet());
                    row.createCell(2).setCellValue(reclamation.getDescription());
                    row.createCell(3).setCellValue(reclamation.getDateDebut().toString());
                    row.createCell(4).setCellValue(reclamation.getEtat());
                } else {
                    // Reclamation with one or more responses
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

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Open file chooser to save the Excel file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Excel File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
            fileChooser.setInitialFileName("Reclamations_Responses.xlsx");
            File file = fileChooser.showSaveDialog(listViewReclamations.getScene().getWindow());

            if (file != null) {
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                    showAlert("Succès", "Fichier Excel exporté avec succès à : " + file.getAbsolutePath());
                }
            } else {
                System.out.println("Exportation annulée.");
            }

            // Close workbook
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
        System.out.println("Loading stylesheet for alert...");
        java.net.URL cssUrl = getClass().getResource("/tn/esprit/styles/styles.css");
        if (cssUrl == null) {
            System.out.println("Error: CSS file not found at /tn/esprit/styles/styles.css");
        } else {
            System.out.println("CSS file found: " + cssUrl.toExternalForm());
            dialogPane.getStylesheets().add(cssUrl.toExternalForm());
            dialogPane.getStyleClass().add("sweet-alert");
        }

        alert.showAndWait();
    }

    private void clearFields() {
        TFReclamationId.clear();
        TFReclamationSujet.clear();
        TAContenu.clear();
        listViewReclamations.getSelectionModel().clearSelection();
    }
}