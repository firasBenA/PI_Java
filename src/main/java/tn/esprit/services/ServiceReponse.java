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
            String qry = "INSERT INTO reponse(contenu, date_de_reponse, reclamation_id) VALUES (?, ?, ?)";
            try {
                PreparedStatement pstm = cnx.prepareStatement(qry);
                pstm.setString(1, reponse.getContenu());
                pstm.setDate(2, Date.valueOf(reponse.getDateReponse()));
                pstm.setInt(3, reponse.getReclamationId());
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
                    Reponse r = new Reponse();
                    r.setId(rs.getInt("id"));
                    r.setContenu(rs.getString("contenu"));
                    r.setDateReponse(rs.getDate("date_de_reponse").toLocalDate());
                    r.setReclamationId(rs.getInt("reclamation_id"));
                    list.add(r);
                }
            } catch (SQLException e) {
                System.out.println("Erreur lors de la récupération : " + e.getMessage());
            }
            return list;
        }

        @Override
        public void update(Reponse reponse) {
            String qry = "UPDATE reponse SET contenu = ?, date_de_reponse = ?, reclamation_id = ? WHERE id = ?";
            try {
                PreparedStatement pstm = cnx.prepareStatement(qry);
                pstm.setString(1, reponse.getContenu());
                pstm.setDate(2, Date.valueOf(reponse.getDateReponse()));
                pstm.setInt(3, reponse.getReclamationId());
                pstm.setInt(4, reponse.getId());
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
                    Reponse r = new Reponse();
                    r.setId(rs.getInt("id"));
                    r.setContenu(rs.getString("contenu"));
                    r.setDateReponse(rs.getDate("date_de_reponse").toLocalDate());
                    r.setReclamationId(rs.getInt("reclamation_id"));
                    list.add(r);
                }
            } catch (SQLException e) {
                System.out.println("Erreur lors de la récupération par reclamation_id : " + e.getMessage());
            }
            return list;
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


