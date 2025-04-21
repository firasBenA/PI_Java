package tn.esprit.models;

public class Panier {
    private int id;
    private int userId;
    private Timestamp creeLe;
    private Timestamp majLe;

    public Panier(int userId) {
        this.userId = userId;
        this.creeLe = new Timestamp(System.currentTimeMillis());
        this.majLe = new Timestamp(System.currentTimeMillis());
    }

    public Panier(int id, int userId, Timestamp creeLe, Timestamp majLe) {
        this.id = id;
        this.userId = userId;
        this.creeLe = creeLe;
        this.majLe = majLe;
    }

    // Getters & setters
}
