package tn.esprit.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import tn.esprit.interfaces.IService;
import tn.esprit.models.Diagnostique;
import tn.esprit.utils.MyDataBase;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceDiagnostique implements IService<Diagnostique> {

    private Connection cnx;


    public ServiceDiagnostique() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Diagnostique diagnostique) {
        String qry = "INSERT INTO `diagnostique`(`dossier_medical_id`, `patient_id`, `medecin_id`, `date_diagnostique`, `nom`, `description`, `zone_corps`, `date_symptomes`, `status`, `selected_symptoms`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, diagnostique.getDossierMedicalId());
            pstm.setInt(2, diagnostique.getPatientId());
            pstm.setInt(3, diagnostique.getMedecinId());
            pstm.setDate(4, diagnostique.getDateDiagnostique());
            pstm.setString(5, diagnostique.getNom());
            pstm.setString(6, diagnostique.getDescription());
            pstm.setString(7, diagnostique.getZoneCorps());
            pstm.setDate(8, diagnostique.getDateSymptomes());
            pstm.setInt(9, diagnostique.getStatus());
            pstm.setString(10, diagnostique.getSelectedSymptoms());

            pstm.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<Diagnostique> getPendingDiagnostiques() {
        List<Diagnostique> diagnostiques = new ArrayList<>();

        String qry = "SELECT * FROM diagnostique WHERE status = 0";

        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);

            while (rs.next()) {
                Diagnostique d = new Diagnostique();
                d.setId(rs.getInt("id"));
                d.setDossierMedicalId(rs.getInt("dossier_medical_id"));
                d.setPatientId(rs.getInt("patient_id"));
                d.setMedecinId(rs.getInt("medecin_id"));
                d.setDateDiagnostique(rs.getDate("date_diagnostique"));
                d.setNom(rs.getString("nom"));
                d.setDescription(rs.getString("description"));
                d.setZoneCorps(rs.getString("zone_corps"));
                d.setDateSymptomes(rs.getDate("date_symptomes"));
                d.setStatus(rs.getInt("status"));
                d.setSelectedSymptoms(rs.getString("selected_symptoms"));

                diagnostiques.add(d);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return diagnostiques;
    }

    @Override
    public List<Diagnostique> getAll() {
        List<Diagnostique> diagnostiques = new ArrayList<>();

        String qry = "SELECT * FROM `diagnostique`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);

            while (rs.next()) {
                Diagnostique d = new Diagnostique();
                d.setId(rs.getInt("id"));
                d.setDossierMedicalId(rs.getInt("dossier_medical_id"));
                d.setPatientId(rs.getInt("patient_id"));
                d.setMedecinId(rs.getInt("medecin_id"));
                d.setDateDiagnostique(rs.getDate("date_diagnostique"));
                d.setNom(rs.getString("nom"));
                d.setDescription(rs.getString("description"));
                d.setZoneCorps(rs.getString("zone_corps"));
                d.setDateSymptomes(rs.getDate("date_symptomes"));
                d.setStatus(rs.getInt("status"));
                d.setSelectedSymptoms(rs.getString("selected_symptoms"));

                diagnostiques.add(d);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return diagnostiques;
    }

    @Override
    public void update(Diagnostique diagnostique) {
        String qry = "UPDATE `diagnostique` SET `dossier_medical_id` = ?, `patient_id` = ?, `medecin_id` = ?, `date_diagnostique` = ?, `nom` = ?, `description` = ?, `zone_corps` = ?, `date_symptomes` = ?, `status` = ?, `selected_symptoms` = ? " +
                "WHERE `id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, diagnostique.getDossierMedicalId());
            pstm.setInt(2, diagnostique.getPatientId());
            pstm.setInt(3, diagnostique.getMedecinId());
            pstm.setDate(4, diagnostique.getDateDiagnostique());
            pstm.setString(5, diagnostique.getNom());
            pstm.setString(6, diagnostique.getDescription());
            pstm.setString(7, diagnostique.getZoneCorps());
            pstm.setDate(8, diagnostique.getDateSymptomes());
            pstm.setInt(9, diagnostique.getStatus());
            pstm.setString(10, diagnostique.getSelectedSymptoms());
            pstm.setInt(11, diagnostique.getId());

            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while updating diagnostique: " + e.getMessage());
        }
    }

    @Override
    public void delete(Diagnostique diagnostique) {
        System.out.println(diagnostique.getId());
        String sql = "DELETE FROM diagnostique WHERE id = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/ehealth_database", "root", "");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, diagnostique.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error deleting diagnosis: " + e.getMessage());
        }
    }


    /////
    public static Map<String, String> diagnose(List<String> symptoms) {
        final String flaskApiUrl = "http://127.0.0.1:5000/predict";

        try {
            // Create JSON payload
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(Map.of("symptoms", symptoms));

            // Create HTTP request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(flaskApiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Parse the response
                return mapper.readValue(response.body(), Map.class);
            } else {
                throw new RuntimeException("Error from API: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error diagnosing symptoms: " + e.getMessage());
        }
    }

    // Save the diagnostic result into the database
    public static void saveDiagnosis(Diagnostique diagnostique) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/ehealth_database", "root", "")) {
            String sql = """
            INSERT INTO diagnostique (
                nom, 
                selected_symptoms, 
                medecin_id, 
                description, 
                date_diagnostique, 
                patient_id, 
                dossier_medical_id, 
                zone_corps, 
                date_symptomes, 
                status
            ) VALUES (?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?)
        """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, diagnostique.getNom());
            stmt.setString(2, String.join(",", diagnostique.getSelectedSymptoms()));
            stmt.setInt(3, diagnostique.getMedecinId());
            stmt.setString(4, diagnostique.getDescription());
            stmt.setInt(5, diagnostique.getPatientId());
            stmt.setInt(6, diagnostique.getDossierMedicalId());
            stmt.setString(7, diagnostique.getZoneCorps());
            stmt.setDate(8, diagnostique.getDateSymptomes());
            stmt.setInt(9, diagnostique.getStatus());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error saving diagnosis: " + e.getMessage());
        }
    }


    // Fetch doctors from the database
    public static Map<String, Integer> getDoctors() {
        Map<String, Integer> doctorIdMap = new HashMap<>();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/ehealth_database", "root", "")) {
            PreparedStatement stmt = conn.prepareStatement("SELECT id, nom FROM user WHERE roles = '[\"ROLE_MEDECIN\"]'");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("nom");
                int id = rs.getInt("id");
                doctorIdMap.put(name, id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doctorIdMap;
    }
    /////


    public boolean exists(int diagnostiqueId) {
        String query = "SELECT COUNT(*) FROM diagnostique WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, diagnostiqueId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}
