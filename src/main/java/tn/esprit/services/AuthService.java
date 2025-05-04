package tn.esprit.services;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import org.apache.commons.mail.SimpleEmail;
import tn.esprit.models.User;
import tn.esprit.repository.UserRepository;
import tn.esprit.services.SessionManager;

import javax.imageio.ImageIO;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class AuthService {

    private static AuthService instance;

    private final UserRepository userRepository;
    private static final String SMTP_HOST = "smtp.mailtrap.io";
    private static final String SMTP_PORT = "465";
    private static final String SMTP_USERNAME = "b05bc95cf579cf";
    private static final String SMTP_PASSWORD = "a2828ad5ae1705";
    private static final String FROM_EMAIL = "no-reply@yourapp.com";
    private User currentUser;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static AuthService getInstance(UserRepository userRepository) {
        if (instance == null) {
            instance = new AuthService(userRepository);
        }
        return instance;
    }

    public void register(User user) throws AuthException {
        if (isEmpty(user.getEmail())) {
            throw new AuthException("L'email est requis.");
        }
        if (isEmpty(user.getPassword())) {
            throw new AuthException("Le mot de passe est requis.");
        }
        if (isEmpty(user.getNom())) {
            throw new AuthException("Le nom est requis.");
        }
        if (isEmpty(user.getPrenom())) {
            throw new AuthException("Le prénom est requis.");
        }
        if (user.getAge() == null || user.getAge() <= 0) {
            throw new AuthException("L'âge est requis et doit être positif.");
        }
        if (isEmpty(user.getAdresse())) {
            throw new AuthException("L'adresse est requise.");
        }
        if (isEmpty(user.getSexe())) {
            throw new AuthException("Le sexe est requis.");
        }
        if (isEmpty(user.getTelephone())) {
            throw new AuthException("Le téléphone est requis.");
        }

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new AuthException("Le rôle est requis.");
        }

        if (isEmpty(user.getUserType())) {
            String inferredUserType = inferUserTypeFromRoles(user.getRoles());
            if (inferredUserType == null) {
                throw new AuthException("Impossible de déterminer le type d'utilisateur à partir des rôles fournis: " + user.getRoles());
            }
            user.setUserType(inferredUserType);
            System.out.println("Inferred userType: " + inferredUserType + " for user with email: " + user.getEmail());
        }

        String expectedRole = switch (user.getUserType()) {
            case "ADMIN" -> "ROLE_ADMIN";
            case "PATIENT" -> "ROLE_PATIENT";
            case "MEDECIN" -> "ROLE_MEDECIN";
            default -> throw new AuthException("Type d'utilisateur invalide: " + user.getUserType());
        };
        if (!user.getRoles().contains(expectedRole)) {
            throw new AuthException("Le rôle " + expectedRole + " est requis pour " + user.getUserType() + ".");
        }

        if ("MEDECIN".equals(user.getUserType())) {
            if (isEmpty(user.getSpecialite())) {
                throw new AuthException("Spécialité requise pour les médecins.");
            }
            if (isEmpty(user.getCertificat())) {
                throw new AuthException("Certificat requis pour les médecins.");
            }
        }

        if (!isEmpty(user.getImageProfil()) && !new File(user.getImageProfil()).exists()) {
            user.setImageProfil(null);
        }

        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new AuthException("Il existe déjà un compte avec cet email.");
        }

        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }

        String verificationCode = generateVerificationCode();
        user.setEmailAuthCode(verificationCode);
        user.setEmailAuthEnabled(false);
        System.out.println("Generated email_auth_code for user " + user.getEmail() + ": " + verificationCode);

        userRepository.save(user);
        System.out.println("User saved with email_auth_code: " + user.getEmailAuthCode() + " for email: " + user.getEmail());

        try {
            sendVerificationEmail(user.getEmail(), verificationCode);
        } catch (AuthException e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
        }
    }

    private String inferUserTypeFromRoles(List<String> roles) {
        if (roles.contains("ROLE_ADMIN")) return "ADMIN";
        if (roles.contains("ROLE_PATIENT")) return "PATIENT";
        if (roles.contains("ROLE_MEDECIN")) return "MEDECIN";
        return null;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private void sendVerificationEmail(String email, String code) throws AuthException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new AuthException("Utilisateur non trouvé pour l'envoi de l'email de vérification.");
        }

        try {
            CountDownLatch latch = new CountDownLatch(1);
            if (!Platform.isFxApplicationThread() && !Platform.isImplicitExit()) {
                Platform.startup(latch::countDown);
                latch.await();
            }

            String userName = user.getPrenom() + " " + user.getNom();
            String htmlContent = String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Email Verification</title>
                </head>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4;">
                    <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; margin: 40px auto;">
                        <tr>
                            <td style="padding: 20px 0; text-align: center; background-color: #007bff; border-radius: 8px 8px 0 0;">
                                <h1 style="color: #ffffff; margin: 0; font-size: 24px;">Verification Email</h1>
                            </td>
                        </tr>
                        <tr>
                            <td style="padding: 20px;">
                                <p style="font-size: 16px; color: #333333;">Salut %s,</p>
                                <p style="font-size: 16px; color: #333333;">Merci de vous être inscrit sur notre site ! Veuillez utiliser le code de vérification ci-dessous pour compléter la vérification :</p>
                                <div style="text-align: center; margin: 20px 0;">
                                    <span style="display: inline-block; padding: 15px 25px; font-size: 18px; font-weight: bold; color: #ffffff; background-color: #28a745; border-radius: 5px;">%s</span>
                                </div>
                                <p style="font-size: 16px; color: #333333;">Ce code est valable pendant 10 minutes. Si vous ne l'avez pas demandé, veuillez ignorer cet e-mail.</p>
                            </td>
                        </tr>
                        <tr>
                            <td style="padding: 15px; text-align: center; background-color: #f4f4f4; border-radius: 0 0 8px 8px;">
                                <p style="font-size: 12px; color: #777777; margin: 0;">© 2025 E Health. Tous les droits sont réservés.</p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """, userName, code);

            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", SMTP_HOST);
            properties.put("mail.smtp.port", SMTP_PORT);
            properties.put("mail.debug", "true");
            properties.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
            properties.put("mail.smtp.ssl.trust", SMTP_HOST);

            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject("Code de vérification de votre compte");

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Verification email sent to: " + email + " with code: " + code);

        } catch (Exception e) {
            e.printStackTrace();
            throw new AuthException("Échec de l'envoi de l'email de vérification : " + e.getMessage());
        }
    }

    public boolean verifyEmailCode(String email, String code) throws AuthException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new AuthException("Utilisateur non trouvé.");
        }
        System.out.println("Retrieved email_auth_code for user " + email + ": " + user.getEmailAuthCode());
        if (user.getEmailAuthCode() == null || !user.getEmailAuthCode().equals(code)) {
            return false;
        }
        user.setEmailAuthEnabled(true);
        user.setEmailAuthCode(null);
        userRepository.save(user);
        System.out.println("Email verified for user " + email + ", email_auth_code cleared.");
        return true;
    }

    public User login(String email, String password) throws AuthException {
        if (isEmpty(email)) {
            throw new AuthException("L'email est requis.");
        }
        if (isEmpty(password)) {
            throw new AuthException("Le mot de passe est requis.");
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new AuthException("Aucun compte trouvé avec cet email.");
        }

        if (!user.getEmailAuthEnabled()) {
            throw new AuthException("Veuillez vérifier votre adresse email avant de vous connecter.");
        }

        if (user.getRoles().contains("ROLE_BLOCKED")) {
            String message = "Votre compte est bloqué par l'administrateur." +
                    (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now()) ?
                            " Réessayez après " + user.getLockUntil().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
            throw new AuthException(message);
        }

        if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
            throw new AuthException("Compte verrouillé. Réessayez après " +
                    user.getLockUntil().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }

        String expectedRole = switch (user.getUserType()) {
            case "ADMIN" -> "ROLE_ADMIN";
            case "MEDECIN" -> "ROLE_MEDECIN";
            case "PATIENT" -> "ROLE_PATIENT";
            default -> throw new AuthException("Type d'utilisateur non supporté : " + user.getUserType());
        };
        if (!user.getRoles().contains(expectedRole)) {
            throw new AuthException("Rôle invalide pour " + user.getUserType() + " : " + user.getRoles());
        }

        if (user.getPassword() == null || !user.checkPassword(password)) {
            int attempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
            user.setFailedLoginAttempts(attempts + 1);
            userRepository.save(user);

            if (user.getFailedLoginAttempts() >= 3) {
                user.setLockUntil(LocalDateTime.now().plusMinutes(30));
                userRepository.save(user);
                throw new AuthException("Compte verrouillé. Réessayez après " +
                        user.getLockUntil().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            throw new AuthException("Mot de passe incorrect.");
        }

        user.setFailedLoginAttempts(0);
        user.setLockUntil(null);
        userRepository.save(user);

        SessionManager.saveSession(email);
        currentUser = user;
        System.out.println("Login successful for user: " + email);
        return user;
    }

    public void logout() {
        SessionManager.clearSession();
        currentUser = null;
    }

    public User getCurrentUser() {
        String email = SessionManager.loadSession();
        System.out.println("AuthService.getCurrentUser: Session email = " + email);
        if (email == null) {
            System.out.println("No session found in SessionManager");
            currentUser = null;
            return null;
        }
        User user = userRepository.findByEmail(email);
        if (user == null) {
            System.out.println("No user found for email: " + email + ", clearing session");
            SessionManager.clearSession();
            currentUser = null;
            return null;
        }
        currentUser = user;
        System.out.println("Current User: " + user.getNom() + " (email: " + email + ")");
        return user;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            SessionManager.saveSession(user.getEmail());
            System.out.println("Current user set: " + user.getEmail());
        } else {
            SessionManager.clearSession();
            System.out.println("Current user cleared");
        }
    }

    public void updateUser(User user) throws AuthException {
        if (user.getId() == null) {
            throw new AuthException("L'ID de l'utilisateur est requis.");
        }
        if (isEmpty(user.getEmail())) {
            throw new AuthException("L'email est requis.");
        }
        if (isEmpty(user.getNom())) {
            throw new AuthException("Le nom est requis.");
        }
        if (isEmpty(user.getPrenom())) {
            throw new AuthException("Le prénom est requis.");
        }
        if (isEmpty(user.getAdresse())) {
            throw new AuthException("L'adresse est requise.");
        }
        if (isEmpty(user.getSexe())) {
            throw new AuthException("Le sexe est requis.");
        }
        if (isEmpty(user.getTelephone())) {
            throw new AuthException("Le téléphone est requis.");
        }
        if (isEmpty(user.getUserType()) || !Arrays.asList("ADMIN", "PATIENT", "MEDECIN").contains(user.getUserType())) {
            throw new AuthException("Le type d'utilisateur doit être ADMIN, PATIENT ou MEDECIN.");
        }
        if ("MEDECIN".equals(user.getUserType())) {
            if (isEmpty(user.getSpecialite())) {
                throw new AuthException("Spécialité requise pour les médecins.");
            }
            if (isEmpty(user.getCertificat())) {
                throw new AuthException("Certificat requis pour les médecins.");
            }
        }
        if (!isEmpty(user.getImageProfil()) && !new File(user.getImageProfil()).exists()) {
            user.setImageProfil(null);
        }

        User existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser != null && !existingUser.getId().equals(user.getId())) {
            throw new AuthException("Cet email est déjà utilisé par un autre compte.");
        }

        try {
            userRepository.save(user);
            SessionManager.saveSession(user.getEmail());
        } catch (Exception e) {
            throw new AuthException("Échec de la mise à jour de l'utilisateur : " + e.getMessage());
        }
    }

    public void deleteUser(Integer userId) throws AuthException {
        if (userId == null) {
            throw new AuthException("L'ID de l'utilisateur est requis.");
        }
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new AuthException("Utilisateur non trouvé.");
        }
        userRepository.delete(userId);
        if (currentUser != null && currentUser.getId().equals(userId)) {
            logout();
        }
    }

    public List<User> getAllUsers() throws AuthException {
        try {
            List<User> users = userRepository.getAllUsers();
            List<User> validUsers = new ArrayList<>();
            for (User user : users) {
                try {
                    if (isEmpty(user.getEmail()) || isEmpty(user.getNom()) || isEmpty(user.getPrenom()) ||
                            isEmpty(user.getAdresse()) || isEmpty(user.getSexe()) || isEmpty(user.getTelephone())) {
                        System.err.println("Skipping invalid user ID: " + user.getId() + ", email: " + user.getEmail());
                        continue;
                    }
                    if (isEmpty(user.getUserType()) || !Arrays.asList("ADMIN", "PATIENT", "MEDECIN").contains(user.getUserType())) {
                        System.err.println("Skipping user with invalid userType ID: " + user.getId() + ", email: " + user.getEmail() + ", userType: " + user.getUserType());
                        continue;
                    }
                    if ("MEDECIN".equals(user.getUserType()) && (isEmpty(user.getSpecialite()) || isEmpty(user.getCertificat()))) {
                        System.err.println("Skipping doctor with missing specialite or certificat ID: " + user.getId() + ", email: " + user.getEmail());
                        continue;
                    }
                    // Allow blocked users
                    if (user.getRoles() != null && user.getRoles().contains("ROLE_BLOCKED")) {
                        validUsers.add(user);
                        continue;
                    }
                    // Validate roles for non-blocked users
                    String expectedRole = switch (user.getUserType()) {
                        case "ADMIN" -> "ROLE_ADMIN";
                        case "PATIENT" -> "ROLE_PATIENT";
                        case "MEDECIN" -> "ROLE_MEDECIN";
                        default -> null;
                    };
                    if (expectedRole != null && !user.getRoles().contains(expectedRole) &&
                            !user.getRoles().contains("ORIGINAL_ROLE_" + user.getUserType())) {
                        System.err.println("Skipping user with invalid role ID: " + user.getId() + ", email: " + user.getEmail() + ", roles: " + user.getRoles());
                        continue;
                    }
                    validUsers.add(user);
                } catch (Exception e) {
                    System.err.println("Error validating user ID: " + user.getId() + ", email: " + user.getEmail() + ": " + e.getMessage());
                }
            }
            System.out.println("Returning " + validUsers.size() + " valid users");
            return validUsers;
        } catch (Exception e) {
            throw new AuthException("Erreur lors de la récupération des utilisateurs : " + e.getMessage());
        }
    }

    public void toggleUserStatus(int userId) throws AuthException {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new AuthException("Utilisateur non trouvé.");
        }

        if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
            user.setLockUntil(null);
        } else {
            user.setLockUntil(LocalDateTime.now().plusHours(24));
        }
        userRepository.save(user);
    }


    public void registerSocial(User user, String provider, String accessToken) throws AuthException {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new AuthException("Email non fourni par " + provider);
        }
        User existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser != null) {
            existingUser.setSocialProvider(provider);
            existingUser.setSocialAccessToken(accessToken);
            userRepository.save(existingUser);
            SessionManager.saveSession(existingUser.getEmail());
            return;
        }
        user.hashPassword(generateRandomPassword());
        user.setSocialProvider(provider);
        user.setSocialAccessToken(accessToken);
        userRepository.save(user);
        SessionManager.saveSession(user.getEmail());
    }

    private String generateRandomPassword() {
        return java.util.UUID.randomUUID().toString().substring(0, 12);
    }

    public User getUserByEmail(String email) throws AuthException {
        if (isEmpty(email)) {
            throw new AuthException("L'email est requis.");
        }
        return userRepository.findByEmail(email);
    }

    public void sendPasswordResetCode(String email) throws AuthException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new AuthException("Aucun compte trouvé avec cet email.");
        }

        // Generate a 6-digit reset code
        String resetCode = String.format("%06d", new Random().nextInt(999999));
        System.out.println("Generated password reset code for " + email + ": " + resetCode);

        // Store the reset code in the user's emailAuthCode field
        user.setEmailAuthCode(resetCode);
        userRepository.save(user);
        System.out.println("Stored reset code in database for " + email + ": " + user.getEmailAuthCode());

        // Prepare email content
        String userName = user.getPrenom() + " " + user.getNom();
        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Password Reset</title>
            </head>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4;">
                <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; margin: 40px auto;">
                    <tr>
                        <td style="padding: 20px 0; text-align: center; background-color: #007bff; border-radius: 8px 8px 0 0;">
                            <h1 style="color: #ffffff; margin: 0; font-size: 24px;">Réinitialiser votre mot de passe</h1>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 20px;">
                            <p style="font-size: 16px; color: #333333;">Salut %s,</p>
                            <p style="font-size: 16px; color: #333333;">Vous avez demandé la réinitialisation de votre mot de passe. Veuillez utiliser le code ci-dessous pour réinitialiser votre mot de passe :</p>
                            <div style="text-align: center; margin: 20px 0;">
                                <span style="display: inline-block; padding: 15px 25px; font-size: 18px; font-weight: bold; color: #ffffff; background-color: #28a745; border-radius: 5px;">%s</span>
                            </div>
                            <p style="font-size: 16px; color: #333333;">Ce code est valable pendant 10 minutes. Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet e-mail.</p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 15px; text-align: center; background-color: #f4f4f4; border-radius: 0 0 8px 8px;">
                            <p style="font-size: 12px; color: #777777; margin: 0;">© 2025 E Health. Tous les droits sont réservés.</p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """, userName, resetCode);

        // Set up mail server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);
        properties.put("mail.debug", "true");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        properties.put("mail.smtp.ssl.trust", SMTP_HOST);

        // Create a session with an authenticator
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });

        try {
            // Create email
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject("Code de réinitialisation du mot de passe");

            // Set HTML content
            message.setContent(htmlContent, "text/html; charset=utf-8");

            // Send the email
            Transport.send(message);
            System.out.println("Password reset code sent successfully to " + email + " with code: " + resetCode);

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new AuthException("Failed to send password reset email: " + e.getMessage());
        }
    }

    public boolean verifyPasswordResetCode(String email, String code) throws AuthException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new AuthException("Utilisateur non trouvé.");
        }
        System.out.println("Verifying reset code for " + email + ": stored=" + user.getEmailAuthCode() + ", provided=" + code);
        if (user.getEmailAuthCode() == null || !user.getEmailAuthCode().equals(code)) {
            return false;
        }
        return true;
    }

    public void resetPassword(String email, String code, String newPassword) throws AuthException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new AuthException("Utilisateur non trouvé.");
        }
        if (user.getEmailAuthCode() == null || !user.getEmailAuthCode().equals(code)) {
            throw new AuthException("Code de réinitialisation invalide.");
        }
        if (isEmpty(newPassword) || newPassword.length() < 6) {
            throw new AuthException("Le nouveau mot de passe doit avoir au moins 6 caractères.");
        }

        user.hashPassword(newPassword);
        user.setEmailAuthCode(null);
        userRepository.save(user);
        System.out.println("Password reset for user " + email + ", email_auth_code cleared.");
    }
}