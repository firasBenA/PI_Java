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

public class ListProduit {
    private List<Produit> cartProducts = new ArrayList<>(); // Local cart list
    private int cartItemCount = 0; // Number of items
    private ServiceProduit serviceProduit = new ServiceProduit(); // Your service to fetch products

    private ServicePanier servicePanier = new ServicePanier(); // Your service to manage cart
    @FXML
    private FlowPane productFlowPane;
    @FXML
    private Label messageLabel;
    @FXML
    private Label cartCountLabel;
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

        loadProducts();

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

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
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
