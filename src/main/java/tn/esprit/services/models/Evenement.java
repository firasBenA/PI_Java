package tn.esprit.services.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Evenement {
    private int id;
    private String nom;
    private String contenue;
    private String type;
    private String statut;
    private String lieuxEvent;
    private LocalDate dateEvent;

    private List<Article> articles = new ArrayList<>();
    private List<Integer> participants = new ArrayList<>(); // Store IDs of users participating in this event

    public Evenement() {
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

    // Getters and setters for existing fields
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

    // Methods for relationship management
    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public void addArticle(Article article) {
        if (!this.articles.contains(article)) {
            this.articles.add(article);
        }
    }

    public void removeArticle(Article article) {
        this.articles.remove(article);
    }

    public List<Integer> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Integer> participants) {
        this.participants = participants;
    }

    public void addParticipant(int userId) {
        if (!this.participants.contains(userId)) {
            this.participants.add(userId);
        }
    }

    public void removeParticipant(int userId) {
        this.participants.remove(Integer.valueOf(userId));
    }

    public int getParticipantCount() {
        return participants.size();
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
                ", participantCount=" + getParticipantCount() +
                '}';
    }
}