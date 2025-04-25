package tn.esprit.controllers;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.InputStream;

public class FirebaseInitializer {

    private static boolean isInitialized = false;

    public static void initialize() {
        if (isInitialized) {
            return;
        }

        try {
            InputStream serviceAccount = FirebaseInitializer.class.getClassLoader()
                    .getResourceAsStream("consultationapp-firebase-adminsdk.json");

            if (serviceAccount == null) {
                throw new IllegalStateException("Firebase service account JSON file not found in resources.");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            isInitialized = true;
            System.out.println("Firebase initialized successfully.");
        } catch (Exception e) {
            System.err.println("Error initializing Firebase: " + e.getMessage());
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }
}