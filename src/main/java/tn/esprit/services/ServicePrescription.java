package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Personne;
import tn.esprit.models.Prescription;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
            pstm.setDate(7, java.sql.Date.valueOf(LocalDate.now()));


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
                ///date convertion
                java.sql.Date sqlDate = rs.getDate("date_prescription");
                LocalDateTime dateTime = sqlDate.toLocalDate().atStartOfDay();
                p.setDate_prescription(dateTime);
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
            pstm.setTimestamp(7, Timestamp.valueOf(prescription.getDate_prescription()));
            pstm.setInt(8, prescription.getId());

            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while updating prescription: " + e.getMessage());
        }
    }


    @Override
    public void delete(Prescription prescription) {
        String qry = "DELETE FROM `prescription` WHERE `id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, prescription.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while deleting prescription: " + e.getMessage());
        }
    }

}
