package tn.esprit.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FirebaseNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseNotificationService.class);
    private static final String SERVICE_ACCOUNT_PATH = "src/main/resources/serviceAccountKey.json";
    private static Firestore firestore;

    public static synchronized void initialize() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FileInputStream serviceAccount = new FileInputStream(SERVICE_ACCOUNT_PATH);

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            firestore = FirestoreClient.getFirestore();
            logger.info("Firebase initialized successfully with Firestore");
        }
    }

    public static void saveToFirestore(String collection, Map<String, Object> data) {
        try {
            firestore.collection(collection).add(data).get();
            logger.info("Data saved to Firestore in collection: {}", collection);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed to save to Firestore", e);
            Thread.currentThread().interrupt();
        }
    }

    public static String sendNotificationToTopic(String topic, String title, String body)
            throws FirebaseMessagingException {
        Message message = Message.builder()
                .setTopic(topic)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        return FirebaseMessaging.getInstance().send(message);
    }

    public static String sendDataNotificationToTopic(String topic, String title, String body,
                                                     Map<String, String> data) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setTopic(topic)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data)
                .build();

        return FirebaseMessaging.getInstance().send(message);
    }

    public static void sendNotificationWithFirestoreLog(String topic, String title, String message) {
        try {
            // 1. Send FCM notification
            String messageId = sendNotificationToTopic(topic, title, message);

            // 2. Save to Firestore
            Map<String, Object> logData = new HashMap<>();
            logData.put("title", title);
            logData.put("message", message);
            logData.put("timestamp", System.currentTimeMillis());
            logData.put("messageId", messageId);
            logData.put("topic", topic);

            saveToFirestore("notification_logs", logData);
            logger.info("Notification sent and logged to Firestore");

        } catch (FirebaseMessagingException e) {
            logger.error("Failed to send FCM notification", e);
        }
    }
}