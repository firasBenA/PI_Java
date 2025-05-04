package tn.esprit.services.models;

import java.time.LocalDate;

public class Reclamation {
    private int id;
    private String sujet;
    private String description;
    private LocalDate dateDebut;
    private String etat;
    private int userId;

    // Constructeur par défaut
    public Reclamation() {}

    // Constructeur pour l'ajout (sans ID, avec etat par défaut)
    public Reclamation(String sujet, String description, LocalDate dateDebut, int userId) {
        this.sujet = sujet;
        this.description = description;
        this.dateDebut = dateDebut;
        this.etat = "En Attente"; // Default to "EnAttente" for new reclamations
        this.userId = userId;
    }

    // Constructeur complet (pour update ou affichage)
    public Reclamation(int id, String sujet, String description, LocalDate dateDebut, String etat, int userId) {
        this.id = id;
        this.sujet = sujet;
        this.description = description;
        this.dateDebut = dateDebut;
        this.etat = etat;
        this.userId = userId;
    }

    // Fix the previously empty constructor used in Save method
    public Reclamation(String sujet, String description, LocalDate dateDebut, String etat, int userId) {
        this.sujet = sujet;
        this.description = description;
        this.dateDebut = dateDebut;
        this.etat = "En Attente"; // Always set to "EnAttente" for new reclamations
        this.userId = userId;
    }

    // Constructor for deletion (only ID needed)
    public Reclamation(int id) {
        this.id = id;
    }

    // Getters
    public int getId() { return id; }
    public String getSujet() { return sujet; }
    public String getDescription() { return description; }
    public LocalDate getDateDebut() { return dateDebut; }
    public String getEtat() { return etat; }
    public int getUserId() { return userId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setSujet(String sujet) { this.sujet = sujet; }
    public void setDescription(String description) { this.description = description; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public void setEtat(String etat) { this.etat = etat; }
    public void setUserId(int userId) { this.userId = userId; }

    // toString
    @Override
    public String toString() {
        return "Reclamation{" +
                "id=" + id +
                ", sujet='" + sujet + '\'' +
                ", description='" + description + '\'' +
                ", dateDebut=" + dateDebut +
                ", etat='" + etat + '\'' +
                ", userId=" + userId +
                '}';
    }
}