package tn.esprit.models;


public class Article {
    private int id;
    private String titre;
    private String contenue;
    private String image;

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

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", contenue='" + contenue + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}