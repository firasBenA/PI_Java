package tn.esprit.services;

import java.io.*;
import java.util.Properties;

public class SessionManager {
    private static final String SESSION_FILE = System.getProperty("user.home") + File.separator + "session.properties";
    private static final String KEY_USER_EMAIL = "user.email";
    private static final String KEY_LOGIN_TIMESTAMP = "login.timestamp";

    public static void saveSession(String email) {
        Properties props = new Properties();
        props.setProperty(KEY_USER_EMAIL, email);
        props.setProperty(KEY_LOGIN_TIMESTAMP, String.valueOf(System.currentTimeMillis()));

        try (FileOutputStream fos = new FileOutputStream(SESSION_FILE)) {
            props.store(fos, "User Session");
            System.out.println("Session saved for user: " + email);
        } catch (IOException e) {
            System.err.println("Failed to save session: " + e.getMessage());
        }
    }

    // Load the session and return the email of the logged-in user, if any
    public static String loadSession() {
        Properties props = new Properties();
        File sessionFile = new File(SESSION_FILE);

        if (!sessionFile.exists()) {
            return null;
        }

        try (FileInputStream fis = new FileInputStream(sessionFile)) {
            props.load(fis);
            String email = props.getProperty(KEY_USER_EMAIL);
            String timestamp = props.getProperty(KEY_LOGIN_TIMESTAMP);

            if (email == null || timestamp == null) {
                return null;
            }

            // Optional: Validate session age (e.g., expire after 7 days)
            long loginTime = Long.parseLong(timestamp);
            long currentTime = System.currentTimeMillis();
            long sevenDaysInMillis = 7L * 24 * 60 * 60 * 1000; // 7 days
            if (currentTime - loginTime > sevenDaysInMillis) {
                clearSession();
                return null;
            }

            System.out.println("Session loaded for user: " + email);
            return email;
        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load session: " + e.getMessage());
            return null;
        }
    }

    // Clear the session on logout
    public static void clearSession() {
        File sessionFile = new File(SESSION_FILE);
        if (sessionFile.exists()) {
            if (sessionFile.delete()) {
                System.out.println("Session cleared successfully");
            } else {
                System.err.println("Failed to clear session file");
            }
        }
    }
}