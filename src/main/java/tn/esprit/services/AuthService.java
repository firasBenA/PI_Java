package tn.esprit.services;

import org.apache.commons.mail.SimpleEmail;
import tn.esprit.models.User;
import tn.esprit.repository.UserRepository;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AuthService {
    private final UserRepository userRepository;
    private static final String SMTP_HOST = "smtp.mailtrap.io";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_USERNAME = "b05bc95cf579cf";
    private static final String SMTP_PASSWORD = "a2828ad5ae1705";
    private static final String FROM_EMAIL = "no-reply@yourapp.com";

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
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
        if (isEmpty(user.getUserType()) || !Arrays.asList("ADMIN", "PATIENT", "MEDECIN").contains(user.getUserType())) {
            throw new AuthException("Le type d'utilisateur doit être ADMIN, PATIENT ou MEDECIN.");
        }

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new AuthException("Le rôle est requis.");
        }
        String expectedRole = switch (user.getUserType()) {
            case "ADMIN" -> "ROLE_ADMIN";
            case "PATIENT" -> "ROLE_PATIENT";
            case "MEDECIN" -> "ROLE_MEDECIN";
            default -> throw new AuthException("Type d'utilisateur invalide.");
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

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private void sendVerificationEmail(String email, String code) throws AuthException {
        try {
            SimpleEmail simpleEmail = new SimpleEmail();
            simpleEmail.setHostName("sandbox.smtp.mailtrap.io");
            simpleEmail.setSmtpPort(587);
            simpleEmail.setAuthenticator(new org.apache.commons.mail.DefaultAuthenticator(
                    "b05bc95cf579cf", "a2828ad5ae1705"
            ));
            simpleEmail.setStartTLSEnabled(true);
            simpleEmail.setFrom("no-reply@yourapp.com");
            simpleEmail.setSubject("Code de vérification de votre compte");
            simpleEmail.setMsg("Votre code de vérification est : " + code);
            simpleEmail.addTo(email);
            simpleEmail.setDebug(true);
            simpleEmail.send();
            System.out.println("Verification email sent to: " + email + " with code: " + code);
        } catch (Exception e) {
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
        return user;
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

        User existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser != null && !existingUser.getId().equals(user.getId())) {
            throw new AuthException("Cet email est déjà utilisé par un autre compte.");
        }

        userRepository.save(user);
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
                    if ("MEDECIN".equals(user.getUserType()) && (isEmpty(user.getSpecialite()) || isEmpty(user.getCertificat()))) {
                        System.err.println("Skipping doctor with missing specialite or certificat ID: " + user.getId() + ", email: " + user.getEmail());
                        continue;
                    }
                    validUsers.add(user);
                } catch (Exception e) {
                    System.err.println("Error validating user ID: " + user.getId() + ", email: " + user.getEmail() + ": " + e.getMessage());
                }
            }
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
            return;
        }
        user.hashPassword(generateRandomPassword());
        user.setSocialProvider(provider);
        user.setSocialAccessToken(accessToken);
        userRepository.save(user);
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
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject("Code de réinitialisation du mot de passe");
            message.setText("Votre code de réinitialisation du mot de passe est le suivant : " + resetCode);
            System.out.println("Préparation de l'envoi du code de réinitialisation à " + email + " code: " + resetCode);

            // Send the email
            Transport.send(message);
            System.out.println("Code de réinitialisation du mot de passe envoyé avec succès à " + email + "  code: " + resetCode);

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