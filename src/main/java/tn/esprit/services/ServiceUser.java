package tn.esprit.services;

import org.json.JSONArray;
import tn.esprit.models.Admin;
import tn.esprit.models.Medecin;
import tn.esprit.models.Patient;
import tn.esprit.models.User;
import tn.esprit.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser {

    private Connection connection;

    public ServiceUser() {
        connection = MyDataBase.getInstance().getCnx();
    }

    public User getUserById(int id) {
        User user = null;
        String query = "SELECT * FROM user WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    String rawRoles = rs.getString("roles");
                    // Supposons que roles est une chaîne JSON comme ["ROLE_MEDECIN"]
                    JSONArray jsonArray = new JSONArray(rawRoles);
                    String role = jsonArray.getString(0);

                    switch (role) {
                        case "ROLE_MEDECIN":
                            user = new Medecin();
                            user.setId(rs.getInt("id"));
                            break;

                        case "ROLE_PATIENT":
                            user = new Patient();
                            user.setId(rs.getInt("id"));
                            break;

                        case "ROLE_ADMIN":
                            user = new Admin();
                            user.setId(rs.getInt("id"));
                            // Ajouter d'autres attributs spécifiques à l'admin si nécessaire
                            break;

                        default:
                            throw new IllegalArgumentException("Role inconnu : " + role);
                    }
                    user.setId(rs.getInt("id"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }


    public static List<Medecin> findMedecinsBySpecialite(String specialite) {
        List<Medecin> medecins = new ArrayList<>();
        try {
            Connection conn = MyDataBase.getInstance().getCnx();
            String sql = "SELECT * FROM user WHERE specialite = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, specialite);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Medecin medecin = new Medecin();  // Instantiate Medecin, not User
                medecin.setId(rs.getInt("id"));
                medecin.setNom(rs.getString("nom"));
                medecin.setPrenom(rs.getString("prenom"));
                medecin.setSpecialite(rs.getString("specialite"));
                // Fill other fields specific to Medecin if needed
                medecins.add(medecin);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return medecins;
    }


    public static int findDoctorIdByName(String name) {
        int id = -1;
        try {
            Connection conn = MyDataBase.getInstance().getCnx();
            String query = "SELECT id FROM user WHERE nom = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, name);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                id = rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }


}