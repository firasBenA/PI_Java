package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Evenement;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
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
}