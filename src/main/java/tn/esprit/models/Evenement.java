package tn.esprit.models;

import java.time.LocalDate;

public class Evenement {
    private int id;
    private String nom;
    private String contenue;
    private String type;
    private String statut;
    private String lieuxEvent;
    private LocalDate dateEvent;


    public Evenement(){

    }

    public Evenement(int id, String nom, String contenue, String type,
                     String statut, String lieuxEvent, LocalDate dateEvent) {
        this();
        this.id = id;
        this.nom = nom;
        this.contenue = contenue;
        this.type = type;
        this.statut = statut;
        this.lieuxEvent = lieuxEvent;
        this.dateEvent = dateEvent;
    }

    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getContenue() {
        return contenue;
    }

    public void setContenue(String contenue) {
        this.contenue = contenue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getLieuxEvent() {
        return lieuxEvent;
    }

    public void setLieuxEvent(String lieuxEvent) {
        this.lieuxEvent = lieuxEvent;
    }

    public LocalDate getDateEvent() {
        return dateEvent;
    }

    public void setDateEvent(LocalDate dateEvent) {
        this.dateEvent = dateEvent;
    }

    @Override
    public String toString() {
        return "Evenement{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", contenue='" + contenue + '\'' +
                ", type='" + type + '\'' +
                ", statut='" + statut + '\'' +
                ", lieuxEvent='" + lieuxEvent + '\'' +
                ", dateEvent=" + dateEvent +
                '}';
    }
}


