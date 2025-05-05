package tn.esprit.services;

import tn.esprit.models.Statistiques;
import tn.esprit.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ServiceStatistiques {

    private Connection cnx;

    public ServiceStatistiques() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public Statistiques getReclamationStatistics() {
        int totalCount = 0;
        int enAttenteCount = 0;
        int traiteCount = 0;
        int highSatisfactionCount = 0;
        int moderateSatisfactionCount = 0;
        int lowSatisfactionCount = 0;
        int unratedCount = 0;

        // Query total reclamations
        String totalQuery = "SELECT COUNT(*) FROM reclamation";
        try (PreparedStatement pstm = cnx.prepareStatement(totalQuery);
             ResultSet rs = pstm.executeQuery()) {
            if (rs.next()) {
                totalCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du comptage total : " + e.getMessage());
        }

        // Query reclamations in "En Attente" status
        String enAttenteQuery = "SELECT COUNT(*) FROM reclamation WHERE etat = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(enAttenteQuery)) {
            pstm.setString(1, "En Attente");
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                enAttenteCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du comptage En Attente : " + e.getMessage());
        }

        // Query reclamations in "Traité" status
        String traiteQuery = "SELECT COUNT(*) FROM reclamation WHERE etat = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(traiteQuery)) {
            pstm.setString(1, "Traité");
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                traiteCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du comptage Traité : " + e.getMessage());
        }

        // Query responses by rating groups
        String ratingQuery = "SELECT CASE " +
                "WHEN rating = 1 THEN 'low' " +
                "WHEN rating IN (2, 3) THEN 'moderate' " +
                "WHEN rating IN (4, 5) THEN 'high' " +
                "END AS rating_group, COUNT(*) " +
                "FROM reponse WHERE rating IS NOT NULL " +
                "GROUP BY rating_group";
        try (PreparedStatement pstm = cnx.prepareStatement(ratingQuery);
             ResultSet rs = pstm.executeQuery()) {
            while (rs.next()) {
                String group = rs.getString("rating_group");
                int count = rs.getInt(2);
                switch (group) {
                    case "high":
                        highSatisfactionCount = count;
                        break;
                    case "moderate":
                        moderateSatisfactionCount = count;
                        break;
                    case "low":
                        lowSatisfactionCount = count;
                        break;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du comptage des évaluations : " + e.getMessage());
        }

        // Query unrated responses
        String unratedQuery = "SELECT COUNT(*) FROM reponse WHERE rating IS NULL";
        try (PreparedStatement pstm = cnx.prepareStatement(unratedQuery);
             ResultSet rs = pstm.executeQuery()) {
            if (rs.next()) {
                unratedCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du comptage des réponses non notées : " + e.getMessage());
        }

        return new Statistiques(totalCount, enAttenteCount, traiteCount,
                highSatisfactionCount, moderateSatisfactionCount,
                lowSatisfactionCount, unratedCount);
    }
}