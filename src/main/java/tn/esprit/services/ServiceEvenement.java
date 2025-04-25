package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Evenement;
import tn.esprit.models.User;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceEvenement implements IService<Evenement> {

    private Connection cnx;

    public ServiceEvenement() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Evenement evenement) {
        String query = "INSERT INTO `evenement`(`nom`, `contenue`, `type`, `statut`, `lieux_event`, `date_event`) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setString(1, evenement.getNom());
            pstm.setString(2, evenement.getContenue());
            pstm.setString(3, evenement.getType());
            pstm.setString(4, evenement.getStatut());
            pstm.setString(5, evenement.getLieuxEvent());
            pstm.setDate(6, Date.valueOf(evenement.getDateEvent()));

            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while adding evenement: " + e.getMessage());
        }
    }

    @Override
    public List<Evenement> getAll() {
        List<Evenement> evenements = new ArrayList<>();

        String query = "SELECT * FROM `evenement`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(query);

            while (rs.next()) {
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setNom(rs.getString("nom"));
                e.setContenue(rs.getString("contenue"));
                e.setType(rs.getString("type"));
                e.setStatut(rs.getString("statut"));
                e.setLieuxEvent(rs.getString("lieux_event"));
                e.setDateEvent(rs.getDate("date_event").toLocalDate());

                evenements.add(e);
            }
        } catch (SQLException e) {
            System.out.println("Error while retrieving evenements: " + e.getMessage());
        }

        return evenements;
    }

    @Override
    public void update(Evenement evenement) {
        String query = "UPDATE `evenement` SET `nom` = ?, `contenue` = ?, `type` = ?, `statut` = ?, " +
                "`lieux_event` = ?, `date_event` = ? WHERE `id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setString(1, evenement.getNom());
            pstm.setString(2, evenement.getContenue());
            pstm.setString(3, evenement.getType());
            pstm.setString(4, evenement.getStatut());
            pstm.setString(5, evenement.getLieuxEvent());
            pstm.setDate(6, Date.valueOf(evenement.getDateEvent()));
            pstm.setInt(7, evenement.getId());

            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while updating evenement: " + e.getMessage());
        }
    }

    @Override
    public void delete(Evenement evenement) {
        String query = "DELETE FROM `evenement` WHERE `id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, evenement.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while deleting evenement: " + e.getMessage());
        }
    }

    // Add a user to an event (participate)
    public void participate(User user, Evenement evenement) {
        String query = "INSERT INTO `user_evenement`(`user_id`, `evenement_id`) VALUES (?, ?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, user.getId());
            pstm.setInt(2, evenement.getId());
            pstm.executeUpdate();
            System.out.println("User " + user.getUsername() + " participated in event " + evenement.getNom());
        } catch (SQLException e) {
            System.out.println("Error while adding participation: " + e.getMessage());
        }
    }

    // Check if a user has already participated in an event
    public boolean hasParticipated(User user, Evenement evenement) {
        String query = "SELECT COUNT(*) FROM `user_evenement` WHERE `user_id` = ? AND `evenement_id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, user.getId());
            pstm.setInt(2, evenement.getId());
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error while checking participation: " + e.getMessage());
        }
        return false;
    }
}