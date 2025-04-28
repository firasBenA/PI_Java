package tn.esprit.models;

import java.sql.Timestamp;

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

    public Produit() {}

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

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public Timestamp getCreerLe() {
        return creerLe;
    }

    public void setCreerLe(Timestamp creerLe) {
        this.creerLe = creerLe;
    }

    public Timestamp getMajLe() {
        return majLe;
    }

    public void setMajLe(Timestamp majLe) {
        this.majLe = majLe;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getVentes() {
        return ventes;
    }

    public void setVentes(int ventes) {
        this.ventes = ventes;
    }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", creerLe=" + creerLe +
                ", majLe=" + majLe +
                ", stock=" + stock +
                ", image='" + image + '\'' +
                ", ventes=" + ventes +
                '}';
    }
}
