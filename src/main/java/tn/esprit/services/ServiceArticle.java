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
            PreparedStatement pstm = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstm.setString(1, article.getTitre());
            pstm.setString(2, article.getContenue());
            pstm.setString(3, article.getImage());

            pstm.executeUpdate();

            // Get the generated article ID
            ResultSet generatedKeys = pstm.getGeneratedKeys();
            if (generatedKeys.next()) {
                article.setId(generatedKeys.getInt(1));

                // Insert relationships into junction table
                if (!article.getEvenements().isEmpty()) {
                    saveArticleEvenementRelationships(article);
                }
            }

            System.out.println("Article added successfully with ID: " + article.getId());
        } catch (SQLException e) {
            System.out.println("Error while adding article: " + e.getMessage());
            e.printStackTrace();
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

                // Load associated events for each article
                loadArticleEvenements(a);

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

            // Update relationships in junction table
            // First, remove existing relationships
            deleteArticleEvenementRelationships(article);

            // Then add current relationships
            if (!article.getEvenements().isEmpty()) {
                saveArticleEvenementRelationships(article);
            }

            System.out.println("Article updated successfully: " + article.getId());
        } catch (SQLException e) {
            System.out.println("Error while updating article: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Article article) {
        // First, delete relationships from junction table
        deleteArticleEvenementRelationships(article);

        // Then, delete the article itself
        String query = "DELETE FROM `article` WHERE `id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, article.getId());
            pstm.executeUpdate();
            System.out.println("Article deleted successfully: " + article.getId());
        } catch (SQLException e) {
            System.out.println("Error while deleting article: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to save article-evenement relationships
    private void saveArticleEvenementRelationships(Article article) {
        String query = "INSERT INTO `evenement_article` (`evenement_id`, `article_id`) VALUES (?, ?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            for (Evenement evenement : article.getEvenements()) {
                pstm.setInt(1, evenement.getId());
                pstm.setInt(2, article.getId());
                pstm.executeUpdate();
                System.out.println("Added relationship: Article " + article.getId() + " with Event " + evenement.getId());
            }
        } catch (SQLException e) {
            System.out.println("Error while saving article-evenement relationships: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to delete article-evenement relationships
    private void deleteArticleEvenementRelationships(Article article) {
        String query = "DELETE FROM `evenement_article` WHERE `article_id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, article.getId());
            pstm.executeUpdate();
            System.out.println("Deleted existing relationships for article: " + article.getId());
        } catch (SQLException e) {
            System.out.println("Error while deleting article-evenement relationships: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to load an article's associated events
    private void loadArticleEvenements(Article article) {
        String query = "SELECT e.* FROM `evenement` e " +
                "JOIN `evenement_article` ea ON e.id = ea.evenement_id " +
                "WHERE ea.article_id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, article.getId());
            ResultSet rs = pstm.executeQuery();

            List<Evenement> evenements = new ArrayList<>();
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

                evenements.add(e);
            }

            article.setEvenements(evenements);
        } catch (SQLException e) {
            System.out.println("Error while loading article events: " + e.getMessage());
            e.printStackTrace();
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

        // Debug log to track if the method is called with a valid event
        System.out.println("Getting related articles for event ID: " + evenement.getId() + ", Name: " + evenement.getNom());

        String query = "SELECT a.* FROM `article` a " +
                "JOIN `evenement_article` ea ON a.id = ea.article_id " +
                "WHERE ea.evenement_id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(query);
            pstm.setInt(1, evenement.getId());
            ResultSet rs = pstm.executeQuery();

            System.out.println("Query executed: " + query.replace("?", String.valueOf(evenement.getId())));

            while (rs.next()) {
                Article a = new Article();
                a.setId(rs.getInt("id"));
                a.setTitre(rs.getString("titre"));
                a.setContenue(rs.getString("contenue"));
                a.setImage(rs.getString("image"));
                relatedArticles.add(a);
                System.out.println("Found related article: " + a.getTitre() + " (ID: " + a.getId() + ")");
            }

            System.out.println("Total related articles found: " + relatedArticles.size());
        } catch (SQLException e) {
            System.out.println("Error while retrieving related articles: " + e.getMessage());
            e.printStackTrace();
        }
        return relatedArticles;
    }
}