package tn.esprit.models;

import java.time.LocalDate;

public class Diagnostique {

    private int id;
    private int dossierMedicalId;
    private int patientId;
    private int medecinId;
    private LocalDate dateDiagnostique;
    private String nom;
    private String description;
    private String zoneCorps;
    private LocalDate dateSymptomes;
    private String status;
    private String selectedSymptoms;


    public Diagnostique(){}
    public Diagnostique(int id, int dossierMedicalId, int patientId, int medecinId, LocalDate dateDiagnostique, String nom, String description, String zoneCorps, LocalDate dateSymptomes, String status, String selectedSymptoms) {
        this.id = id;
        this.dossierMedicalId = dossierMedicalId;
        this.patientId = patientId;
        this.medecinId = medecinId;
        this.dateDiagnostique = dateDiagnostique;
        this.nom = nom;
        this.description = description;
        this.zoneCorps = zoneCorps;
        this.dateSymptomes = dateSymptomes;
        this.status = status;
        this.selectedSymptoms = selectedSymptoms;
    }

    public Diagnostique( int dossierMedicalId, int patientId, int medecinId, LocalDate dateDiagnostique, String nom, String description, String zoneCorps, LocalDate dateSymptomes, String status, String selectedSymptoms) {
        this.dossierMedicalId = dossierMedicalId;
        this.patientId = patientId;
        this.medecinId = medecinId;
        this.dateDiagnostique = dateDiagnostique;
        this.nom = nom;
        this.description = description;
        this.zoneCorps = zoneCorps;
        this.dateSymptomes = dateSymptomes;
        this.status = status;
        this.selectedSymptoms = selectedSymptoms;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDossierMedicalId() {
        return dossierMedicalId;
    }

    public void setDossierMedicalId(int dossierMedicalId) {
        this.dossierMedicalId = dossierMedicalId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(int medecinId) {
        this.medecinId = medecinId;
    }

    public LocalDate getDateDiagnostique() {
        return dateDiagnostique;
    }

    public void setDateDiagnostique(LocalDate dateDiagnostique) {
        this.dateDiagnostique = dateDiagnostique;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getZoneCorps() {
        return zoneCorps;
    }

    public void setZoneCorps(String zoneCorps) {
        this.zoneCorps = zoneCorps;
    }

    public LocalDate getDateSymptomes() {
        return dateSymptomes;
    }

    public void setDateSymptomes(LocalDate dateSymptomes) {
        this.dateSymptomes = dateSymptomes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSelectedSymptoms() {
        return selectedSymptoms;
    }

    public void setSelectedSymptoms(String selectedSymptoms) {
        this.selectedSymptoms = selectedSymptoms;
    }

    @Override
    public String toString() {
        return "Diagnostique{" +
                "id=" + id +
                ", dateDiagnostique=" + dateDiagnostique +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", zoneCorps='" + zoneCorps + '\'' +
                ", selectedSymptoms='" + selectedSymptoms + '\'' +
                '}';
    }
}
