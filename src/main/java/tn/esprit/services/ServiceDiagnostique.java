package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Diagnostique;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
            pstm.setDate(4, Date.valueOf(diagnostique.getDateDiagnostique()));
            pstm.setString(5, diagnostique.getNom());
            pstm.setString(6, diagnostique.getDescription());
            pstm.setString(7, diagnostique.getZoneCorps());
            pstm.setDate(8, Date.valueOf(diagnostique.getDateSymptomes()));
            pstm.setString(9, diagnostique.getStatus());
            pstm.setString(10, diagnostique.getSelectedSymptoms());

            pstm.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
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
                d.setDateDiagnostique(rs.getDate("date_diagnostique").toLocalDate());
                d.setNom(rs.getString("nom"));
                d.setDescription(rs.getString("description"));
                d.setZoneCorps(rs.getString("zone_corps"));
                d.setDateSymptomes(rs.getDate("date_symptomes").toLocalDate());
                d.setStatus(rs.getString("status"));
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
            pstm.setDate(4, Date.valueOf(diagnostique.getDateDiagnostique()));
            pstm.setString(5, diagnostique.getNom());
            pstm.setString(6, diagnostique.getDescription());
            pstm.setString(7, diagnostique.getZoneCorps());
            pstm.setDate(8, Date.valueOf(diagnostique.getDateSymptomes()));
            pstm.setString(9, diagnostique.getStatus());
            pstm.setString(10, diagnostique.getSelectedSymptoms());
            pstm.setInt(11, diagnostique.getId());

            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while updating diagnostique: " + e.getMessage());
        }
    }

    @Override
    public void delete(Diagnostique diagnostique) {
        String qry = "DELETE FROM `diagnostique` WHERE `id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, diagnostique.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while deleting diagnostique: " + e.getMessage());
        }
    }

}
