package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Article;
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
            PreparedStatement pstm = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstm.setString(1, evenement.getNom());
            pstm.setString(2, evenement.getContenue());
            pstm.setString(3, evenement.getType());
            pstm.setString(4, evenement.getStatut());
            pstm.setString(5, evenement.getLieuxEvent());
            pstm.setDate(6, Date.valueOf(evenement.getDateEvent()));

            pstm.executeUpdate();

            // Get the generated evenement ID
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                evenement.setId(rs.getInt(1));
            }
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

                // Load relationships
                loadEvenementArticles(e);
                loadEvenementParticipants(e);

                evenements.add(e);
            }
        } catch (SQLException e) {
            System.out.println("Error while retrieving evenements: " + e.getMessage());
        }

        return evenements;
    }

    public Evenement getById(int id) {
        String query = "SELECT * FROM `evenement` WHERE `id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setNom(rs.getString("nom"));
                e.setContenue(rs.getString("contenue"));
                e.setType(rs.getString("type"));
                e.setStatut(rs.getString("statut"));
                e.setLieuxEvent(rs.getString("lieux_event"));
                e.setDateEvent(rs.getDate("date_event").toLocalDate());

                // Load relationships
                loadEvenementArticles(e);
                loadEvenementParticipants(e);

                return e;
            }
        } catch (SQLException e) {
            System.out.println("Error while retrieving evenement by id: " + e.getMessage());
        }
        return null;
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
        // First delete from relationship tables
        try {
            // Delete from evenement_article table
            PreparedStatement pstmArticles = cnx.prepareStatement("DELETE FROM `evenement_article` WHERE `evenement_id` = ?");
            pstmArticles.setInt(1, evenement.getId());
            pstmArticles.executeUpdate();

            // Delete from user_evenement table
            PreparedStatement pstmUsers = cnx.prepareStatement("DELETE FROM `user_evenement` WHERE `evenement_id` = ?");
            pstmUsers.setInt(1, evenement.getId());
            pstmUsers.executeUpdate();

            // Delete the evenement
            PreparedStatement pstmEvenement = cnx.prepareStatement("DELETE FROM `evenement` WHERE `id` = ?");
            pstmEvenement.setInt(1, evenement.getId());
            pstmEvenement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while deleting evenement: " + e.getMessage());
        }
    }

    // Methods to manage relationships

    private void loadEvenementArticles(Evenement evenement) {
        String query = "SELECT a.* FROM `article` a " +
                "JOIN `evenement_article` ea ON a.id = ea.article_id " +
                "WHERE ea.evenement_id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, evenement.getId());
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                Article a = new Article();
                a.setId(rs.getInt("id"));
                a.setTitre(rs.getString("titre"));
                a.setContenue(rs.getString("contenue"));
                a.setImage(rs.getString("image"));

                evenement.addArticle(a);
            }
        } catch (SQLException e) {
            System.out.println("Error loading evenement articles: " + e.getMessage());
        }
    }

    private void loadEvenementParticipants(Evenement evenement) {
        String query = "SELECT `user_id` FROM `user_evenement` WHERE `evenement_id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, evenement.getId());
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                evenement.addParticipant(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            System.out.println("Error loading evenement participants: " + e.getMessage());
        }
    }

    public void addArticleToEvenement(Evenement evenement, Article article) {
        String query = "INSERT INTO `evenement_article`(`evenement_id`, `article_id`) VALUES (?, ?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, evenement.getId());
            pstm.setInt(2, article.getId());
            pstm.executeUpdate();

            evenement.addArticle(article);
            article.addEvenement(evenement);
        } catch (SQLException e) {
            System.out.println("Error adding article to evenement: " + e.getMessage());
        }
    }

    public void removeArticleFromEvenement(Evenement evenement, Article article) {
        String query = "DELETE FROM `evenement_article` WHERE `evenement_id` = ? AND `article_id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, evenement.getId());
            pstm.setInt(2, article.getId());
            pstm.executeUpdate();

            evenement.removeArticle(article);
            article.removeEvenement(evenement);
        } catch (SQLException e) {
            System.out.println("Error removing article from evenement: " + e.getMessage());
        }
    }

    public void addParticipantToEvenement(Evenement evenement, int userId) {
        String query = "INSERT INTO `user_evenement`(`evenement_id`, `user_id`) VALUES (?, ?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, evenement.getId());
            pstm.setInt(2, userId);
            pstm.executeUpdate();

            evenement.addParticipant(userId);
        } catch (SQLException e) {
            System.out.println("Error adding participant to evenement: " + e.getMessage());
        }
    }

    public void removeParticipantFromEvenement(Evenement evenement, int userId) {
        String query = "DELETE FROM `user_evenement` WHERE `evenement_id` = ? AND `user_id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, evenement.getId());
            pstm.setInt(2, userId);
            pstm.executeUpdate();

            evenement.removeParticipant(userId);
        } catch (SQLException e) {
            System.out.println("Error removing participant from evenement: " + e.getMessage());
        }
    }

    public boolean isUserParticipatingInEvenement(Evenement evenement, int userId) {
        String query = "SELECT COUNT(*) FROM `user_evenement` WHERE `evenement_id` = ? AND `user_id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, evenement.getId());
            pstm.setInt(2, userId);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking if user is participating: " + e.getMessage());
        }
        return false;
    }

    public List<Evenement> getEvenementsByUser(int userId) {
        List<Evenement> evenements = new ArrayList<>();
        String query = "SELECT e.* FROM `evenement` e " +
                "JOIN `user_evenement` ue ON e.id = ue.evenement_id " +
                "WHERE ue.user_id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, userId);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setNom(rs.getString("nom"));
                e.setContenue(rs.getString("contenue"));
                e.setType(rs.getString("type"));
                e.setStatut(rs.getString("statut"));
                e.setLieuxEvent(rs.getString("lieux_event"));
                e.setDateEvent(rs.getDate("date_event").toLocalDate());

                // Load relationships
                loadEvenementArticles(e);
                loadEvenementParticipants(e);
                evenements.add(e);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving evenements by user: " + e.getMessage());
        }
        return evenements;
    }
}