package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Article;
import tn.esprit.models.Evenement;
import tn.esprit.models.User;
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
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setString(1, article.getTitre());
            pstm.setString(2, article.getContenue());
            pstm.setString(3, article.getImage());

            pstm.executeUpdate();
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

                articles.add(a);
            }
        } catch (SQLException e) {
            System.out.println("Error while retrieving articles: " + e.getMessage());
        }

        return articles;
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
        String query = "DELETE FROM `article` WHERE `id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, article.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while deleting article: " + e.getMessage());
        }
    }

    // Add a like to an article
    public void like(User user, Article article) {
        if (!hasLiked(user, article)) { // Prevent duplicate likes
            String query = "INSERT INTO `article_likes`(`user_id`, `article_id`) VALUES (?, ?)";
            try (PreparedStatement pstm = cnx.prepareStatement(query)) {
                pstm.setInt(1, user.getId());
                pstm.setInt(2, article.getId());
                pstm.executeUpdate();
                System.out.println("User " + user.getPrenom() + " liked article " + article.getTitre());
            } catch (SQLException e) {
                System.err.println("Error while adding like: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("User " + user.getPrenom() + " has already liked article " + article.getTitre());
        }
    }

    public boolean hasLiked(User user, Article article) {
        String query = "SELECT COUNT(*) FROM `article_likes` WHERE `user_id` = ? AND `article_id` = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setInt(1, user.getId());
            pstm.setInt(2, article.getId());
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error while checking like: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Get articles related to an event (via evenement_article relationship)
    public List<Article> getRelatedArticles(Evenement evenement) {
        List<Article> relatedArticles = new ArrayList<>();
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
                relatedArticles.add(a);
            }
        } catch (SQLException e) {
            System.out.println("Error while retrieving related articles: " + e.getMessage());
        }
        return relatedArticles;
    }
}