package tn.esprit.services;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationHistoryService {
    private static final String HISTORY_FILE = "notification_history.json";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void addNotification(int patientId, String action, String details) {
        try {
            // Lire le fichier existant
            JSONArray historyArray = readHistoryFile();

            // Créer la nouvelle notification
            JSONObject notification = new JSONObject();
            notification.put("patientId", patientId);
            notification.put("action", action);
            notification.put("details", details);
            notification.put("timestamp", LocalDateTime.now().format(formatter));

            historyArray.put(notification);

            Files.write(Paths.get(HISTORY_FILE), historyArray.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getPatientNotifications(int patientId) {
        List<String> notifications = new ArrayList<>();
        try {
            JSONArray historyArray = readHistoryFile();
            for (int i = 0; i < historyArray.length(); i++) {
                JSONObject obj = historyArray.getJSONObject(i);
                if (obj.getInt("patientId") == patientId) {
                    String notif = String.format("[%s] %s - %s",
                            obj.getString("timestamp"),
                            obj.getString("action"),
                            obj.getString("details"));
                    notifications.add(notif);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    private static JSONArray readHistoryFile() throws IOException {
        File file = new File(HISTORY_FILE);
        if (!file.exists()) {
            return new JSONArray();
        }
        String content = new String(Files.readAllBytes(Paths.get(HISTORY_FILE)));
        return new JSONArray(content);
    }
}