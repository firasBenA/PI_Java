package tn.esprit.models;

import java.time.LocalDate;

public class Reponse {
    private int id;
    private String contenu;
    private LocalDate dateReponse;
    private int reclamationId;
    private Integer rating; // New field for rating (1-5, nullable)

    public Reponse() {}

    public Reponse(String contenu, LocalDate dateReponse, int reclamationId) {
        this.contenu = contenu;
        this.dateReponse = dateReponse;
        this.reclamationId = reclamationId;
        this.rating = null;
    }

    public Reponse(int id, String contenu, LocalDate dateReponse, int reclamationId) {
        this.id = id;
        this.contenu = contenu;
        this.dateReponse = dateReponse;
        this.reclamationId = reclamationId;
        this.rating = null;
    }

    public Reponse(int id, String contenu, LocalDate dateReponse, int reclamationId, Integer rating) {
        this.id = id;
        this.contenu = contenu;
        this.dateReponse = dateReponse;
        this.reclamationId = reclamationId;
        setRating(rating); // Validate rating during construction
    }

    public int getId() { return id; }
    public String getContenu() { return contenu; }
    public LocalDate getDateReponse() { return dateReponse; }
    public int getReclamationId() { return reclamationId; }
    public Integer getRating() { return rating; }

    public void setId(int id) { this.id = id; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public void setDateReponse(LocalDate dateReponse) { this.dateReponse = dateReponse; }
    public void setReclamationId(int reclamationId) { this.reclamationId = reclamationId; }
    public void setRating(Integer rating) {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Reponse{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", dateReponse=" + dateReponse +
                ", reclamationId=" + reclamationId +
                ", rating=" + (rating != null ? rating : "unrated") +
                '}';
    }
}