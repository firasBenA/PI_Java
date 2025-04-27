package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Prescription;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServicePrescription implements IService<Prescription> {

    private Connection cnx;

    public ServicePrescription(){cnx = MyDataBase.getInstance().getCnx();}
    @Override
    public void add(Prescription prescription) {
    String qry ="INSERT INTO `prescription`(`dossier_medical_id`, `diagnostique_id`, `medecin_id`, `patient_id`, `titre`, `contenue`, `date_prescription`)" +
            " VALUES (?,?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1,1);
            pstm.setInt(2,19);
            pstm.setInt(3,3);
            pstm.setInt(4,1);
            pstm.setString(5,prescription.getTitre());
            pstm.setString(6,prescription.getContenue());
            pstm.setDate(7, Date.valueOf(LocalDate.now()));


            pstm.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    @Override
    public List<Prescription> getAll() {
        List<Prescription> prescriptions = new ArrayList<>();

        String qry ="SELECT * FROM `prescription`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);

            while(rs.next()){
                Prescription p =new Prescription();
                p.setId(rs.getInt(1));
                p.setTitre(rs.getString("titre"));
                p.setContenue(rs.getString("contenue"));
                p.setDiagnostique_id(rs.getInt("diagnostique_id"));
                p.setDossier_medical_id(rs.getInt("dossier_medical_id"));
                p.setMedecin_id(rs.getInt("medecin_id"));
                p.setPatient_id(rs.getInt("patient_id"));

                ///date convertion
                Date sqlDate = rs.getDate("date_prescription");
                Date utilDate = new Date(sqlDate.getTime());
                p.setDate_prescription(utilDate);

                //////

                prescriptions.add(p);
            }


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }


        return prescriptions;
    }

    @Override
    public void update(Prescription prescription) {
        String qry = "UPDATE `prescription` SET `dossier_medical_id` = ?, `diagnostique_id` = ?, `medecin_id` = ?, `patient_id` = ?, `titre` = ?, `contenue` = ?, `date_prescription` = ? " +
                "WHERE `id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);

            pstm.setInt(1, prescription.getDossier_medical_id());
            pstm.setInt(2, prescription.getDiagnostique_id());
            pstm.setInt(3, prescription.getMedecin_id());
            pstm.setInt(4, prescription.getPatient_id());
            pstm.setString(5, prescription.getTitre());
            pstm.setString(6, prescription.getContenue());
            pstm.setDate(7, prescription.getDate_prescription());
            pstm.setInt(8, prescription.getId());

            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while updating prescription: " + e.getMessage());
        }
    }

    @Override
    public void delete(Prescription prescription) {
        String qry = "DELETE FROM `prescription` WHERE `id` = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, prescription.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while deleting prescription: " + e.getMessage());
        }
    }

    public boolean deletePrescriptionById(int id) {
        String qry = "DELETE FROM prescription WHERE id = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}
