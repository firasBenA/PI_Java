package tn.esprit.services;
import tn.esprit.interfaces.IService;
import tn.esprit.entities.Panier;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PanierDAO {

    public void addPanier(Panier panier) throws SQLException {
    String sql = "INSERT INTO panier (user_id_id, cree_le, maj_le) VALUES (?, ?, ?)";
    PreparedStatement stmt = conn.prepareStatement(sql);
    stmt.setInt(1, panier.getUserId());
    stmt.setTimestamp(2, panier.getCreeLe());
    stmt.setTimestamp(3, panier.getMajLe());
    stmt.executeUpdate();
}


    public List<Panier> getAllPaniers() throws SQLException {
        List<Panier> list = new ArrayList<>();
        String sql = "SELECT * FROM panier";
        Statement st = MyConnection.getInstance().getCnx().createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            list.add(new Panier(rs.getInt("id"), rs.getInt("userId")));
        }
        return list;
    }

    public void updatePanier(Panier p) throws SQLException {
        String sql = "UPDATE panier SET userId=? WHERE id=?";
        PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(sql);
        ps.setInt(1, p.getUserId());
        ps.setInt(2, p.getId());
        ps.executeUpdate();
    }

    public void deletePanier(int id) throws SQLException {
        String sql = "DELETE FROM panier WHERE id=?";
        PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }
}
