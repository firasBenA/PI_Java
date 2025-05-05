package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Reponse;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReponse implements IService<Reponse> {

    private Connection cnx;

    public ServiceReponse() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Reponse reponse) {
        String qry = "INSERT INTO reponse (contenu, date_de_reponse, reclamation_id, rating) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, reponse.getContenu());
            pstm.setDate(2, Date.valueOf(reponse.getDateReponse()));
            pstm.setInt(3, reponse.getReclamationId());
            pstm.setObject(4, reponse.getRating());
            pstm.executeUpdate();
            System.out.println("Réponse ajoutée avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @Override
    public List<Reponse> getAll() {
        List<Reponse> list = new ArrayList<>();
        String qry = "SELECT * FROM reponse";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                Reponse r = new Reponse(
                        rs.getInt("id"),
                        rs.getString("contenu"),
                        rs.getDate("date_de_reponse").toLocalDate(),
                        rs.getInt("reclamation_id"),
                        rs.getObject("rating") != null ? rs.getInt("rating") : null
                );
                list.add(r);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération : " + e.getMessage());
        }
        return list;
    }

    @Override
    public void update(Reponse reponse) {
        String qry = "UPDATE reponse SET contenu = ?, date_de_reponse = ?, reclamation_id = ?, rating = ? WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, reponse.getContenu());
            pstm.setDate(2, Date.valueOf(reponse.getDateReponse()));
            pstm.setInt(3, reponse.getReclamationId());
            pstm.setObject(4, reponse.getRating());
            pstm.setInt(5, reponse.getId());
            pstm.executeUpdate();
            System.out.println("Réponse mise à jour avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    public List<Reponse> getByReclamationId(int reclamationId) {
        List<Reponse> list = new ArrayList<>();
        String qry = "SELECT * FROM reponse WHERE reclamation_id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, reclamationId);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                Reponse r = new Reponse(
                        rs.getInt("id"),
                        rs.getString("contenu"),
                        rs.getDate("date_de_reponse").toLocalDate(),
                        rs.getInt("reclamation_id"),
                        rs.getObject("rating") != null ? rs.getInt("rating") : null
                );
                list.add(r);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération par reclamation_id : " + e.getMessage());
        }
        return list;
    }

    public void setRatingForResponse(int responseId, int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        String qry = "UPDATE reponse SET rating = ? WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, rating);
            pstm.setInt(2, responseId);
            int rowsAffected = pstm.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Note de " + rating + " attribuée à la réponse ID " + responseId + " avec succès !");
            } else {
                System.out.println("Aucune réponse trouvée avec l'ID " + responseId);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'attribution de la note : " + e.getMessage());
        }
    }

    @Override
    public void delete(Reponse reponse) {
        String qry = "DELETE FROM reponse WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, reponse.getId());
            pstm.executeUpdate();
            System.out.println("Réponse supprimée avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }
}