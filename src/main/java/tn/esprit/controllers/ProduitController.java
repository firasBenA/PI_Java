package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import tn.esprit.models.Produit;
import tn.esprit.services.ServiceProduit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class ProduitController {

    @FXML
    private TableView<Produit> tableProduit;
    @FXML
    private TableColumn<Produit, Integer> colId;
    @FXML
    private TableColumn<Produit, String> colName;
    @FXML
    private TableColumn<Produit, Double> colPrice;
    @FXML
    private TableColumn<Produit, Integer> colStock;
    @FXML
    private TableColumn<Produit, String> colImage;
    @FXML
    private TableColumn<Produit, String> colDescription;

    @FXML
    private TextField tfNom;
    @FXML
    private TextField tfPrix;
    @FXML
    private TextField tfStock;
    @FXML
    private TextField tfImage;
    @FXML
    private TextArea taDescription;

    private final ServiceProduit serviceProduit = new ServiceProduit();
    private File selectedImageFile; // for keeping the selected real image

    private static final Path IMAGE_DIRECTORY = Paths.get("uploaded_images");

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNom()));
        colPrice.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrix()).asObject());
        colStock.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getStock()).asObject());
        colImage.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getImage())
        );

// Custom CellFactory to show image thumbnail
        colImage.setCellFactory(column -> {
            return new TableCell<Produit, String>() {
                private final ImageView imageView = new ImageView();

                {
                    imageView.setFitWidth(80);
                    imageView.setFitHeight(80);
                    imageView.setPreserveRatio(true);
                }

                @Override
                protected void updateItem(String imageName, boolean empty) {
                    super.updateItem(imageName, empty);
                    if (empty || imageName == null || imageName.isEmpty()) {
                        setGraphic(null);
                    } else {
                        File imageFile = new File("uploaded_images/" + imageName);
                        if (imageFile.exists()) {
                            imageView.setImage(new javafx.scene.image.Image(imageFile.toURI().toString()));
                            setGraphic(imageView);
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            };
        });

        colDescription.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));

        // Create directory if not exists
        try {
            if (!Files.exists(IMAGE_DIRECTORY)) {
                Files.createDirectories(IMAGE_DIRECTORY);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        tableProduit.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                tfNom.setText(newValue.getNom());
                tfPrix.setText(String.valueOf(newValue.getPrix()));
                tfStock.setText(String.valueOf(newValue.getStock()));
                tfImage.setText(newValue.getImage()); // just showing image filename
                taDescription.setText(newValue.getDescription());

                // Important: reset selectedImageFile to null (because you didn't choose a new file yet)
                selectedImageFile = null;
            }
        });

        refreshTable();
    }

    private void refreshTable() {
        ObservableList<Produit> list = FXCollections.observableArrayList(serviceProduit.getAll());
        tableProduit.setItems(list);
    }

    @FXML
    private void addProduit() {
        try {
            if (!validateFields()) {
                return; // If not valid, stop here.
            }

            String savedImageName = saveImage(selectedImageFile); // Save and get image name

            String nom = tfNom.getText().trim();
            String desc = taDescription.getText().trim();
            double prix = Double.parseDouble(tfPrix.getText().trim());
            int stock = Integer.parseInt(tfStock.getText().trim());

            Produit produit = new Produit(nom, desc, prix, stock, savedImageName);
            serviceProduit.add(produit);
            refreshTable();
            clearFields();
            selectedImageFile = null;
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ajout du produit.");
        }
    }

    @FXML
    private void updateProduit() {
        Produit selected = tableProduit.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                if (!validateFields()) {
                    return; // If not valid, stop here
                }

                selected.setNom(tfNom.getText().trim());
                selected.setPrix(Double.parseDouble(tfPrix.getText().trim()));
                selected.setStock(Integer.parseInt(tfStock.getText().trim()));

                if (selectedImageFile != null) {
                    String savedImageName = saveImage(selectedImageFile);
                    selected.setImage(savedImageName);
                }

                selected.setDescription(taDescription.getText().trim());
                selected.setMajLe(new Timestamp(System.currentTimeMillis()));

                serviceProduit.update(selected);
                refreshTable();
                clearFields();
                selectedImageFile = null;
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de la mise à jour du produit.");
            }
        }
    }

    private boolean validateFields() {
        String nom = tfNom.getText().trim();
        String prixText = tfPrix.getText().trim();
        String stockText = tfStock.getText().trim();
        String desc = taDescription.getText().trim();

        if (nom.isEmpty() || nom.length() < 2) {
            showAlert("Validation Erreur", "Le nom du produit doit contenir au moins 2 caractères.");
            return false;
        }

        if (desc.isEmpty() || desc.length() < 5) {
            showAlert("Validation Erreur", "La description du produit doit contenir au moins 5 caractères.");
            return false;
        }

        if (prixText.isEmpty()) {
            showAlert("Validation Erreur", "Le prix est obligatoire.");
            return false;
        }

        try {
            double prix = Double.parseDouble(prixText);
            if (prix <= 0) {
                showAlert("Validation Erreur", "Le prix doit être supérieur à 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Erreur", "Le prix doit être un nombre valide.");
            return false;
        }

        if (stockText.isEmpty()) {
            showAlert("Validation Erreur", "Le stock est obligatoire.");
            return false;
        }

        try {
            int stock = Integer.parseInt(stockText);
            if (stock < 0) {
                showAlert("Validation Erreur", "Le stock ne peut pas être négatif.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Erreur", "Le stock doit être un nombre entier valide.");
            return false;
        }

        if (selectedImageFile == null && tfImage.getText().isEmpty()) {
            showAlert("Validation Erreur", "Veuillez sélectionner une image pour le produit.");
            return false;
        }

        return true;
    }


    @FXML
    private void deleteProduit() {
        Produit selected = tableProduit.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                serviceProduit.delete(selected);
                refreshTable();
                clearFields();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(tfImage.getScene().getWindow());

        if (file != null) {
            selectedImageFile = file;
            tfImage.setText(file.getName()); // just show the filename in the textfield
        }
    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFields() {
        tfNom.clear();
        tfPrix.clear();
        tfStock.clear();
        tfImage.clear();
        taDescription.clear();
    }

    private String saveImage(File imageFile) throws IOException {
        File uploadDir = new File("uploaded_images");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String uniqueName = System.currentTimeMillis() + "_" + imageFile.getName();
        File destinationFile = new File(uploadDir, uniqueName);

        Files.copy(imageFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return uniqueName; // return only the filename, to save in DB
    }

    @FXML
    private void handleDownloadPdf() {
        try {
            // Select where to save the PDF
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
            File file = fileChooser.showSaveDialog(tableProduit.getScene().getWindow());

            if (file != null) {
                generatePdf(file.getAbsolutePath());
                showAlert("Succès", "Le PDF a été généré avec succès !");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la génération du PDF : " + e.getMessage());
        }
    }

    private void generatePdf(String filePath) throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

        document.add(new Paragraph("Liste des Produits", titleFont));
        document.add(new Paragraph("\n"));

        for (Produit produit : tableProduit.getItems()) {
            document.add(new Paragraph("Nom: " + produit.getNom(), normalFont));
            document.add(new Paragraph("Prix: " + produit.getPrix() + " DT", normalFont));
            document.add(new Paragraph("Stock: " + produit.getStock(), normalFont));
            document.add(new Paragraph("Description: " + produit.getDescription(), normalFont));

            if (produit.getImage() != null && !produit.getImage().isEmpty()) {
                File imageFile = new File("uploaded_images/" + produit.getImage());
                if (imageFile.exists()) {
                    Image img = Image.getInstance(imageFile.getAbsolutePath());
                    img.scaleToFit(100, 100);
                    document.add(img);
                }
            }
            document.add(new Paragraph("\n--------------------------------\n"));
        }

        document.close();
    }

}
