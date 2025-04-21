package tn.esprit.models;

public class Produit {
    private int id;
    private String nom;
    private String description;
    private double prix;
    private Timestamp creerLe;
    private Timestamp majLe;
    private int stock;
    private String image;
    private int ventes;

    public Produit(int id, String nom, String description, double prix, Timestamp creerLe, Timestamp majLe, int stock, String image, int ventes) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.creerLe = creerLe;
        this.majLe = majLe;
        this.stock = stock;
        this.image = image;
        this.ventes = ventes;
    }

    public Produit(String nom, String description, double prix, int stock, String image) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.stock = stock;
        this.image = image;
        this.creerLe = new Timestamp(System.currentTimeMillis());
        this.majLe = new Timestamp(System.currentTimeMillis());
        this.ventes = 0;
    }

    // Getters & setters for all fields...
}
