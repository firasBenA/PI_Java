package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Consultation;
import tn.esprit.models.RendeVous;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
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

            // 🔹 Récupérer l'ID du patient connecté dynamiquement (changer pour ID 11)
            int patientId = 10; // Hardcoded patient ID

            // 1. Insérer le rendez-vous
            String rdvQuery = "INSERT INTO `rendez_vous` ( `date`, `statut`, `type_rdv`, `cause`,`user_id`) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstRdv = cnx.prepareStatement(rdvQuery, Statement.RETURN_GENERATED_KEYS)) {
                pstRdv.setDate(1, Date.valueOf(rdv.getDate()));
                pstRdv.setString(2, rdv.getStatut());
                pstRdv.setString(3, rdv.getType());
                pstRdv.setString(4, rdv.getCause());
                pstRdv.setInt(5, 11);

                int affectedRows = pstRdv.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Échec de l'insertion du rendez-vous");
                }

                try (ResultSet generatedKeys = pstRdv.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        rdv.setId(generatedKeys.getInt(1));
                        rdv.setIdPatient(patientId); // pour cohérence dans le modèle
                    } else {
                        throw new SQLException("Échec de la récupération de l'ID du rendez-vous");
                    }
                }
            }

            // 2. Créer la consultation associée
            Consultation consultation = new Consultation();
            consultation.setRendez_vous_id(rdv.getId());
            consultation.setPatient_id(patientId); // <- Remplacer patientId par l'ID utilisateur
            consultation.setMedecin_id(rdv.getIdMedecin());
            consultation.setDate(rdv.getDate());
            consultation.setPrix(0); // Prix par défaut
            consultation.setType_consultation(rdv.getType());

// Si le patient est stocké dans la table "user", utilisez l'ID de l'utilisateur (assurez-vous que `patientId` correspond bien à `user.id`)
            String consultationQuery = "INSERT INTO `consultation` (`rendez_vous_id`, `date`, `prix`, `type_consultation`, `user_id`) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstConsult = cnx.prepareStatement(consultationQuery)) {
                pstConsult.setInt(1, consultation.getRendez_vous_id());
                pstConsult.setDate(2, Date.valueOf(consultation.getDate()));
                pstConsult.setDouble(3, consultation.getPrix());
                pstConsult.setString(4, consultation.getType_consultation());
                pstConsult.setInt(5, 11);

                int consultAffected = pstConsult.executeUpdate();
                if (consultAffected == 0) {
                    throw new SQLException("Échec de la création de la consultation");
                }
            }

            cnx.commit();
            System.out.println("Rendez-vous et consultation créés avec succès. ID RDV: " + rdv.getId());

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
        String query = "UPDATE `rendez_vous` SET  `user_id`=?, `date`=?, `statut`=?, `type_rdv`=?, `cause`=? WHERE `id`=?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, 11);
            pst.setDate(2, Date.valueOf(rdv.getDate()));
            pst.setString(3, rdv.getStatut());
            pst.setString(4, rdv.getType());
            pst.setString(5, rdv.getCause());
            pst.setInt(6, rdv.getId());

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

            String deleteConsultQuery = "DELETE FROM `consultation` WHERE `rendez_vous_id`=?";
            try (PreparedStatement pstConsult = cnx.prepareStatement(deleteConsultQuery)) {
                pstConsult.setInt(1, rdv.getId());
                pstConsult.executeUpdate();
            }

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
        String query = "SELECT `id` FROM `rendez_vous` WHERE `user_id`=?";

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
        rdv.setUserId(rs.getInt(11));
        rdv.setDate(rs.getDate("date").toLocalDate());
        rdv.setStatut(rs.getString("statut"));
        rdv.setType(rs.getString("type_rdv"));
        rdv.setCause(rs.getString("cause"));
        return rdv;
    }
}
