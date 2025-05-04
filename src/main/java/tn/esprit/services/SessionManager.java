package tn.esprit.services;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class SessionManager {
    private static final String DEFAULT_SESSION_FILE = System.getProperty("user.home") + File.separator + "session.properties";
    private static final String FALLBACK_SESSION_FILE = "src/main/resources/sessions/session.properties";
    private static final String KEY_USER_EMAIL = "user.email";
    private static final String KEY_LOGIN_TIMESTAMP = "login.timestamp";
    private static final long SESSION_TIMEOUT = 7L * 24 * 60 * 60 * 1000; // 7 days in milliseconds

    public static void saveSession(String email) {
        File sessionFile = new File(DEFAULT_SESSION_FILE);
        System.out.println("Attempting to save session for user: " + email + " at " + sessionFile.getAbsolutePath());
        System.out.println("User home: " + System.getProperty("user.home"));
        System.out.println("Parent directory writable: " + (sessionFile.getParentFile() != null && sessionFile.getParentFile().canWrite()));

        Properties props = new Properties();
        props.setProperty(KEY_USER_EMAIL, email);
        props.setProperty(KEY_LOGIN_TIMESTAMP, String.valueOf(System.currentTimeMillis()));

        boolean saved = saveToFile(sessionFile, props, email);
        if (!saved) {
            sessionFile = new File(FALLBACK_SESSION_FILE);
            System.out.println("Retrying with fallback session file: " + sessionFile.getAbsolutePath());
            saved = saveToFile(sessionFile, props, email);
            if (!saved) {
                System.err.println("Failed to save session in both default and fallback locations");
            }
        }
    }

    private static boolean saveToFile(File sessionFile, Properties props, String email) {
        try {
            File parentDir = sessionFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    System.out.println("Created parent directory: " + parentDir.getAbsolutePath());
                } else {
                    System.err.println("Failed to create parent directory: " + parentDir.getAbsolutePath());
                    return false;
                }
            }

            try (FileOutputStream fos = new FileOutputStream(sessionFile)) {
                props.store(fos, "User Session");
                System.out.println("Session saved successfully for user: " + email + " at " + sessionFile.getAbsolutePath());
                return true;
            }
        } catch (IOException e) {
            System.err.println("Failed to save session to " + sessionFile.getAbsolutePath() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static String loadSession() {
        File sessionFile = new File(DEFAULT_SESSION_FILE);
        System.out.println("Attempting to load session from: " + sessionFile.getAbsolutePath());

        String email = loadFromFile(sessionFile);
        if (email == null) {
            sessionFile = new File(FALLBACK_SESSION_FILE);
            System.out.println("Trying fallback session file: " + sessionFile.getAbsolutePath());
            email = loadFromFile(sessionFile);
        }
        return email;
    }

    private static String loadFromFile(File sessionFile) {
        if (!sessionFile.exists()) {
            System.out.println("Session file does not exist: " + sessionFile.getAbsolutePath());
            return null;
        }

        System.out.println("Session file exists: " + sessionFile.exists() +
                ", readable: " + sessionFile.canRead() +
                ", writable: " + sessionFile.canWrite());

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(sessionFile)) {
            props.load(fis);
            String email = props.getProperty(KEY_USER_EMAIL);
            String timestamp = props.getProperty(KEY_LOGIN_TIMESTAMP);

            if (email == null || timestamp == null) {
                System.out.println("Invalid session data in " + sessionFile.getAbsolutePath());
                clearSession();
                return null;
            }

            long loginTime = Long.parseLong(timestamp);
            long currentTime = System.currentTimeMillis();
            if (currentTime - loginTime > SESSION_TIMEOUT) {
                System.out.println("Session expired for user: " + email + " at " + sessionFile.getAbsolutePath());
                clearSession();
                return null;
            }

            System.out.println("Session loaded for user: " + email + " from " + sessionFile.getAbsolutePath());
            return email;
        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load session from " + sessionFile.getAbsolutePath() + ": " + e.getMessage());
            e.printStackTrace();
            clearSession();
            return null;
        }
    }

    public static boolean clearSession() {
        boolean clearedDefault = clearSessionFile(new File(DEFAULT_SESSION_FILE));
        boolean clearedFallback = clearSessionFile(new File(FALLBACK_SESSION_FILE));
        return clearedDefault && clearedFallback;
    }

    private static boolean clearSessionFile(File sessionFile) {
        System.out.println("Attempting to clear session file: " + sessionFile.getAbsolutePath());
        System.out.println("Session file exists: " + sessionFile.exists() +
                ", readable: " + sessionFile.canRead() +
                ", writable: " + sessionFile.canWrite());

        if (!sessionFile.exists()) {
            System.out.println("No session file to clear: " + sessionFile.getAbsolutePath());
            return true;
        }

        try {
            Files.deleteIfExists(Paths.get(sessionFile.getAbsolutePath()));
            System.gc(); // Suggest garbage collection
            if (!sessionFile.exists()) {
                System.out.println("Session file deleted successfully: " + sessionFile.getAbsolutePath());
                return true;
            } else {
                System.err.println("Failed to delete session file during Files.deleteIfExists: " + sessionFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("IOException while deleting session file " + sessionFile.getAbsolutePath() + ": " + e.getMessage());
            e.printStackTrace();
        }

        try {
            if (sessionFile.delete()) {
                System.gc();
                System.out.println("Session file force-deleted successfully: " + sessionFile.getAbsolutePath());
                return true;
            } else {
                System.err.println("Failed to force-delete session file: " + sessionFile.getAbsolutePath());
                return false;
            }
        } catch (SecurityException e) {
            System.err.println("SecurityException while deleting session file " + sessionFile.getAbsolutePath() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}