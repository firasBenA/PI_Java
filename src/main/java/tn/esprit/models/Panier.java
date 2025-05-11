package tn.esprit.models;

import java.sql.Timestamp;

public class Panier {

    private int id;
    private int userId;
    private Timestamp creeLe;
    private Timestamp majLe;

    public Panier() {}

    public Panier(int id, int userId, Timestamp creeLe, Timestamp majLe) {
        this.id = id;
        this.userId = userId;
        this.creeLe = creeLe;
        this.majLe = majLe;
    }

    public Panier(int userId, Timestamp creeLe, Timestamp majLe) {
        this.userId = userId;
        this.creeLe = creeLe;
        this.majLe = majLe;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Timestamp getCreeLe() {
        return creeLe;
    }

    public void setCreeLe(Timestamp creeLe) {
        this.creeLe = creeLe;
    }

    public Timestamp getMajLe() {
        return majLe;
    }

    public void setMajLe(Timestamp majLe) {
        this.majLe = majLe;
    }

    @Override
    public String toString() {
        return "Panier{" +
                "id=" + id +
                ", userId=" + userId +
                ", creeLe=" + creeLe +
                ", majLe=" + majLe +
                '}';
    }
}
