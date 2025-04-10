package tn.esprit.models;

import java.time.LocalDateTime;
import java.util.Date;

public class Prescription {

    private int id ,dossier_medical_id, diagnostique_id,medecin_id,patient_id ;
    private String titre ,contenue ;
    private LocalDateTime date_prescription;

    public Prescription() {
    }

    public Prescription(int id, int dossier_medical_id, int diagnostique_id, int medecin_id, int patient_id, String titre, String contenue, LocalDateTime date_prescription) {
        this.id = id;
        this.dossier_medical_id = dossier_medical_id;
        this.diagnostique_id = diagnostique_id;
        this.medecin_id = medecin_id;
        this.patient_id = patient_id;
        this.titre = titre;
        this.contenue = contenue;
        this.date_prescription = date_prescription;
    }

    public Prescription( int dossier_medical_id, int diagnostique_id, int medecin_id, int patient_id, String titre, String contenue, LocalDateTime date_prescription) {
        this.dossier_medical_id = dossier_medical_id;
        this.diagnostique_id = diagnostique_id;
        this.medecin_id = medecin_id;
        this.patient_id = patient_id;
        this.titre = titre;
        this.contenue = contenue;
        this.date_prescription = date_prescription;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDossier_medical_id() {
        return dossier_medical_id;
    }

    public void setDossier_medical_id(int dossier_medical_id) {
        this.dossier_medical_id = dossier_medical_id;
    }

    public int getDiagnostique_id() {
        return diagnostique_id;
    }

    public void setDiagnostique_id(int diagnostique_id) {
        this.diagnostique_id = diagnostique_id;
    }

    public int getMedecin_id() {
        return medecin_id;
    }

    public void setMedecin_id(int medecin_id) {
        this.medecin_id = medecin_id;
    }

    public int getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(int patient_id) {
        this.patient_id = patient_id;
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

    public LocalDateTime getDate_prescription() {
        return date_prescription;
    }

    public void setDate_prescription(LocalDateTime date_prescription) {
        this.date_prescription = date_prescription;
    }

    @Override
    public String toString() {
        return "Prescription{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", contenue='" + contenue + '\'' +
                ", date_prescription=" + date_prescription +
                '}';
    }
}
