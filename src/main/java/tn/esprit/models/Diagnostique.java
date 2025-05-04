package tn.esprit.models;

import java.time.LocalDate;
import java.sql.Date;

public class Diagnostique {

    private int id;
    private int dossierMedicalId;
    private int patientId;
    private int medecinId;
    private Date dateDiagnostique;
    private String nom;
    private String description;
    private String zoneCorps;
    private Date dateSymptomes;
    private int status;
    private String selectedSymptoms;




    // Empty constructor
    public Diagnostique() {}

    // Constructor for basic use (when loading from DB with selected fields)
    public Diagnostique(int id, Date dateDiagnostique, String nom, String zoneCorps, String selectedSymptoms, int patientId) {
        this.id = id;
        this.dateDiagnostique = dateDiagnostique;
        this.nom = nom;
        this.zoneCorps = zoneCorps;
        this.selectedSymptoms = selectedSymptoms;
        this.patientId = patientId;
    }

    // Constructor when creating a new Diagnostique (without IDs)
    public Diagnostique(String dateDiagnostique, String nom, String zoneCorps, String selectedSymptoms) {
        this.dateDiagnostique = Date.valueOf(dateDiagnostique);
        this.nom = nom;
        this.zoneCorps = zoneCorps;
        this.selectedSymptoms = selectedSymptoms;
    }

    // Full constructor (used when you want all fields)
    public Diagnostique(int id, int dossierMedicalId, int patientId, int medecinId, Date dateDiagnostique, String nom, String description, String zoneCorps, Date dateSymptomes, int status, String selectedSymptoms) {
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

    // Constructor without id (for inserting new data)
    public Diagnostique(int dossierMedicalId, int patientId, int medecinId, Date dateDiagnostique, String nom, String description, String zoneCorps, Date dateSymptomes, int status, String selectedSymptoms) {
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

    public Date getDateDiagnostique() {
        return dateDiagnostique;
    }

    public void setDateDiagnostique(Date dateDiagnostique) {
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

    public Date getDateSymptomes() {
        return dateSymptomes;
    }

    public void setDateSymptomes(Date dateSymptomes) {
        this.dateSymptomes = dateSymptomes;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
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
