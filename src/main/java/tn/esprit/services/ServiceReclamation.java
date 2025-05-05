package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Reclamation;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReclamation implements IService<Reclamation> {

    private Connection cnx;

    public ServiceReclamation() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Reclamation reclamation) {
        String qry = "INSERT INTO reclamation (sujet, description, date_debut, etat, user_id) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, reclamation.getSujet());
            pstm.setString(2, reclamation.getDescription());
            pstm.setDate(3, Date.valueOf(reclamation.getDateDebut()));
            pstm.setString(4, reclamation.getEtat());
            pstm.setInt(5, reclamation.getUserId());
            pstm.executeUpdate();
            System.out.println("Réclamation ajoutée avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @Override
    public List<Reclamation> getAll() {
        List<Reclamation> list = new ArrayList<>();
        String qry = "SELECT * FROM reclamation";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                Reclamation r = new Reclamation();
                r.setId(rs.getInt("id"));
                r.setSujet(rs.getString("sujet"));
                r.setDescription(rs.getString("description"));
                r.setDateDebut(rs.getDate("date_debut").toLocalDate());
                r.setEtat(rs.getString("etat"));
                r.setUserId(rs.getInt("user_id"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération : " + e.getMessage());
        }
        return list;
    }

    public Reclamation getById(int id) {
        String qry = "SELECT * FROM reclamation WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                Reclamation r = new Reclamation();
                r.setId(rs.getInt("id"));
                r.setSujet(rs.getString("sujet"));
                r.setDescription(rs.getString("description"));
                r.setDateDebut(rs.getDate("date_debut").toLocalDate());
                r.setEtat(rs.getString("etat"));
                r.setUserId(rs.getInt("user_id"));
                return r;
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération par ID : " + e.getMessage());
        }
        return null;
    }

    public List<Reclamation> getByEtat(String etat) {
        List<Reclamation> list = new ArrayList<>();
        String qry = "SELECT * FROM reclamation WHERE etat = ? ORDER BY date_debut DESC";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, etat);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                Reclamation r = new Reclamation();
                r.setId(rs.getInt("id"));
                r.setSujet(rs.getString("sujet"));
                r.setDescription(rs.getString("description"));
                r.setDateDebut(rs.getDate("date_debut").toLocalDate());
                r.setEtat(rs.getString("etat"));
                r.setUserId(rs.getInt("user_id"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération par état : " + e.getMessage());
        }
        return list;
    }

    public int getCount() {
        String qry = "SELECT COUNT(*) FROM reclamation";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du comptage total : " + e.getMessage());
        }
        return 0;
    }

    public int getCountByEtat(String etat) {
        String qry = "SELECT COUNT(*) FROM reclamation WHERE etat = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, etat);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du comptage par état : " + e.getMessage());
        }
        return 0;
    }

    @Override
    public void update(Reclamation reclamation) {
        String qry = "UPDATE reclamation SET sujet = ?, description = ?, date_debut = ?, etat = ?, user_id = ? WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, reclamation.getSujet());
            pstm.setString(2, reclamation.getDescription());
            pstm.setDate(3, Date.valueOf(reclamation.getDateDebut()));
            pstm.setString(4, reclamation.getEtat());
            pstm.setInt(5, reclamation.getUserId());
            pstm.setInt(6, reclamation.getId());
            pstm.executeUpdate();
            System.out.println("Réclamation mise à jour avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @Override
    public void delete(Reclamation reclamation) {
        String qry = "DELETE FROM reclamation WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, reclamation.getId());
            pstm.executeUpdate();
            System.out.println("Réclamation supprimée avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }
}