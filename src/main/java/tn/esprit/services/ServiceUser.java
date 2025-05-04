package tn.esprit.services;

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

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("id"));

                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setTelephone(rs.getString("telephone"));

                String userType = user.determineUserType();
                if ("ADMIN".equals(userType)) {
                    user = new Admin();
                } else if ("PATIENT".equals(userType)) {
                    user = new Patient();
                } else if ("MEDECIN".equals(userType)) {
                    user = new Medecin();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }


    public static List<User> findMedecinsBySpecialite(String specialite) {
        List<User> medecins = new ArrayList<>();
        try {
            Connection conn = MyDataBase.getInstance().getCnx();
            String sql = "SELECT * FROM user WHERE specialite = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, specialite);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setSpecialite(rs.getString("specialite"));
                // Fill other fields if needed
                medecins.add(u);
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
