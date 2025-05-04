package tn.esprit.services.models;

public class Patient extends User {
    public Patient() {
        super();
        getRoles().clear();
        getRoles().add("ROLE_PATIENT");
        setImageProfil(null);
    }
}
