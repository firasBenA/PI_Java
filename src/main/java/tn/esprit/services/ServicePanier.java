package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Panier;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePanier implements IService<Panier> {

    private Connection cnx;

    public ServicePanier() {
        cnx = MyDataBase.getInstance().getCnx();
    }
    public Panier getOrCreatePanier(int userId) throws Exception {
        // Try to find an existing panier for the user
        String selectQuery = "SELECT * FROM panier WHERE user_id_id = ?";
        PreparedStatement selectStmt = cnx.prepareStatement(selectQuery);
        selectStmt.setInt(1, userId);

        ResultSet rs = selectStmt.executeQuery();
        if (rs.next()) {
            // Panier exists
            Panier panier = new Panier();
            panier.setId(rs.getInt("id"));
            panier.setUserId(rs.getInt("user_id_id"));
            return panier;
        } else {
            // No panier found -> Create one
            String insertQuery = "INSERT INTO panier (user_id_id, cree_le, maj_le) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = cnx.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            Timestamp now = new Timestamp(System.currentTimeMillis());
            insertStmt.setInt(1, userId);
            insertStmt.setTimestamp(2, now); // cree_le
            insertStmt.setTimestamp(3, now);
            insertStmt.executeUpdate();

            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                Panier panier = new Panier();
                panier.setId(generatedKeys.getInt(1));
                panier.setUserId(userId);
                return panier;
            } else {
                throw new Exception("Erreur lors de la création du panier");
            }
        }
    }

    public void addProduitToPanier(int panierId, int produitId) throws SQLException {
        Connection conn = MyDataBase.getInstance().getCnx();

        // Check if product already exists in panier
        String checkSql = "SELECT COUNT(*) FROM panier_produit WHERE panier_id = ? AND produit_id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, panierId);
            checkStmt.setInt(2, produitId);

            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // Product already exists, don't insert again
                System.out.println("Produit déjà dans le panier.");
                return;
            }
        }

        // Insert new product
        String insertSql = "INSERT INTO panier_produit (panier_id, produit_id) VALUES (?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setInt(1, panierId);
            insertStmt.setInt(2, produitId);
            insertStmt.executeUpdate();
        }
    }

    @Override
    public void add(Panier panier) {
        String sql = "INSERT INTO panier (user_id_id, cree_le, maj_le) VALUES (?, ?, ?)";
        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setInt(1, panier.getUserId());
            stmt.setTimestamp(2, panier.getCreeLe());
            stmt.setTimestamp(3, panier.getMajLe());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while adding panier: " + e.getMessage());
        }
    }

    @Override
    public List<Panier> getAll() {
        List<Panier> paniers = new ArrayList<>();
        String sql = "SELECT * FROM panier";
        try {
            Statement stmt = cnx.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Panier panier = new Panier(
                        rs.getInt("id"),
                        rs.getInt("user_id_id"),
                        rs.getTimestamp("cree_le"),
                        rs.getTimestamp("maj_le")
                );
                paniers.add(panier);
            }
        } catch (SQLException e) {
            System.out.println("Error while fetching paniers: " + e.getMessage());
        }

        return paniers;
    }

    @Override
    public void update(Panier panier) {
        String sql = "UPDATE panier SET user_id_id = ?, cree_le = ?, maj_le = ? WHERE id = ?";
        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setInt(1, panier.getUserId());
            stmt.setTimestamp(2, panier.getCreeLe());
            stmt.setTimestamp(3, panier.getMajLe());
            stmt.setInt(4, panier.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while updating panier: " + e.getMessage());
        }
    }

    @Override
    public void delete(Panier panier) {
        String sql = "DELETE FROM panier WHERE id = ?";
        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setInt(1, panier.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while deleting panier: " + e.getMessage());
        }
    }
}
