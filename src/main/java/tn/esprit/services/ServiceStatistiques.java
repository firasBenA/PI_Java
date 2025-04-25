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

        String totalQuery = "SELECT COUNT(*) FROM reclamation";
        try (PreparedStatement pstm = cnx.prepareStatement(totalQuery);
             ResultSet rs = pstm.executeQuery()) {
            if (rs.next()) {
                totalCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du comptage total : " + e.getMessage());
        }
//
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

        return new Statistiques(totalCount, enAttenteCount, traiteCount);
    }
}