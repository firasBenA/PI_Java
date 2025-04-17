package tn.esprit.services;

import tn.esprit.models.Consultation;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceConsultation {

    private final Connection connection;

    public ServiceConsultation() {
        connection = MyDataBase.getInstance().getCnx();
    }

    public List<Consultation> getByMedecinId(int medecinId) {
        List<Consultation> consultations = new ArrayList<>();
        String query = "SELECT c.*, u.prenom as patient_prenom, u.nom as patient_nom, r.statut " +
                "FROM consultation c " +
                "JOIN user u ON c.patient_id = u.id " +
                "JOIN rendez_vous r ON c.rendez_vous_id = r.id " +
                "WHERE c.medecin_id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, medecinId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Consultation consultation = new Consultation();
                consultation.setId(rs.getInt("id"));
                consultation.setRendez_vous_id(rs.getInt("rendez_vous_id"));
                consultation.setPatient_id(rs.getInt("patient_id"));
                consultation.setMedecin_id(rs.getInt("medecin_id"));
                consultation.setDate(rs.getDate("date").toLocalDate());
                consultation.setPrix(rs.getDouble("prix"));
                consultation.setType_consultation(rs.getString("type_consultation"));
                consultation.setUser_id(rs.getInt("user_id"));
                consultation.setPatientPrenom(rs.getString("patient_prenom"));
                consultation.setPatientNom(rs.getString("patient_nom"));
                consultation.setStatut(rs.getString("statut")); // Ajout du statut

                consultations.add(consultation);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des consultations: " + e.getMessage());
        }

        return consultations;
    }

    public void updateStatutRendezVous(int rendezVousId, String newStatut) {
        String query = "UPDATE rendez_vous SET statut = ? WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, newStatut);
            pst.setInt(2, rendezVousId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du statut: " + e.getMessage());
        }
    }
    public void updateConsultationDate(int consultationId, LocalDate newDate) {
        String query = "UPDATE consultation SET date = ? WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setDate(1, Date.valueOf(newDate));
            pst.setInt(2, consultationId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la date: " + e.getMessage());
        }
    }

    public int getConsultationCountForDate(int medecinId, LocalDate date) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM consultation WHERE id_medecin = ? AND date = ?";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, medecinId);
            stmt.setDate(2, java.sql.Date.valueOf(date));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du nombre de consultations : " + e.getMessage());
        }

        return count;
    }

    public List<String> getNomsMedecins() {
        // retourne tous les noms de médecins
        return List.of();
    }

    public int getIdMedecinParNom(String nom) {
        // retourne l’ID du médecin en fonction de son nom
        return 0;
    }


}