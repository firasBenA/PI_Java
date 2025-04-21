package tn.esprit.services;
import tn.esprit.interfaces.IService;
import tn.esprit.entities.Produit;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitDAO {

    public void addProduit(Produit produit) throws SQLException {
    String sql = "INSERT INTO produit (nom, description, prix, creer_le, maj_le, stock, image, ventes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    PreparedStatement stmt = conn.prepareStatement(sql);
    stmt.setString(1, produit.getNom());
    stmt.setString(2, produit.getDescription());
    stmt.setDouble(3, produit.getPrix());
    stmt.setTimestamp(4, produit.getCreerLe());
    stmt.setTimestamp(5, produit.getMajLe());
    stmt.setInt(6, produit.getStock());
    stmt.setString(7, produit.getImage());
    stmt.setInt(8, produit.getVentes());
    stmt.executeUpdate();
}

public List<Produit> getAllProduits() throws SQLException {
    List<Produit> list = new ArrayList<>();
    String sql = "SELECT * FROM produit";
    ResultSet rs = conn.createStatement().executeQuery(sql);
    while (rs.next()) {
        list.add(new Produit(
            rs.getInt("id"),
            rs.getString("nom"),
            rs.getString("description"),
            rs.getDouble("prix"),
            rs.getTimestamp("creer_le"),
            rs.getTimestamp("maj_le"),
            rs.getInt("stock"),
            rs.getString("image"),
            rs.getInt("ventes")
        ));
    }
    return list;
}

}
