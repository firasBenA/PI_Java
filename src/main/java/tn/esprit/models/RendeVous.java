package tn.esprit.models;

import java.time.LocalDate;

public class RendeVous {
    private int id;
    private int patient_id;
    private int medecin_id;
    private LocalDate date;
    private String statut;
    private String type;
    private String cause;

    private int user_id;

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }



    // Constructeurs
    public RendeVous() {
    }

    public RendeVous(int id, int idPatient, int idMedecin, LocalDate date,
                     String statut, String type, String cause) {
        this.id = id;
        this.patient_id = patient_id;
        this.medecin_id = idMedecin;
        this.date = date;
        this.statut = statut;
        this.type = type;
        this.cause = cause;
    }

    public RendeVous(LocalDate selectedDate, String value, String text, Integer integer, Integer id, String enAttente) {
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdPatient() {
        return patient_id;
    }



    public void setIdPatient(int patient_id) {
        this.patient_id = patient_id;
    }

    public int getIdMedecin() {
        return medecin_id;
    }

    public void setIdMedecin(int medecin_id) {
        this.medecin_id = medecin_id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    @Override
    public String toString() {
        return "RendeVous{" +
                "id=" + id +
                ", idPatient=" + patient_id +
                ", idMedecin=" + medecin_id +
                ", date=" + date +
                ", statut='" + statut + '\'' +
                ", type='" + type + '\'' +
                ", cause='" + cause + '\'' +
                '}';
    }

    public enum StatutRendezVous {
        EN_ATTENTE, APPROUVE, REFUSE
    }

    public enum TypeRendezVous {
        CONSULTATION, URGENCE, SUIVI
    }
}