package tn.esprit.services.models;

public class Statistiques {
    private int totalCount;
    private int enAttenteCount;
    private int traiteCount;

    public Statistiques(){}

    public Statistiques(int totalCount, int enAttenteCount, int traiteCount) {
        this.totalCount = totalCount;
        this.enAttenteCount = enAttenteCount;
        this.traiteCount = traiteCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getEnAttenteCount() {
        return enAttenteCount;
    }

    public void setEnAttenteCount(int enAttenteCount) {
        this.enAttenteCount = enAttenteCount;
    }

    public int getTraiteCount() {
        return traiteCount;
    }

    public void setTraiteCount(int traiteCount) {
        this.traiteCount = traiteCount;
    }
}