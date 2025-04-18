package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Article;
import tn.esprit.models.Evenement;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceArticle implements IService<Article> {

    private Connection cnx;

    public ServiceArticle() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Article article) {
        String query = "INSERT INTO `article`(`titre`, `contenue`, `image`) VALUES (?, ?, ?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstm.setString(1, article.getTitre());
            pstm.setString(2, article.getContenue());
            pstm.setString(3, article.getImage());

            pstm.executeUpdate();

            // Get the generated article ID
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                article.setId(rs.getInt(1));
            }

        } catch (SQLException e) {
            System.out.println("Error while adding article: " + e.getMessage());
        }
    }

    @Override
    public List<Article> getAll() {
        List<Article> articles = new ArrayList<>();

        String query = "SELECT * FROM `article`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(query);

            while (rs.next()) {
                Article a = new Article();
                a.setId(rs.getInt("id"));
                a.setTitre(rs.getString("titre"));
                a.setContenue(rs.getString("contenue"));
                a.setImage(rs.getString("image"));

                // Load relationships
                loadArticleEvenements(a);
                loadArticleLikes(a);

                articles.add(a);
            }
        } catch (SQLException e) {
            System.out.println("Error while retrieving articles: " + e.getMessage());
        }

        return articles;
    }

    public Article getById(int id) {
        String query = "SELECT * FROM `article` WHERE `id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                Article a = new Article();
                a.setId(rs.getInt("id"));
                a.setTitre(rs.getString("titre"));
                a.setContenue(rs.getString("contenue"));
                a.setImage(rs.getString("image"));

                // Load relationships
                loadArticleEvenements(a);
                loadArticleLikes(a);

                return a;
            }
        } catch (SQLException e) {
            System.out.println("Error while retrieving article by id: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void update(Article article) {
        String query = "UPDATE `article` SET `titre` = ?, `contenue` = ?, `image` = ? WHERE `id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setString(1, article.getTitre());
            pstm.setString(2, article.getContenue());
            pstm.setString(3, article.getImage());
            pstm.setInt(4, article.getId());

            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while updating article: " + e.getMessage());
        }
    }

    @Override
    public void delete(Article article) {
        // First delete from relationship tables
        try {
            // Delete from evenement_article table
            PreparedStatement pstmEvt = cnx.prepareStatement("DELETE FROM `evenement_article` WHERE `article_id` = ?");
            pstmEvt.setInt(1, article.getId());
            pstmEvt.executeUpdate();

            // Delete from article_likes table
            PreparedStatement pstmLikes = cnx.prepareStatement("DELETE FROM `article_likes` WHERE `article_id` = ?");
            pstmLikes.setInt(1, article.getId());
            pstmLikes.executeUpdate();

            // Delete the article
            PreparedStatement pstmArticle = cnx.prepareStatement("DELETE FROM `article` WHERE `id` = ?");
            pstmArticle.setInt(1, article.getId());
            pstmArticle.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while deleting article: " + e.getMessage());
        }
    }

    // Methods to manage relationships

    private void loadArticleEvenements(Article article) {
        String query = "SELECT e.* FROM `evenement` e " +
                "JOIN `evenement_article` ea ON e.id = ea.evenement_id " +
                "WHERE ea.article_id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, article.getId());
            ResultSet rs = pstm.executeQuery();

            ServiceEvenement serviceEvenement = new ServiceEvenement();
            while (rs.next()) {
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setNom(rs.getString("nom"));
                e.setContenue(rs.getString("contenue"));
                e.setType(rs.getString("type"));
                e.setStatut(rs.getString("statut"));
                e.setLieuxEvent(rs.getString("lieux_event"));
                e.setDateEvent(rs.getDate("date_event").toLocalDate());

                article.addEvenement(e);
            }
        } catch (SQLException e) {
            System.out.println("Error loading article evenements: " + e.getMessage());
        }
    }

    private void loadArticleLikes(Article article) {
        String query = "SELECT `user_id` FROM `article_likes` WHERE `article_id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, article.getId());
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                article.addUserLike(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            System.out.println("Error loading article likes: " + e.getMessage());
        }
    }

    public void addArticleToEvenement(Article article, Evenement evenement) {
        String query = "INSERT INTO `evenement_article`(`evenement_id`, `article_id`) VALUES (?, ?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, evenement.getId());
            pstm.setInt(2, article.getId());
            pstm.executeUpdate();

            article.addEvenement(evenement);
            evenement.addArticle(article);
        } catch (SQLException e) {
            System.out.println("Error adding article to evenement: " + e.getMessage());
        }
    }

    public void removeArticleFromEvenement(Article article, Evenement evenement) {
        String query = "DELETE FROM `evenement_article` WHERE `evenement_id` = ? AND `article_id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, evenement.getId());
            pstm.setInt(2, article.getId());
            pstm.executeUpdate();

            article.removeEvenement(evenement);
            evenement.removeArticle(article);
        } catch (SQLException e) {
            System.out.println("Error removing article from evenement: " + e.getMessage());
        }
    }

    public void likeArticle(Article article, int userId) {
        String query = "INSERT INTO `article_likes`(`article_id`, `user_id`) VALUES (?, ?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, article.getId());
            pstm.setInt(2, userId);
            pstm.executeUpdate();

            article.addUserLike(userId);
        } catch (SQLException e) {
            System.out.println("Error liking article: " + e.getMessage());
        }
    }

    public void unlikeArticle(Article article, int userId) {
        String query = "DELETE FROM `article_likes` WHERE `article_id` = ? AND `user_id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, article.getId());
            pstm.setInt(2, userId);
            pstm.executeUpdate();

            article.removeUserLike(userId);
        } catch (SQLException e) {
            System.out.println("Error unliking article: " + e.getMessage());
        }
    }

    public boolean isArticleLikedByUser(Article article, int userId) {
        String query = "SELECT COUNT(*) FROM `article_likes` WHERE `article_id` = ? AND `user_id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, article.getId());
            pstm.setInt(2, userId);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking if article is liked: " + e.getMessage());
        }
        return false;
    }

    public List<Article> getArticlesByEvenement(int evenementId) {
        List<Article> articles = new ArrayList<>();
        String query = "SELECT a.* FROM `article` a " +
                "JOIN `evenement_article` ea ON a.id = ea.article_id " +
                "WHERE ea.evenement_id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, evenementId);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                Article a = new Article();
                a.setId(rs.getInt("id"));
                a.setTitre(rs.getString("titre"));
                a.setContenue(rs.getString("contenue"));
                a.setImage(rs.getString("image"));

                loadArticleLikes(a);
                articles.add(a);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving articles by evenement: " + e.getMessage());
        }
        return articles;
    }
}