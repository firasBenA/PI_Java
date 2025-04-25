package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Reclamation;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceReclamation implements IService<Reclamation> {

    private final Connection cnx;

    public ServiceReclamation() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Reclamation reclamation) {
        String qry = "INSERT INTO reclamation(sujet, description, date_debut, etat, user_id) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, reclamation.getSujet());
            pstm.setString(2, reclamation.getDescription());

            // Log the dateDebut to confirm its value
            System.out.println("ServiceReclamation - dateDebut before saving: " + reclamation.getDateDebut());

            // Safety check for dateDebut
            LocalDate dateDebut = reclamation.getDateDebut();
            if (dateDebut == null) {
                dateDebut = LocalDate.now(); // Fallback to current date if null
                reclamation.setDateDebut(dateDebut);
                System.out.println("ServiceReclamation - dateDebut was null, set to: " + dateDebut);
            }
            pstm.setDate(3, Date.valueOf(dateDebut));

            // etat is guaranteed to be "EnAttente" by the Reclamation constructor
            pstm.setString(4, reclamation.getEtat());
            pstm.setInt(5, reclamation.getUserId());
            pstm.executeUpdate();
            System.out.println("Réclamation ajoutée avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout : " + e.getMessage());
            throw new RuntimeException(e); // Re-throw the exception to be caught by the controller
        }
    }

    @Override
    public List<Reclamation> getAll() {
        List<Reclamation> list = new ArrayList<>();
        String qry = "SELECT * FROM reclamation order by date_debut desc";
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
                list.add(r);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération : " + e.getMessage());
        }
        return list;
    }

    public Reclamation getById(int id) {
        Reclamation reclamation = null;
        String qry = "SELECT * FROM reclamation WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                reclamation = new Reclamation();
                reclamation.setId(rs.getInt("id"));
                reclamation.setSujet(rs.getString("sujet"));
                reclamation.setDescription(rs.getString("description"));
                reclamation.setDateDebut(rs.getDate("date_debut").toLocalDate());
                reclamation.setEtat(rs.getString("etat"));
                reclamation.setUserId(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération par ID : " + e.getMessage());
        }
        return reclamation;
    }

    @Override
    public void update(Reclamation reclamation) {
        String qry = "UPDATE reclamation SET sujet = ?, description = ?, date_debut = ?, etat = ? WHERE id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, reclamation.getSujet());
            pstm.setString(2, reclamation.getDescription());
            pstm.setDate(3, Date.valueOf(reclamation.getDateDebut()));
            pstm.setString(4, reclamation.getEtat());
            pstm.setInt(5, reclamation.getId());
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
}