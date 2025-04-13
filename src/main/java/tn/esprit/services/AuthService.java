package tn.esprit.services;
import tn.esprit.models.User;
import tn.esprit.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

public class AuthService {

        private final UserRepository userRepository;

        public AuthService(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        public User login(String email, String password) throws AuthException {
            if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
                throw new AuthException("Veuillez remplir les champs email et mot de passe.");
            }

            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new AuthException("Aucun compte trouvé avec cet email.");
            }

            // Check if account is locked
            if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
                throw new AuthException("Votre compte est bloqué. Veuillez réessayer plus tard.");
            }

            // Verify password
            if (!user.checkPassword(password)) {
                // Increment failed attempts
                user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

                // Lock account after 3 failed attempts
                if (user.getFailedLoginAttempts() >= 3) {
                    user.setLockUntil(LocalDateTime.now().plusMinutes(15));
                    userRepository.save(user);
                    throw new AuthException("Trop de tentatives échouées. Votre compte est bloqué pour 15 minutes.");
                }

                userRepository.save(user);
                throw new AuthException("Mot de passe incorrect.");
            }

            // Reset failed attempts on successful login
            user.setFailedLoginAttempts(0);
            user.setLockUntil(null);
            userRepository.save(user);

            return user;
        }

        public void register(User user) throws AuthException {
            // Validate required fields
            if (user.getCreatedAt() == null) {
                user.setCreatedAt(LocalDateTime.now());
            }

            // Validation supplémentaire pour les médecins
            if(user.getRoles().contains("ROLE_MEDECIN")) {
                if(user.getSpecialite() == null || user.getSpecialite().isEmpty()) {
                    throw new AuthException("Spécialité requise pour les médecins");
                }
                if(user.getCertificat() == null || user.getCertificat().isEmpty()) {
                    throw new AuthException("Certificat requis pour les médecins");
                }
            }

            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                throw new AuthException("L'email est requis.");
            }

            // Check if email already exists
            if (userRepository.findByEmail(user.getEmail()) != null) {
                throw new AuthException("Il existe déjà un compte avec cet email.");
            }

            // Set default role if none provided
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                user.getRoles().add("ROLE_USER");
            }

            // Hash password before saving
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                throw new AuthException("Le mot de passe est requis");
            }

            userRepository.save(user);
        }
    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
    public void updateUser(User user) throws AuthException {
        if (user == null || user.getId() == null) {
            throw new AuthException("Utilisateur invalide");
        }
        userRepository.save(user);
    }
    public void deleteUser(int userId) throws AuthException {
        try {
            userRepository.delete(userId);
        } catch (Exception e) {
            throw new AuthException("Échec de la suppression: " + e.getMessage());
        }
    }

    //admin
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean toggleUserStatus(int userId) throws Exception {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new Exception("Utilisateur non trouvé");
        }

        if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
            user.setLockUntil(null); // Débloquer
        } else {
            user.setLockUntil(LocalDateTime.now().plusDays(30)); // Bloquer
        }

        userRepository.save(user);
        return true;
    }



    private User currentUser;

    public void logout() {
        this.currentUser = null;
        // Autres opérations de nettoyage si nécessaire
    }

    public User getCurrentUser() {
        return currentUser;
    }

}




