package tn.esprit.services.models;

import java.util.ArrayList;
import java.util.List;

public class Article {
    private int id;
    private String titre;
    private String contenue;
    private String image;

    // Lists to manage relationships
    private List<Evenement> evenements = new ArrayList<>();
    private List<Integer> userLikes = new ArrayList<>(); // Store IDs of users who liked this article

    public Article() {
    }

    public Article(int id, String titre, String contenue, String image) {
        this();
        this.id = id;
        this.titre = titre;
        this.contenue = contenue;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenue() {
        return contenue;
    }

    public void setContenue(String contenue) {
        this.contenue = contenue;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    // Methods for relationship management
    public List<Evenement> getEvenements() {
        return evenements;
    }

    public void setEvenements(List<Evenement> evenements) {
        this.evenements = evenements;
    }

    public void addEvenement(Evenement evenement) {
        if (!this.evenements.contains(evenement)) {
            this.evenements.add(evenement);
        }
    }

    public void removeEvenement(Evenement evenement) {
        this.evenements.remove(evenement);
    }

    public List<Integer> getUserLikes() {
        return userLikes;
    }

    public void setUserLikes(List<Integer> userLikes) {
        this.userLikes = userLikes;
    }

    public void addUserLike(int userId) {
        if (!this.userLikes.contains(userId)) {
            this.userLikes.add(userId);
        }
    }

    public void removeUserLike(int userId) {
        this.userLikes.remove(Integer.valueOf(userId));
    }

    public int getLikeCount() {
        return userLikes.size();
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", contenue='" + contenue + '\'' +
                ", image='" + image + '\'' +
                ", likesCount=" + getLikeCount() +
                '}';
    }
}