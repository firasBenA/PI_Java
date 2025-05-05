package tn.esprit.repository;

import tn.esprit.models.Admin;
import tn.esprit.models.Medecin;
import tn.esprit.models.Patient;
import tn.esprit.models.User;
import tn.esprit.utils.DataSource;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UserRepositoryImpl implements UserRepository {
    private final Connection connection;

    public UserRepositoryImpl() {
        this.connection = getValidConnection();
    }

    private Connection getValidConnection() {
        try {
            Connection conn = MyDataBase.getInstance().getCnx();
            if (conn == null || conn.isClosed()) {
                System.err.println("Connection is closed or null. Attempting to reconnect...");
                conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/ehealth_database", "root", "");
                System.out.println("New connection established.");
            }
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to establish database connection: " + e.getMessage());
        }
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
        // Infer userType from roles if not set
        if (user.getUserType() == null && user.getRoles() != null && !user.getRoles().isEmpty()) {
            if (user.getRoles().contains("ROLE_ADMIN")) {
                user.setUserType("ADMIN");
            } else if (user.getRoles().contains("ROLE_PATIENT")) {
                user.setUserType("PATIENT");
            } else if (user.getRoles().contains("ROLE_MEDECIN")) {
                user.setUserType("MEDECIN");
            }
        }
        if (user.getUserType() == null) {
            throw new IllegalArgumentException("User type must be set before saving user with email: " + user.getEmail());
        }
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(List.of("ROLE_" + user.getUserType()));
        }

        String sql = "INSERT INTO user (user_type, email, roles, password, nom, prenom, age, adresse, sexe, telephone, " +
                "image_profil, specialite, certificat, latitude, longitude, email_auth_enabled, email_auth_code, " +
                "created_at, failed_login_attempts, lock_until) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getValidConnection();
             PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setUserParameters(statement, user);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
            }
            return user;
        } catch (SQLException e) {
            System.err.println("Error inserting user email: " + user.getEmail() + ": " + e.getMessage());
            throw new RuntimeException("Failed to insert user: " + e.getMessage());
        }
    }

    private User update(User user) {
        // Infer userType from roles if not set
        if (user.getUserType() == null && user.getRoles() != null && !user.getRoles().isEmpty()) {
            if (user.getRoles().contains("ROLE_ADMIN")) {
                user.setUserType("ADMIN");
            } else if (user.getRoles().contains("ROLE_PATIENT")) {
                user.setUserType("PATIENT");
            } else if (user.getRoles().contains("ROLE_MEDECIN")) {
                user.setUserType("MEDECIN");
            }
        }
        if (user.getUserType() == null) {
            throw new IllegalArgumentException("User type must be set before updating user with email: " + user.getEmail());
        }
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(List.of("ROLE_" + user.getUserType()));
        }

        String sql = "UPDATE user SET user_type = ?, email = ?, roles = ?, password = ?, nom = ?, prenom = ?, age = ?, " +
                "adresse = ?, sexe = ?, telephone = ?, image_profil = ?, specialite = ?, certificat = ?, " +
                "latitude = ?, longitude = ?, email_auth_enabled = ?, email_auth_code = ?, " +
                "created_at = ?, failed_login_attempts = ?, lock_until = ? WHERE id = ?";

        try (Connection conn = getValidConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            setUserParameters(statement, user);
            statement.setInt(21, user.getId());
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                System.err.println("No rows updated for user ID: " + user.getId() + ", email: " + user.getEmail());
            }
            return user;
        } catch (SQLException e) {
            System.err.println("SQL Error updating user ID: " + user.getId() + ", email: " + user.getEmail() + ": " + e.getMessage());
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
        Connection connection = DataSource.getInstance().getConnection(); // ✅ Toujours ici

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = mapResultSetToUser(resultSet);
                    if (user != null) {
                        return user;
                    }
                    System.err.println("Failed to map user for email: " + email);
                    return null;
                }
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by email: " + email + ", Error: " + e.getMessage());
            return null;
        }
    }


    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user";
        try (Connection conn = getValidConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                try {
                    User user = mapResultSetToUser(rs);
                    if (user != null) {
                        users.add(user);
                    }
                } catch (Exception e) {
                    System.err.println("Error mapping user ID: " + rs.getInt("id") + ", email: " + rs.getString("email") + ": " + e.getMessage());
                }
            }
            System.out.println("Total users loaded: " + users.size());
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching users: " + e.getMessage());
        }
    }

    @Override
    public List<User> findAll() {
        return getAllUsers();
    }

    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        String userType = resultSet.getString("user_type");
        if (userType == null || !Arrays.asList("ADMIN", "PATIENT", "MEDECIN").contains(userType)) {
            System.err.println("Invalid user_type for user ID: " + resultSet.getInt("id") + ", email: " + resultSet.getString("email") + ": " + userType);
            return null; // Skip invalid userType
        }

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
                System.err.println("Unexpected user_type: " + userType);
                return null;
        }

        user.setId(resultSet.getInt("id"));
        user.setEmail(resultSet.getString("email"));

        String rolesJson = resultSet.getString("roles");
        List<String> roles = new ArrayList<>();
        if (rolesJson != null && !rolesJson.isEmpty()) {
            String cleaned = rolesJson.replaceAll("[\\[\\]\"]", "").trim();
            if (!cleaned.isEmpty()) {
                roles = Arrays.stream(cleaned.split(","))
                        .map(String::trim)
                        .map(role -> role.equals("BLOCKED") ? "ROLE_BLOCKED" : role)
                        .collect(Collectors.toList());
            }
        }

        // Allow ORIGINAL_ROLE_* roles
        List<String> validRoles = Arrays.asList("ROLE_ADMIN", "ROLE_PATIENT", "ROLE_MEDECIN", "ROLE_BLOCKED",
                "ORIGINAL_ROLE_PATIENT", "ORIGINAL_ROLE_MEDECIN");
        roles = roles.stream()
                .filter(role -> validRoles.contains(role))
                .collect(Collectors.toList());

        if (roles.isEmpty()) {
            System.err.println("No valid roles for user ID: " + user.getId() + ", email: " + user.getEmail());
            return null; // Skip users with no valid roles
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