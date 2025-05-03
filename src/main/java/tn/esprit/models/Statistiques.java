package tn.esprit.models;

public class Statistiques {
    private int totalCount;
    private int enAttenteCount;
    private int traiteCount;
    private int highSatisfactionCount; // Ratings 4 and 5
    private int moderateSatisfactionCount; // Ratings 2 and 3
    private int lowSatisfactionCount; // Rating 1
    private int unratedCount;

    public Statistiques() {}

    public Statistiques(int totalCount, int enAttenteCount, int traiteCount) {
        this.totalCount = totalCount;
        this.enAttenteCount = enAttenteCount;
        this.traiteCount = traiteCount;
        this.highSatisfactionCount = 0;
        this.moderateSatisfactionCount = 0;
        this.lowSatisfactionCount = 0;
        this.unratedCount = 0;
    }

    public Statistiques(int totalCount, int enAttenteCount, int traiteCount,
                        int highSatisfactionCount, int moderateSatisfactionCount,
                        int lowSatisfactionCount, int unratedCount) {
        this.totalCount = totalCount;
        this.enAttenteCount = enAttenteCount;
        this.traiteCount = traiteCount;
        this.highSatisfactionCount = highSatisfactionCount;
        this.moderateSatisfactionCount = moderateSatisfactionCount;
        this.lowSatisfactionCount = lowSatisfactionCount;
        this.unratedCount = unratedCount;
    }

    
    public int getTotalCount() { return totalCount; }
    public int getEnAttenteCount() { return enAttenteCount; }
    public int getTraiteCount() { return traiteCount; }
    public int getHighSatisfactionCount() { return highSatisfactionCount; }
    public int getModerateSatisfactionCount() { return moderateSatisfactionCount; }
    public int getLowSatisfactionCount() { return lowSatisfactionCount; }
    public int getUnratedCount() { return unratedCount; }

    // Setters
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public void setEnAttenteCount(int enAttenteCount) { this.enAttenteCount = enAttenteCount; }
    public void setTraiteCount(int traiteCount) { this.traiteCount = traiteCount; }
    public void setHighSatisfactionCount(int highSatisfactionCount) { this.highSatisfactionCount = highSatisfactionCount; }
    public void setModerateSatisfactionCount(int moderateSatisfactionCount) { this.moderateSatisfactionCount = moderateSatisfactionCount; }
    public void setLowSatisfactionCount(int lowSatisfactionCount) { this.lowSatisfactionCount = lowSatisfactionCount; }
    public void setUnratedCount(int unratedCount) { this.unratedCount = unratedCount; }
}