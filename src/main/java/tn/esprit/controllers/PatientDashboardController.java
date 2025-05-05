package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.models.Panier;
import tn.esprit.models.Produit;
import tn.esprit.models.User;
import tn.esprit.services.AuthException;
import tn.esprit.services.AuthService;
import tn.esprit.services.ServicePanier;
import tn.esprit.services.ServiceProduit;
import tn.esprit.utils.SceneManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatientDashboardController {
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField telephoneField;
    @FXML
    private TextArea adresseField;
    @FXML
    private Button modifierBtn;
    @FXML
    private Button supprimerBtn;
    @FXML
    private Button deconnexionBtn;
    @FXML
    private Label messageLabel;
    @FXML
    private ImageView profileImage;
    @FXML
    private Button changeImageBtn;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label ageLabel;
    @FXML
    private Label ageValueLabel;
    @FXML
    private Label latitudeLabel;
    @FXML
    private Label longitudeLabel;
    @FXML
    private ComboBox<String> sexeComboBox;
    @FXML
    private Label nomErrorLabel;
    @FXML
    private Label prenomErrorLabel;
    @FXML
    private Label emailErrorLabel;
    @FXML
    private Label telErrorLabel;
    @FXML
    private Label adresseErrorLabel;
    @FXML
    private Label sexeErrorLabel;
    @FXML
    private ListView<String> appointmentsList;
    @FXML
    private Button appointmentsBtn;
    @FXML
    private Button prescriptionsBtn;
    @FXML
    private Button newAppointmentBtn;
    @FXML
    private Button emergencyBtn;
    @FXML
    private TextField ageField;
    @FXML
    private Label ageErrorLabel;
    @FXML
    private FlowPane productFlowPane;

    @FXML
    private Button cartButton;

    @FXML
    private Label cartCountLabel;

    private List<Produit> cartProducts = new ArrayList<>(); // Local cart list
    private int cartItemCount = 0; // Number of items
    private ServiceProduit serviceProduit = new ServiceProduit(); // Your service to fetch products

    private ServicePanier servicePanier = new ServicePanier(); // Your service to manage cart

    private User currentUser;
    private AuthService authService;
    private SceneManager sceneManager;
    private File selectedImageFile;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8,15}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s-]{2,50}$");
    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024; // 2 Mo
    private static final String STRIPE_SECRET_KEY = "sk_test_51RIiu5FL5nArcCfbIkZMXqjjuwAFxqzHlQ1Pol1ew0qtdSHwmWqiVS0qc5l06BLGSjBVgsjpdK6R23vlnpLTuyXa00GBmkkPmi";

    @FXML
    public void initialize() {
        configureButtonActions();
        disableFields();
        initializeComboBoxes();
        clearErrorMessages();
        loadProducts();

    }

    private void initializeComboBoxes() {
        sexeComboBox.getItems().addAll("Homme", "Femme", "Autre");
    }

    private void clearErrorMessages() {
        nomErrorLabel.setText("");
        prenomErrorLabel.setText("");
        emailErrorLabel.setText("");
        telErrorLabel.setText("");
        adresseErrorLabel.setText("");
        sexeErrorLabel.setText("");
        ageErrorLabel.setText("");
        messageLabel.setText("");
    }

    private void configureButtonActions() {
        changeImageBtn.setOnAction(e -> handleChangeImage());
        deconnexionBtn.setOnAction(e -> handleDeconnexion());
        modifierBtn.setOnAction(e -> handleModification());
        supprimerBtn.setOnAction(e -> handleSuppression());
        appointmentsBtn.setOnAction(e -> handleShowAppointments());
        prescriptionsBtn.setOnAction(e -> handleShowPrescriptions());
        newAppointmentBtn.setOnAction(e -> handleNewAppointment());
        emergencyBtn.setOnAction(e -> handleEmergency());
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserData();
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    private void loadUserData() {
        if (currentUser != null) {
            nomField.setText(currentUser.getNom());
            prenomField.setText(currentUser.getPrenom());
            emailField.setText(currentUser.getEmail());
            telephoneField.setText(currentUser.getTelephone());
            adresseField.setText(currentUser.getAdresse());
            ageField.setText(currentUser.getAge() != null ? currentUser.getAge().toString() : "");
            sexeComboBox.setValue(currentUser.getSexe());

            welcomeLabel.setText("Bienvenue " + currentUser.getPrenom() + " " + currentUser.getNom());
            ageLabel.setText("Âge: " + (currentUser.getAge() != null ? currentUser.getAge() + " ans" : "Non spécifié"));
            ageValueLabel.setText(currentUser.getAge() != null ? currentUser.getAge().toString() : "N/A");

            latitudeLabel.setText(currentUser.getLatitude() != null ? String.format("%.4f", currentUser.getLatitude()) : "N/A");
            longitudeLabel.setText(currentUser.getLongitude() != null ? String.format("%.4f", currentUser.getLongitude()) : "N/A");

            loadProfileImage();
            loadUpcomingAppointments();
        }
    }


    private void loadUpcomingAppointments() {
        // TODO: Load actual appointments from service
        appointmentsList.getItems().addAll(
                "15 Nov - 10:00 - Dr. Smith - Cardiologie",
                "20 Nov - 14:30 - Dr. Johnson - Généraliste"
        );
    }

    private void loadProfileImage() {
        try {
            if (currentUser.getImageProfil() != null && !currentUser.getImageProfil().isEmpty()) {
                File file = new File(currentUser.getImageProfil());
                if (file.exists()) {
                    profileImage.setImage(new Image(file.toURI().toString()));
                    return;
                }
            }

            // Charger l'image par défaut
            String defaultImagePath = "/images/default-profil.jpg";
            InputStream is = getClass().getResourceAsStream(defaultImagePath);
            if (is != null) {
                profileImage.setImage(new Image(is));
            } else {
                profileImage.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Erreur de chargement de l'image: " + e.getMessage());
            profileImage.setImage(null);
        }
    }


    @FXML
    private void handleChangeImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        selectedImageFile = fileChooser.showOpenDialog(profileImage.getScene().getWindow());
        if (selectedImageFile != null) {
            try {
                // Vérifier la taille de l'image
                if (selectedImageFile.length() > MAX_IMAGE_SIZE) {
                    showErrorMessage("L'image ne doit pas dépasser 2 Mo");
                    return;
                }

                // Sauvegarder l'image dans le dossier
                Path destination = saveImageToProfileFolder(selectedImageFile);

                // Mettre à jour l'utilisateur et la base de données
                currentUser.setImageProfil(destination.toString());
                authService.updateUser(currentUser);

                // Mettre à jour l'affichage
                profileImage.setImage(new Image(destination.toUri().toString()));

                showSuccessMessage("Photo de profil mise à jour avec succès");
            } catch (IOException e) {
                showErrorMessage("Erreur lors de l'enregistrement de l'image");
                e.printStackTrace();
            } catch (AuthException e) {
                showErrorMessage("Erreur lors de la mise à jour en base de données");
                e.printStackTrace();
            }
        }
    }

    private Path saveImageToProfileFolder(File imageFile) throws IOException {
        Path profileDir = Paths.get("src/main/resources/profile_images");
        if (!Files.exists(profileDir)) {
            Files.createDirectories(profileDir);
        }

        String fileName = "patient_" + currentUser.getId() + "_"
                + System.currentTimeMillis() + getFileExtension(imageFile.getName());
        Path destination = profileDir.resolve(fileName);
        Files.copy(imageFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        return destination;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        String normalizedFileName = fileName.replace('\\', '/');
        int lastDot = normalizedFileName.lastIndexOf('.');
        int lastSeparator = normalizedFileName.lastIndexOf('/');
        if (lastDot == -1 || (lastSeparator != -1 && lastDot < lastSeparator)) {
            return "";
        }
        return normalizedFileName.substring(lastDot).toLowerCase();
    }


    @FXML
    private void handleDeconnexion() {
        sceneManager.showLoginScene();
    }

    @FXML
    private void handleModification() {
        if (modifierBtn.getText().equals("Modifier Profil")) {
            enableFields();
            modifierBtn.setText("Enregistrer");
            clearErrorMessages();
            messageLabel.setText("");
        } else {
            if (saveChanges()) {
                disableFields();
                modifierBtn.setText("Modifier Profil");

            }
        }
    }

    @FXML
    private void handleSuppression() {
        showModernDeleteConfirmation();
    }

    @FXML
    private void handleShowAppointments() {
        showSuccessMessage("Affichage des rendez-vous");
    }

    @FXML
    private void handleShowPrescriptions() {
        showSuccessMessage("Affichage des ordonnances");
    }

    @FXML
    private void handleNewAppointment() {
        showSuccessMessage("Création d'un nouveau rendez-vous");
    }

    @FXML
    private void handleEmergency() {
        showSuccessMessage("Contact d'urgence");
    }

    private void enableFields() {
        setFieldsEditable(true);
        emailField.setEditable(false);
    }

    private void disableFields() {
        setFieldsEditable(false);
    }

    private void setFieldsEditable(boolean editable) {
        nomField.setEditable(editable);
        prenomField.setEditable(editable);
        telephoneField.setEditable(editable);
        adresseField.setEditable(editable);
        ageField.setEditable(editable);
        sexeComboBox.setDisable(!editable);
    }

    private boolean saveChanges() {
        try {
            if (validateFields()) {
                updateUserFromFields();
                authService.updateUser(currentUser);
                loadUserData();
                showSuccessMessage("Profil mis à jour avec succès");
                return true;
            }
            return false;
        } catch (Exception e) {
            showErrorMessage("Erreur lors de la mise à jour: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (AuthException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean validateFields() {
        boolean isValid = true;
        clearErrorMessages();

        if (nomField.getText().isEmpty()) {
            nomErrorLabel.setText("Le nom est obligatoire");
            isValid = false;
        } else if (!NAME_PATTERN.matcher(nomField.getText()).matches()) {
            nomErrorLabel.setText("Nom invalide (2-50 caractères alphabétiques)");
            isValid = false;
        }

        if (prenomField.getText().isEmpty()) {
            prenomErrorLabel.setText("Le prénom est obligatoire");
            isValid = false;
        } else if (!NAME_PATTERN.matcher(prenomField.getText()).matches()) {
            prenomErrorLabel.setText("Prénom invalide (2-50 caractères alphabétiques)");
            isValid = false;
        }

        if (emailField.getText().isEmpty()) {
            emailErrorLabel.setText("L'email est obligatoire");
            isValid = false;
        } else if (!EMAIL_PATTERN.matcher(emailField.getText()).matches()) {
            emailErrorLabel.setText("Email invalide");
            isValid = false;
        }

        if (telephoneField.getText().isEmpty()) {
            telErrorLabel.setText("Le téléphone est obligatoire");
            isValid = false;
        } else if (!PHONE_PATTERN.matcher(telephoneField.getText()).matches()) {
            telErrorLabel.setText("Téléphone invalide (8-15 chiffres)");
            isValid = false;
        }

        if (adresseField.getText().isEmpty()) {
            adresseErrorLabel.setText("L'adresse est obligatoire");
            isValid = false;
        }
        if (ageField.getText().isEmpty()) {
            ageErrorLabel.setText("L'âge est obligatoire");
            isValid = false;
        } else {
            try {
                int age = Integer.parseInt(ageField.getText());
                if (age < 0 || age > 120) {
                    ageErrorLabel.setText("Âge invalide (0-120 ans)");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                ageErrorLabel.setText("L'âge doit être un nombre");
                isValid = false;
            }
        }
        if (sexeComboBox.getValue() == null || sexeComboBox.getValue().isEmpty()) {
            sexeErrorLabel.setText("Veuillez sélectionner un sexe");
            isValid = false;
        }

        return isValid;
    }

    private void updateUserFromFields() {
        currentUser.setNom(nomField.getText().trim());
        currentUser.setPrenom(prenomField.getText().trim());
        currentUser.setTelephone(telephoneField.getText().trim());
        currentUser.setAdresse(adresseField.getText().trim());
        if (!ageField.getText().isEmpty()) {
            currentUser.setAge(Integer.parseInt(ageField.getText()));
        } else {
            currentUser.setAge(null);
        }
        currentUser.setSexe(sexeComboBox.getValue());
    }

    private void showModernDeleteConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Suppression du compte");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer définitivement votre compte ? Cette action est irréversible.");

        // Customize buttons
        ButtonType confirmButton = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        // Apply modern styling
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/patient.css").toExternalForm());
        dialogPane.getStyleClass().add("confirmation-dialog");

        // Show and wait for response
        alert.showAndWait().ifPresent(response -> {
            if (response == confirmButton) {
                try {
                    authService.deleteUser(currentUser.getId());
                    sceneManager.showLoginScene();
                    showSuccessMessage("Compte supprimé avec succès");
                } catch (Exception e) {
                    showErrorMessage("Échec de la suppression: " + e.getMessage());
                    e.printStackTrace();
                } catch (AuthException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void loadProducts() {
        try {
            List<Produit> produits = serviceProduit.getAll();

            for (Produit produit : produits) {
                VBox productCard = createProductCard(produit);
                productFlowPane.getChildren().add(productCard);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createProductCard(Produit produit) {
        VBox card = new VBox(10);
        card.setPrefSize(150, 220);
        card.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: #ccc; -fx-border-radius: 8; -fx-background-radius: 8;");
        card.setAlignment(javafx.geometry.Pos.CENTER);

        // Product Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);

        try {
            if (produit.getImage() != null && !produit.getImage().isEmpty()) {
                File imgFile = new File("uploaded_images/" + produit.getImage());
                if (imgFile.exists()) {
                    imageView.setImage(new Image(imgFile.toURI().toString()));
                } else {
                    System.out.println("Image not found: " + imgFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Label nameLabel = new Label(produit.getNom());
        nameLabel.setStyle("-fx-font-weight: bold;");

        Label priceLabel = new Label(String.format("%.2f DT", produit.getPrix()));

        Button addToCartBtn = new Button("Ajouter au panier");
        addToCartBtn.getStyleClass().add("primary-btn");

        addToCartBtn.setOnAction(e -> {
            addToCart(produit);
        });

        card.getChildren().addAll(imageView, nameLabel, priceLabel, addToCartBtn);
        return card;
    }


    private void addToCart(Produit produit) {
        try {
            if (currentUser == null) {
                showErrorMessage("Utilisateur non connecté");
                return;
            }

            Panier panier = servicePanier.getOrCreatePanier(currentUser.getId());
            servicePanier.addProduitToPanier(panier.getId(), produit.getId());

            // Local cart update
            cartProducts.add(produit);
            cartItemCount++;
            updateCartCount();

            showSuccessMessage(produit.getNom() + " ajouté au panier !");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Erreur lors de l'ajout au panier");
        }
    }

    private void updateCartCount() {
        cartCountLabel.setText(String.valueOf(cartItemCount));
    }

    @FXML
    private void handleOpenCart() {
        if (cartProducts.isEmpty()) {
            showErrorMessage("Votre panier est vide.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Votre Panier");

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20; -fx-background-color: white;");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);

        VBox productList = new VBox(10);
        updateProductList(productList, content);

        Label totalLabel = new Label();
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button checkoutButton = new Button("Passer au Paiement");
        checkoutButton.setStyle("-fx-background-color: #6772E5; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-size: 14px; -fx-background-radius: 8px;");

        checkoutButton.setOnAction(event -> {
            double finalTotal = cartProducts.stream().mapToDouble(Produit::getPrix).sum();
            launchStripeCheckout(finalTotal);
            dialog.close();
        });

        VBox mainContent = new VBox(20, productList, totalLabel, checkoutButton);
        content.getChildren().add(mainContent);

        updateTotalLabel(totalLabel);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.show();
    }

    // Helper to update the products list in the cart
    private void updateProductList(VBox productList, VBox content) {
        productList.getChildren().clear();
        for (Produit produit : cartProducts) {
            HBox productRow = new HBox(10);
            productRow.setStyle("-fx-alignment: center-left;");

            Label label = new Label(produit.getNom() + " - " + String.format("%.2f DT", produit.getPrix()));
            label.setStyle("-fx-font-size: 14px; -fx-pref-width: 300px;");

            Button deleteBtn = new Button("✖");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-size: 14px; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> {
                cartProducts.remove(produit);
                cartItemCount--;
                updateCartCount();
                updateProductList(productList, content); // 🔥 refresh instantly
                updateTotalLabel((Label) ((VBox) content.getChildren().get(0)).getChildren().get(1));
                if (cartProducts.isEmpty()) {
                    ((Stage) content.getScene().getWindow()).close();
                    showErrorMessage("Votre panier est vide.");
                }
            });

            productRow.getChildren().addAll(label, deleteBtn);
            productList.getChildren().add(productRow);
        }
    }

    // Helper to update the total price
    private void updateTotalLabel(Label totalLabel) {
        double total = cartProducts.stream().mapToDouble(Produit::getPrix).sum();
        totalLabel.setText("Total: " + String.format("%.2f DT", total));
    }


    private void launchStripeCheckout(double amount) {
        try {
            java.net.URL url = new java.net.URL("https://api.stripe.com/v1/checkout/sessions");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + STRIPE_SECRET_KEY);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            String postData =
                    "success_url=" + java.net.URLEncoder.encode("https://success.stripe.com", "UTF-8") +
                            "&cancel_url=" + java.net.URLEncoder.encode("https://cancel.stripe.com", "UTF-8") +
                            "&mode=payment" +
                            "&line_items[0][price_data][currency]=usd" +
                            "&line_items[0][price_data][product_data][name]=" + java.net.URLEncoder.encode("Paiement Panier", "UTF-8") +
                            "&line_items[0][price_data][unit_amount]=" + ((int) (amount * 100)) +
                            "&line_items[0][quantity]=1";

            conn.getOutputStream().write(postData.getBytes());

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                java.io.InputStream is = conn.getInputStream();
                java.util.Scanner scanner = new java.util.Scanner(is, "UTF-8");
                String jsonResponse = scanner.useDelimiter("\\A").next();
                scanner.close();

                String checkoutUrl = extractUrlFromJson(jsonResponse);

                if (checkoutUrl != null) {
                    openStripeWebView(checkoutUrl);
                } else {
                    System.out.println("Stripe success but no URL found.");
                    System.out.println("Response: " + jsonResponse);
                }
            } else {
                System.out.println("Erreur Stripe: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String extractUrlFromJson(String json) {
        Pattern pattern = Pattern.compile("\"url\"\\s*:\\s*\"(.*?)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1).replace("\\u0026", "&");
        }
        return null;
    }

    private void openStripeWebView(String checkoutUrl) {
        Platform.runLater(() -> {
            Stage stage = new Stage();
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();

            webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.contains("success.stripe.com")) {
                    stage.close();
                    clearCart(); // 🧹 clear cart after success
                    showSuccessMessage("✅ Merci pour votre achat !");
                } else if (newValue.contains("cancel.stripe.com")) {
                    stage.close();
                    showErrorMessage("❌ Paiement annulé.");
                }
            });

            webEngine.load(checkoutUrl);

            Scene scene = new Scene(webView, 800, 600);
            stage.setTitle("Paiement Stripe");
            stage.setScene(scene);
            stage.show();
        });
    }

    private void clearCart() {
        cartProducts.clear();
        cartItemCount = 0;
        updateCartCount();
    }


    private void showSuccessMessage(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().remove("error");
        messageLabel.getStyleClass().add("success");

        // Masquer le message après 3 secondes
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> {
                    messageLabel.setText("");
                    messageLabel.getStyleClass().remove("success");
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showErrorMessage(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().remove("success");
        messageLabel.getStyleClass().add("error");
    }
}