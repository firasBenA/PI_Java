package tn.esprit.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import tn.esprit.interfaces.IService;
import tn.esprit.models.Consultation;
import tn.esprit.models.RendeVous;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceAddRdv implements IService<RendeVous> {
    private final Connection cnx;

    public ServiceAddRdv() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(RendeVous rdv) {
        boolean autoCommit = false;
        try {
            autoCommit = cnx.getAutoCommit();
            cnx.setAutoCommit(false);

            // 1. Insérer le rendez-vous
            String rdvQuery = "INSERT INTO `rendez_vous`(`patient_id`, `medecin_id`, `date`, `statut`, `type_rdv`, `cause`) VALUES (?,?,?,?,?,?)";

            try (PreparedStatement pstRdv = cnx.prepareStatement(rdvQuery, Statement.RETURN_GENERATED_KEYS)) {
                pstRdv.setInt(1, rdv.getIdPatient());
                pstRdv.setInt(2, rdv.getIdMedecin());
                pstRdv.setDate(3, Date.valueOf(rdv.getDate()));
                pstRdv.setString(4, rdv.getStatut());
                pstRdv.setString(5, rdv.getType());
                pstRdv.setString(6, rdv.getCause());

                int affectedRows = pstRdv.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Échec de l'insertion du rendez-vous");
                }

                // Récupérer l'ID généré
                try (ResultSet generatedKeys = pstRdv.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        rdv.setId(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Échec de la récupération de l'ID du rendez-vous");
                    }
                }
            }

            // 2. Créer la consultation associée
            Consultation consultation = new Consultation();
            consultation.setRendez_vous_id(rdv.getId());
            consultation.setPatient_id(rdv.getIdPatient());
            consultation.setMedecin_id(rdv.getIdMedecin());
            consultation.setDate(rdv.getDate());
            consultation.setPrix(0); // Prix par défaut
            consultation.setType_consultation(rdv.getType());
            consultation.setUser_id(rdv.getIdPatient()); // Utiliser patient_id comme user_id

            String consultationQuery = "INSERT INTO `consultation`(`rendez_vous_id`, `patient_id`, `medecin_id`, `date`, `prix`, `type_consultation`, `user_id`) VALUES (?,?,?,?,?,?,?)";

            try (PreparedStatement pstConsult = cnx.prepareStatement(consultationQuery)) {
                pstConsult.setInt(1, consultation.getRendez_vous_id());
                pstConsult.setInt(2, consultation.getPatient_id());
                pstConsult.setInt(3, consultation.getMedecin_id());
                pstConsult.setDate(4, Date.valueOf(consultation.getDate()));
                pstConsult.setDouble(5, consultation.getPrix());
                pstConsult.setString(6, consultation.getType_consultation());
                pstConsult.setInt(7, consultation.getUser_id());

                int consultAffected = pstConsult.executeUpdate();
                if (consultAffected == 0) {
                    throw new SQLException("Échec de la création de la consultation");
                }
            }

            // Valider la transaction
            cnx.commit();
            System.out.println("Rendez-vous et consultation créés avec succès. ID RDV: " + rdv.getId());

            // 3. Envoyer une notification FCM au patient
            String patientFcmToken = getUserFcmToken(rdv.getIdPatient());
            if (patientFcmToken != null) {
                sendRdvAddedNotification(patientFcmToken, rdv.getDate().toString(), rdv.getType());
            }

            // 4. Envoyer une notification FCM au médecin
            String doctorFcmToken = getUserFcmToken(rdv.getIdMedecin());
            if (doctorFcmToken != null) {
                sendNewConsultationNotification(doctorFcmToken, rdv.getDate().toString(), rdv.getType());
            }

        } catch (SQLException e) {
            try {
                cnx.rollback();
                System.err.println("Rollback effectué: " + e.getMessage());
            } catch (SQLException ex) {
                System.err.println("Erreur lors du rollback: " + ex.getMessage());
            }
            throw new RuntimeException("Erreur lors de l'ajout du rendez-vous", e);
        } finally {
            try {
                cnx.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                System.err.println("Erreur lors du rétablissement de l'auto-commit: " + e.getMessage());
            }
        }
    }

    @Override
    public List<RendeVous> getAll() {
        List<RendeVous> rendezVousList = new ArrayList<>();
        String query = "SELECT * FROM `rendez_vous`";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                RendeVous rdv = mapResultSetToRendezVous(rs);
                rendezVousList.add(rdv);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des rendez-vous: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return rendezVousList;
    }

    @Override
    public void update(RendeVous rdv) {
        String query = "UPDATE `rendez_vous` SET `patient_id`=?, `medecin_id`=?, `date`=?, `statut`=?, `type_rdv`=?, `cause`=? WHERE `id`=?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, rdv.getIdPatient());
            pst.setInt(2, rdv.getIdMedecin());
            pst.setDate(3, Date.valueOf(rdv.getDate()));
            pst.setString(4, rdv.getStatut());
            pst.setString(5, rdv.getType());
            pst.setString(6, rdv.getCause());
            pst.setInt(7, rdv.getId());

            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucun rendez-vous trouvé avec l'ID: " + rdv.getId());
            }
            System.out.println("Rendez-vous mis à jour avec succès. ID: " + rdv.getId());
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du rendez-vous: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(RendeVous rdv) {
        boolean autoCommit = false;
        try {
            autoCommit = cnx.getAutoCommit();
            cnx.setAutoCommit(false);

            // 1. D'abord supprimer la consultation associée
            String deleteConsultQuery = "DELETE FROM `consultation` WHERE `rendez_vous_id`=?";
            try (PreparedStatement pstConsult = cnx.prepareStatement(deleteConsultQuery)) {
                pstConsult.setInt(1, rdv.getId());
                pstConsult.executeUpdate();
            }

            // 2. Ensuite supprimer le rendez-vous
            String deleteRdvQuery = "DELETE FROM `rendez_vous` WHERE `id`=?";
            try (PreparedStatement pstRdv = cnx.prepareStatement(deleteRdvQuery)) {
                pstRdv.setInt(1, rdv.getId());
                int affectedRows = pstRdv.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Aucun rendez-vous trouvé avec l'ID: " + rdv.getId());
                }
            }

            cnx.commit();
            System.out.println("Rendez-vous et consultation supprimés avec succès. ID: " + rdv.getId());

        } catch (SQLException e) {
            try {
                cnx.rollback();
                System.err.println("Rollback effectué: " + e.getMessage());
            } catch (SQLException ex) {
                System.err.println("Erreur lors du rollback: " + ex.getMessage());
            }
            throw new RuntimeException("Erreur lors de la suppression du rendez-vous", e);
        } finally {
            try {
                cnx.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                System.err.println("Erreur lors du rétablissement de l'auto-commit: " + e.getMessage());
            }
        }
    }

    public List<RendeVous> findByPatientId(int patientId) {
        List<RendeVous> rendezVousList = new ArrayList<>();
        String query = "SELECT * FROM `rendez_vous` WHERE `patient_id`=?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, patientId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    RendeVous rdv = mapResultSetToRendezVous(rs);
                    rendezVousList.add(rdv);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des RDV du patient: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return rendezVousList;
    }

    private RendeVous mapResultSetToRendezVous(ResultSet rs) throws SQLException {
        RendeVous rdv = new RendeVous();
        rdv.setId(rs.getInt("id"));
        rdv.setIdPatient(rs.getInt("patient_id"));
        rdv.setIdMedecin(rs.getInt("medecin_id"));
        rdv.setDate(rs.getDate("date").toLocalDate());
        rdv.setStatut(rs.getString("statut"));
        rdv.setType(rs.getString("type_rdv"));
        rdv.setCause(rs.getString("cause"));
        return rdv;
    }

    private String getUserFcmToken(int userId) throws SQLException {
        String query = "SELECT fcm_token FROM user WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String token = rs.getString("fcm_token");
                if (token == null || token.isEmpty()) {
                    System.err.println("FCM token not found for user ID: " + userId);
                    return null;
                }
                return token;
            }
            throw new SQLException("User not found with ID: " + userId);
        }
    }

    private void sendRdvAddedNotification(String fcmToken, String date, String typeConsultation) {
        try {
            Notification notification = Notification.builder()
                    .setTitle("Rendez-vous ajouté avec succès")
                    .setBody(String.format("Votre rendez-vous a été ajouté.\nDate: %s\nType: %s", date, typeConsultation))
                    .build();

            Message message = Message.builder()
                    .setNotification(notification)
                    .setToken(fcmToken)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent RDV added notification: " + response);
        } catch (Exception e) {
            System.err.println("Failed to send RDV added notification: " + e.getMessage());
        }
    }

    private void sendNewConsultationNotification(String fcmToken, String date, String typeConsultation) {
        try {
            Notification notification = Notification.builder()
                    .setTitle("Nouvelle Consultation à Examiner")
                    .setBody(String.format("Une nouvelle consultation a été ajoutée.\nDate: %s\nType: %s", date, typeConsultation))
                    .build();

            Message message = Message.builder()
                    .setNotification(notification)
                    .setToken(fcmToken)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent new consultation notification: " + response);
        } catch (Exception e) {
            System.err.println("Failed to send new consultation notification: " + e.getMessage());
        }
    }
}