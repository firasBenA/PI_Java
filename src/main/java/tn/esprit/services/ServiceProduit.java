package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Produit;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceProduit implements IService<Produit> {

    private Connection cnx;

    public ServiceProduit() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Produit produit) {
        String sql = "INSERT INTO produit (nom, description, prix, creer_le, maj_le, stock, image, ventes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setString(1, produit.getNom());
            stmt.setString(2, produit.getDescription());
            stmt.setDouble(3, produit.getPrix());
            stmt.setTimestamp(4, produit.getCreerLe());
            stmt.setTimestamp(5, produit.getMajLe());
            stmt.setInt(6, produit.getStock());
            stmt.setString(7, produit.getImage());
            stmt.setInt(8, produit.getVentes());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while adding produit: " + e.getMessage());
        }
    }

    @Override
    public List<Produit> getAll() {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT * FROM produit";
        try {
            Statement stmt = cnx.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Produit produit = new Produit(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getDouble("prix"),
                        rs.getTimestamp("creer_le"),
                        rs.getTimestamp("maj_le"),
                        rs.getInt("stock"),
                        rs.getString("image"),
                        rs.getInt("ventes")
                );
                produits.add(produit);
            }
        } catch (SQLException e) {
            System.out.println("Error while fetching produits: " + e.getMessage());
        }
        return produits;
    }

    @Override
    public void update(Produit produit) {
        String sql = "UPDATE produit SET nom=?, description=?, prix=?, creer_le=?, maj_le=?, stock=?, image=?, ventes=? WHERE id=?";
        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setString(1, produit.getNom());
            stmt.setString(2, produit.getDescription());
            stmt.setDouble(3, produit.getPrix());
            stmt.setTimestamp(4, produit.getCreerLe());
            stmt.setTimestamp(5, produit.getMajLe());
            stmt.setInt(6, produit.getStock());
            stmt.setString(7, produit.getImage());
            stmt.setInt(8, produit.getVentes());
            stmt.setInt(9, produit.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while updating produit: " + e.getMessage());
        }
    }

    @Override
    public void delete(Produit produit) {
        String sql = "DELETE FROM produit WHERE id = ?";
        try {
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setInt(1, produit.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while deleting produit: " + e.getMessage());
        }
    }
}
