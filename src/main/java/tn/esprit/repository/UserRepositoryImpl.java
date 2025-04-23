package tn.esprit.repository;

import tn.esprit.models.Admin;
import tn.esprit.models.Medecin;
import tn.esprit.models.Patient;
import tn.esprit.models.User;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserRepositoryImpl implements UserRepository {
    private final Connection connection;

    public UserRepositoryImpl() {
        this.connection = MyDataBase.getInstance().getCnx();
    }

    @Override
    public User save(User user) {
        if (user.getId() == null || user.getId() == 0) {
            return insert(user);
        } else {
            return update(user);
        }
    }

    private User insert(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(List.of("ROLE_USER"));
        }

        String sql = "INSERT INTO user (user_type, email, roles, password, nom, prenom, age, adresse, sexe, telephone, " +
                "image_profil, specialite, certificat, latitude, longitude, email_auth_enabled, " +
                "email_auth_code, created_at, failed_login_attempts, lock_until) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Échec de l'enregistrement : " + e.getMessage(), e);
        }
    }

    private User update(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(List.of("ROLE_USER"));
        }

        String sql = "UPDATE user SET user_type = ?, email = ?, roles = ?, password = ?, nom = ?, prenom = ?, age = ?, " +
                "adresse = ?, sexe = ?, telephone = ?, image_profil = ?, specialite = ?, certificat = ?, " +
                "latitude = ?, longitude = ?, email_auth_enabled = ?, email_auth_code = ?, " +
                "created_at = ?, failed_login_attempts = ?, lock_until = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            setUserParameters(statement, user);
            statement.setInt(21, user.getId());
            statement.executeUpdate();
            return user;
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Échec de la mise à jour : " + e.getMessage(), e);
        }
    }

    private void setUserParameters(PreparedStatement statement, User user) throws SQLException {
        statement.setString(1, user.getUserType());
        statement.setString(2, user.getEmail());
        statement.setString(3, serializeRolesToJson(user.getRoles()));
        statement.setString(4, user.getPassword());
        statement.setString(5, user.getNom());
        statement.setString(6, user.getPrenom());
        statement.setInt(7, user.getAge() != null ? user.getAge() : 0);
        statement.setString(8, user.getAdresse());
        statement.setString(9, user.getSexe());
        statement.setString(10, user.getTelephone());
        statement.setObject(11, user.getImageProfil());
        statement.setString(12, user.getSpecialite());
        statement.setString(13, user.getCertificat());
        statement.setObject(14, user.getLatitude());
        statement.setObject(15, user.getLongitude());
        statement.setBoolean(16, user.getEmailAuthEnabled());
        statement.setString(17, user.getEmailAuthCode() != null ? user.getEmailAuthCode() : "");
        statement.setTimestamp(18, Timestamp.valueOf(user.getCreatedAt()));
        statement.setInt(19, user.getFailedLoginAttempts());
        if (user.getLockUntil() != null) {
            statement.setTimestamp(20, Timestamp.valueOf(user.getLockUntil()));
        } else {
            statement.setNull(20, Types.TIMESTAMP);
        }
    }

    private String serializeRolesToJson(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return "[\"ROLE_USER\"]";
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
            throw new RuntimeException("Échec de la recherche par email", e);
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
            throw new RuntimeException("Échec de la récupération des utilisateurs", e);
        }
    }

    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        String userType = resultSet.getString("user_type");
        User user;
        switch (userType) {
            case "ADMIN":
                user = new Admin();
                break;
            case "PATIENT":
                user = new Patient();
                break;
            case "MEDECIN":
                user = new Medecin();
                break;
            default:
                throw new SQLException("Type d'utilisateur invalide : " + userType);
        }

        user.setId(resultSet.getInt("id"));
        user.setEmail(resultSet.getString("email"));

        String rolesJson = resultSet.getString("roles");
        List<String> roles = new ArrayList<>();
        if (rolesJson != null && !rolesJson.isEmpty()) {
            String cleaned = rolesJson.replaceAll("[\\[\\]\"]", "").trim();
            if (!cleaned.isEmpty()) {
                roles = new ArrayList<>(Arrays.asList(cleaned.split(",")));
            }
        }

        // Validate roles
        if (roles.isEmpty()) {
            System.err.println("No valid roles for user ID: " + user.getId() + ", email: " + user.getEmail());
            throw new SQLException("Rôles invalides pour l'utilisateur : aucun rôle défini");
        }
        for (String role : roles) {
            if (!Arrays.asList("ROLE_ADMIN", "ROLE_PATIENT", "ROLE_MEDECIN").contains(role)) {
                System.err.println("Invalid role '" + role + "' for user ID: " + user.getId() + ", email: " + user.getEmail());
                throw new SQLException("Rôle invalide pour l'utilisateur : " + role);
            }
        }
        user.setRoles(roles);

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
        user.setLatitude(resultSet.getObject("latitude") != null ? resultSet.getDouble("latitude") : null);
        user.setLongitude(resultSet.getObject("longitude") != null ? resultSet.getDouble("longitude") : null);
        user.setEmailAuthEnabled(resultSet.getBoolean("email_auth_enabled"));
        user.setEmailAuthCode(resultSet.getString("email_auth_code"));
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        user.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        user.setFailedLoginAttempts(resultSet.getInt("failed_login_attempts"));
        Timestamp lockUntil = resultSet.getTimestamp("lock_until");
        user.setLockUntil(lockUntil != null ? lockUntil.toLocalDateTime() : null);
        user.setUserType(userType);

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
            throw new RuntimeException("Échec de la recherche par ID", e);
        }
        return null;
    }

    @Override
    public void delete(int userId) {
        String sql = "DELETE FROM user WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de la suppression, aucun utilisateur trouvé.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur de suppression : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Échec de la suppression : " + e.getMessage(), e);
        }
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
            throw new RuntimeException("Échec de la recherche d'utilisateurs", e);
        }
        return users;
    }
}