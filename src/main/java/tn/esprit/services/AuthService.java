package tn.esprit.services;

import tn.esprit.models.User;
import tn.esprit.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public void register(User user) throws AuthException {
        // Validate NOT NULL fields
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

        // Validate roles
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

        // Validate Medecin-specific fields
        if ("MEDECIN".equals(user.getUserType())) {
            if (isEmpty(user.getSpecialite())) {
                throw new AuthException("Spécialité requise pour les médecins.");
            }
            if (isEmpty(user.getCertificat())) {
                throw new AuthException("Certificat requis pour les médecins.");
            }
        }

        // Check for existing email
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new AuthException("Il existe déjà un compte avec cet email.");
        }

        // Set created_at if not set
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }

        userRepository.save(user);
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

        if (user.getPassword() == null || !user.checkPassword(password)) {
            int attempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
            user.setFailedLoginAttempts(attempts + 1);
            userRepository.save(user);

            if (user.getFailedLoginAttempts() >= 3) {
                user.setLockUntil(LocalDateTime.now().plusMinutes(30));
                userRepository.save(user);
                throw new AuthException("Compte verrouillé. Réessayez dans 30 minutes.");
            }
            throw new AuthException("Mot de passe incorrect.");
        }

        if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
            throw new AuthException("Compte verrouillé. Réessayez dans 30 minutes.");
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

        // Validate Medecin-specific fields
        if ("MEDECIN".equals(user.getUserType())) {
            if (isEmpty(user.getSpecialite())) {
                throw new AuthException("Spécialité requise pour les médecins.");
            }
            if (isEmpty(user.getCertificat())) {
                throw new AuthException("Certificat requis pour les médecins.");
            }
        }

        // Check if email is taken by another user
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
                    // Validate mandatory fields
                    if (isEmpty(user.getEmail()) || isEmpty(user.getNom()) || isEmpty(user.getPrenom()) ||
                            isEmpty(user.getAdresse()) || isEmpty(user.getSexe()) || isEmpty(user.getTelephone())) {
                        System.err.println("Skipping invalid user ID: " + user.getId() + ", email: " + user.getEmail());
                        continue;
                    }
                    // Validate doctor-specific fields
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
            // Unblock: Clear lock_until
            user.setLockUntil(null);
        } else {
            // Block: Set lock_until to 24 hours from now
            user.setLockUntil(LocalDateTime.now().plusHours(24));
        }
        userRepository.save(user);
    }
}