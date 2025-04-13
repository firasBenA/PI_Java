package tn.esprit.repository;

import tn.esprit.models.User;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserRepositoryImpl implements UserRepository {
    private final Connection connection;

    public UserRepositoryImpl() {
        this.connection = MyDataBase.getInstance().getCnx();
    }

    @Override
    public User save(User user) {
        if (user.getId() == null || user.getId() == 0) {  // Fixed null/0 comparison
            return insert(user);
        } else {
            return update(user);
        }
    }

    private User insert(User user) {
        // Ensure roles exist
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(List.of("ROLE_USER"));
        }

        String sql = "INSERT INTO user (email, roles, password, nom, prenom, age, adresse, sexe, telephone, " +
                "image_profil, specialite, certificat, latitude, longitude, email_auth_enabled, " +
                "email_auth_code, created_at, failed_login_attempts, lock_until) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setUserParameters(statement, user);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }

            return user;
        } catch (SQLException e) {
            System.err.println("Erreur SQL:");
            System.err.println("Message: " + e.getMessage());
            System.err.println("Code d'état: " + e.getSQLState());
            System.err.println("Code d'erreur: " + e.getErrorCode());
            e.printStackTrace(); // Pour la stack trace complète
            throw new RuntimeException("Échec de l'enregistrement : " + e.getMessage(), e);
        }
    }

    private User update(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(List.of("ROLE_USER"));
        }

        String sql = "UPDATE user SET " +
                "email = ?, roles = ?, password = ?, nom = ?, prenom = ?, age = ?, " +
                "adresse = ?, sexe = ?, telephone = ?, image_profil = ?, specialite = ?, certificat = ?, " +
                "latitude = ?, longitude = ?, email_auth_enabled = ?, email_auth_code = ?, " +
                "created_at = ?, failed_login_attempts = ?, lock_until = ? " +
                "WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            // Set all parameters
            statement.setString(1, user.getEmail());
            statement.setString(2, serializeRolesToJson(user.getRoles()));
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getNom());
            statement.setString(5, user.getPrenom());
            statement.setInt(6, user.getAge() != null ? user.getAge() : 0);
            statement.setString(7, user.getAdresse());
            statement.setString(8, user.getSexe());
            statement.setString(9, user.getTelephone());
            statement.setString(10, user.getImageProfil());
            statement.setString(11, user.getSpecialite());
            statement.setString(12, user.getCertificat());
            statement.setObject(13, user.getLatitude(), Types.FLOAT);
            statement.setObject(14, user.getLongitude(), Types.FLOAT);
            statement.setBoolean(15, user.getEmailAuthEnabled());
            statement.setString(16, user.getEmailAuthCode() != null ? user.getEmailAuthCode() : "");
            statement.setTimestamp(17, Timestamp.valueOf(user.getCreatedAt()));
            statement.setInt(18, user.getFailedLoginAttempts());

            // Handle lock_until which might be null
            if (user.getLockUntil() != null) {
                statement.setTimestamp(19, Timestamp.valueOf(user.getLockUntil()));
            } else {
                statement.setNull(19, Types.TIMESTAMP);
            }

            // Set the ID for WHERE clause
            statement.setInt(20, user.getId());

            statement.executeUpdate();
            return user;
        } catch (SQLException e) {
            System.err.println("Error updating user:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
        }
    }
    @Override  // Add this annotation to properly override
    public void delete(int userId) {
        String sql = "DELETE FROM user WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting user failed, no rows affected.");
            }
        } catch (SQLException e) {
            // Replace logError with direct logging
            System.err.println("Error deleting user:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    private void setUserParameters(PreparedStatement statement, User user) throws SQLException {
        statement.setString(1, user.getEmail());
        statement.setString(2, serializeRolesToJson(user.getRoles()));         statement.setString(3, user.getPassword());
        statement.setString(4, user.getNom());
        statement.setString(5, user.getPrenom());
        statement.setInt(6, user.getAge());
        statement.setString(7, user.getAdresse());
        statement.setString(8, user.getSexe());
        statement.setString(9, user.getTelephone());
        statement.setObject(10, user.getImageProfil());
        statement.setString(11, user.getSpecialite() );
        statement.setString(12, user.getCertificat()) ;
        statement.setObject(13, user.getLatitude());
        statement.setObject(14, user.getLongitude());
        statement.setBoolean(15, user.getEmailAuthEnabled());  // Changed from isEmailAuthEnabled()
        statement.setString(16, user.getEmailAuthCode() != null ? user.getEmailAuthCode() : "");
        statement.setTimestamp(17, Timestamp.valueOf(user.getCreatedAt()));
        statement.setInt(18, user.getFailedLoginAttempts());
        if (user.getLockUntil() != null) {
            statement.setTimestamp(19, Timestamp.valueOf(user.getLockUntil()));
        } else {
            statement.setNull(19, Types.TIMESTAMP); // Spécifiez explicitement le type SQL
        }
    }
    private String getNullIfEmpty(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value;
    }

    private String serializeRolesToJson(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return "[\"ROLE_USER\"]"; // default role
        }
        return "[\"" + String.join("\",\"", roles) + "\"]";
    }

    @Override
    public User findByEmail(String email) {
        String sql = "SELECT * FROM user WHERE email = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by email", e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM user";
        List<User> users = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all users", e);
        }
    }

    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setEmail(resultSet.getString("email"));

        // Parse roles
        String rolesJson = resultSet.getString("roles");
        if (rolesJson != null && !rolesJson.isEmpty()) {
            // Remove brackets and quotes, then split
            String cleaned = rolesJson.replaceAll("[\\[\\]\"]", "");
            if (!cleaned.isEmpty()) {
                user.getRoles().addAll(List.of(cleaned.split(",")));
            }
        }

        user.setPassword(resultSet.getString("password"));
        user.setNom(resultSet.getString("nom"));
        user.setPrenom(resultSet.getString("prenom"));
        user.setAge(resultSet.getInt("age"));
        user.setAdresse(resultSet.getString("adresse"));
        user.setSexe(resultSet.getString("sexe"));
        user.setTelephone(resultSet.getString("telephone"));
        user.setImageProfil(resultSet.getString("image_profil"));
        user.setSpecialite(resultSet.getString("specialite"));
        user.setCertificat(resultSet.getString("certificat"));
        user.setLatitude(resultSet.getObject("latitude") != null ? resultSet.getFloat("latitude") : null);
        user.setLongitude(resultSet.getObject("longitude") != null ? resultSet.getFloat("longitude") : null);
        user.setEmailAuthEnabled(resultSet.getBoolean("email_auth_enabled"));
        user.setEmailAuthCode(resultSet.getString("email_auth_code"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        user.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);

        user.setFailedLoginAttempts(resultSet.getInt("failed_login_attempts"));

        Timestamp lockUntil = resultSet.getTimestamp("lock_until");
        user.setLockUntil(lockUntil != null ? lockUntil.toLocalDateTime() : null);

        return user;
    }
    @Override
    public User findById(int id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by id", e);
        }
        return null;
    }

    @Override
    public List<User> searchUsers(String keyword) {
        String sql = "SELECT * FROM user WHERE LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ? OR LOWER(email) LIKE ?";
        List<User> users = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String searchTerm = "%" + keyword.toLowerCase() + "%";
            for (int i = 1; i <= 3; i++) {
                statement.setString(i, searchTerm);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapResultSetToUser(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search users", e);
        }
        return users;
    }



}