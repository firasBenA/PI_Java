package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import tn.esprit.models.Produit;
import tn.esprit.services.ServiceProduit;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Timestamp;
import java.util.List;

public class ProduitController {

    @FXML private FlowPane flowPane;
    @FXML private TextField tfNom, tfPrix, tfStock, tfImage;
    @FXML private TextArea taDescription;
    @FXML private ScrollPane scrollPane;

    private final ServiceProduit serviceProduit = new ServiceProduit();
    private File selectedImageFile;
    private Produit selectedProduit;
    private static final Path IMAGE_DIRECTORY = Paths.get("uploaded_images");

    @FXML
    public void initialize() {
        try {
            if (!Files.exists(IMAGE_DIRECTORY)) Files.createDirectories(IMAGE_DIRECTORY);
        } catch (IOException e) {
            e.printStackTrace();
        }
        refreshCards();
    }

    private void refreshCards() {
        flowPane.getChildren().clear();
        List<Produit> produits = serviceProduit.getAll();

        for (Produit produit : produits) {
            VBox card = createProduitCard(produit);
            flowPane.getChildren().add(card);
        }
    }

    private VBox createProduitCard(Produit produit) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-border-color: gray; -fx-border-radius: 10; -fx-background-radius: 10; -fx-background-color: #f9f9f9;");
        card.setPrefWidth(160);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(140);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);

        File imageFile = new File("uploaded_images/" + produit.getImage());
        if (imageFile.exists()) {
            imageView.setImage(new Image(imageFile.toURI().toString()));
        }

        Label nameLabel = new Label("Nom: " + produit.getNom());
        Label priceLabel = new Label("Prix: " + produit.getPrix() + " DT");
        Label stockLabel = new Label("Stock: " + produit.getStock());
        Label descLabel = new Label("Desc: " + produit.getDescription());
        descLabel.setWrapText(true);

        Button selectBtn = new Button("Select");
        selectBtn.setOnAction(e -> {
            selectedProduit = produit;
            tfNom.setText(produit.getNom());
            tfPrix.setText(String.valueOf(produit.getPrix()));
            tfStock.setText(String.valueOf(produit.getStock()));
            tfImage.setText(produit.getImage());
            taDescription.setText(produit.getDescription());
            selectedImageFile = null;
        });

        card.getChildren().addAll(imageView, nameLabel, priceLabel, stockLabel, descLabel, selectBtn);
        return card;
    }
    @FXML
    private void addProduit() {
        try {
            if (!validateFields()) return;

            String savedImageName = saveImage(selectedImageFile);
            String nom = tfNom.getText().trim();
            String desc = taDescription.getText().trim();
            double prix = Double.parseDouble(tfPrix.getText().trim());
            int stock = Integer.parseInt(tfStock.getText().trim());

            Produit produit = new Produit(nom, desc, prix, stock, savedImageName);
            serviceProduit.add(produit);

            clearFields();
            selectedImageFile = null;
            refreshCards();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ajout du produit.");
        }
    }

    @FXML
    private void updateProduit() {
        if (selectedProduit != null) {
            try {
                if (!validateFields()) return;

                selectedProduit.setNom(tfNom.getText().trim());
                selectedProduit.setPrix(Double.parseDouble(tfPrix.getText().trim()));
                selectedProduit.setStock(Integer.parseInt(tfStock.getText().trim()));
                selectedProduit.setDescription(taDescription.getText().trim());
                selectedProduit.setMajLe(new Timestamp(System.currentTimeMillis()));

                if (selectedImageFile != null) {
                    String savedImageName = saveImage(selectedImageFile);
                    selectedProduit.setImage(savedImageName);
                }

                serviceProduit.update(selectedProduit);
                clearFields();
                selectedImageFile = null;
                selectedProduit = null;
                refreshCards();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de la mise à jour du produit.");
            }
        }
    }

    @FXML
    private void deleteProduit() {
        if (selectedProduit != null) {
            try {
                serviceProduit.delete(selectedProduit);
                clearFields();
                selectedImageFile = null;
                selectedProduit = null;
                refreshCards();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de la suppression.");
            }
        }
    }

    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(tfImage.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            tfImage.setText(file.getName());
        }
    }

    private boolean validateFields() {
        String nom = tfNom.getText().trim();
        String prixText = tfPrix.getText().trim();
        String stockText = tfStock.getText().trim();
        String desc = taDescription.getText().trim();

        if (nom.length() < 2) {
            showAlert("Validation", "Nom doit contenir au moins 2 caractères.");
            return false;
        }

        if (desc.length() < 5) {
            showAlert("Validation", "Description doit contenir au moins 5 caractères.");
            return false;
        }

        try {
            double prix = Double.parseDouble(prixText);
            if (prix <= 0) {
                showAlert("Validation", "Prix doit être supérieur à 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation", "Prix invalide.");
            return false;
        }

        try {
            int stock = Integer.parseInt(stockText);
            if (stock < 0) {
                showAlert("Validation", "Stock ne peut pas être négatif.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation", "Stock invalide.");
            return false;
        }

        if (selectedImageFile == null && tfImage.getText().isEmpty()) {
            showAlert("Validation", "Veuillez sélectionner une image.");
            return false;
        }

        return true;
    }

    private void clearFields() {
        tfNom.clear();
        tfPrix.clear();
        tfStock.clear();
        tfImage.clear();
        taDescription.clear();
        selectedProduit = null;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String saveImage(File imageFile) throws IOException {
        File uploadDir = new File("uploaded_images");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String uniqueName = System.currentTimeMillis() + "_" + imageFile.getName();
        File destinationFile = new File(uploadDir, uniqueName);
        Files.copy(imageFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return uniqueName;
    }

    @FXML
    private void handleDownloadPdf() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
            File file = fileChooser.showSaveDialog(tfNom.getScene().getWindow());

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

        for (Produit produit : serviceProduit.getAll()) {
            document.add(new Paragraph("Nom: " + produit.getNom(), normalFont));
            document.add(new Paragraph("Prix: " + produit.getPrix() + " DT", normalFont));
            document.add(new Paragraph("Stock: " + produit.getStock(), normalFont));
            document.add(new Paragraph("Description: " + produit.getDescription(), normalFont));
            File imageFile = new File("uploaded_images/" + produit.getImage());
            if (imageFile.exists()) {
                com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(imageFile.getAbsolutePath());
                img.scaleToFit(100, 100);
                document.add(img);
            }
            document.add(new Paragraph("\n--------------------------------\n"));
        }

        document.close();
    }
}