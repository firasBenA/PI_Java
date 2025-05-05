package tn.esprit.services.models;

import java.time.LocalDate;

public class Consultation {
    private int id;
    private int rendez_vous_id;
    private int patient_id;
    private int medecin_id;
    private LocalDate date;
    private double prix;
    private String type_consultation;
    private int user_id;
    private String statut;

    private String patientPrenom;
    private String patientNom;

    // Constructors
    public Consultation() {
    }

    public Consultation(int rendez_vous_id, int patient_id, int medecin_id, LocalDate date,
                        double prix, String type_consultation, int user_id) {
        this.rendez_vous_id = rendez_vous_id;
        this.patient_id = patient_id;
        this.medecin_id = medecin_id;
        this.date = date;
        this.prix = prix;
        this.type_consultation = type_consultation;
        this.user_id = user_id;
        this.statut = "en_attente"; // Valeur par défaut
    }

    public Consultation(int rendez_vous_id, int patient_id, int medecin_id, LocalDate date,
                        double prix, String type_consultation, int user_id,
                        String patientPrenom, String patientNom) {
        this(rendez_vous_id, patient_id, medecin_id, date, prix, type_consultation, user_id);
        this.patientPrenom = patientPrenom;
        this.patientNom = patientNom;
    }

    // Full constructor with status
    public Consultation(int rendez_vous_id, int patient_id, int medecin_id, LocalDate date,
                        double prix, String type_consultation, int user_id,
                        String patientPrenom, String patientNom, String statut) {
        this(rendez_vous_id, patient_id, medecin_id, date, prix, type_consultation, user_id,
                patientPrenom, patientNom);
        this.statut = statut;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRendez_vous_id() {
        return rendez_vous_id;
    }

    public void setRendez_vous_id(int rendez_vous_id) {
        this.rendez_vous_id = rendez_vous_id;
    }

    public int getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(int patient_id) {
        this.patient_id = patient_id;
    }

    public int getMedecin_id() {
        return medecin_id;
    }

    public void setMedecin_id(int medecin_id) {
        this.medecin_id = medecin_id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getType_consultation() {
        return type_consultation;
    }

    public void setType_consultation(String type_consultation) {
        this.type_consultation = type_consultation;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    // Getters and Setters for patient names
    public String getPatientPrenom() {
        return patientPrenom;
    }

    public void setPatientPrenom(String patientPrenom) {
        this.patientPrenom = patientPrenom;
    }

    public String getPatientNom() {
        return patientNom;
    }

    public void setPatientNom(String patientNom) {
        this.patientNom = patientNom;
    }

    // Getter and Setter for status
    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "Consultation{" +
                "id=" + id +
                ", rendez_vous_id=" + rendez_vous_id +
                ", patient_id=" + patient_id +
                ", patient=" + patientPrenom + " " + patientNom +
                ", medecin_id=" + medecin_id +
                ", date=" + date +
                ", prix=" + prix +
                ", type_consultation='" + type_consultation + '\'' +
                ", user_id=" + user_id +
                ", statut='" + statut + '\'' +
                '}';
    }

    public String getPatientFullName() {
        return (patientPrenom != null ? patientPrenom : "") + " " + (patientNom != null ? patientNom : "");
    }

    public boolean isApprouvee() {
        return "approuvé".equalsIgnoreCase(statut);
    }

    public boolean isRefusee() {
        return "refusé".equalsIgnoreCase(statut);
    }

    public boolean isEnAttente() {
        return "en_attente".equalsIgnoreCase(statut);
    }
}