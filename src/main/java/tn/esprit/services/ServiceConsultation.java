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
                "JOIN user u ON c.user_id = u.id " +
                "JOIN rendez_vous r ON c.rendez_vous_id = r.id " +
                "WHERE c.user_id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, medecinId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Consultation consultation = new Consultation();
                consultation.setId(rs.getInt("id"));
                consultation.setRendez_vous_id(rs.getInt("rendez_vous_id"));
                consultation.setUser_id(rs.getInt(11));
                consultation.setDate(rs.getDate("date").toLocalDate());
                consultation.setPrix(rs.getDouble("prix"));
                consultation.setType_consultation(rs.getString("type_consultation"));
                consultation.setPatientPrenom(rs.getString("patient_prenom"));
                consultation.setPatientNom(rs.getString("patient_nom"));
                consultation.setStatut(rs.getString("statut")); // Statut depuis rendez_vous

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
        String sql = "SELECT COUNT(*) FROM consultation WHERE user_id = ? AND date = ?";

        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, 11);
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

    public String getPatientEmailById(int patientId) throws SQLException {
        String query = "SELECT email FROM user WHERE id = ? AND roles LIKE '%PATIENT%'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
            throw new SQLException("Patient non trouvé ou non associé au rôle 'patient'");
        }
    }

    public void updateConsultationDateAndPrice(int consultationId, LocalDate date, double price) {
        // Mettre à jour la date et le prix dans consultation
        String query = "UPDATE consultation SET date = ?, prix = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, java.sql.Date.valueOf(date));
            stmt.setDouble(2, price);
            stmt.setInt(3, consultationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de la consultation : " + e.getMessage());
        }

        // Mettre à jour le statut du rendez-vous correspondant
        String queryRdv = "UPDATE rendez_vous SET statut = 'approuvé' WHERE id = (SELECT rendez_vous_id FROM consultation WHERE id = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(queryRdv)) {
            stmt.setInt(1, consultationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du statut du rendez-vous : " + e.getMessage());
        }
    }
}