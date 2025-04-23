package tn.esprit.models;

public class Medecin extends User {
    public Medecin() {
        super();
        getRoles().clear();
        getRoles().add("ROLE_MEDECIN");
        setImageProfil(null);
    }
    @Override
    public void setSpecialite(String specialite) {
        if (specialite == null || specialite.trim().isEmpty()) {
            throw new IllegalArgumentException("Spécialité est requise pour un médecin");
        }
        super.setSpecialite(specialite);
    }

    @Override
    public void setCertificat(String certificat) {
        if (certificat == null || certificat.trim().isEmpty()) {
            throw new IllegalArgumentException("Certificat est requis pour un médecin");
        }
        super.setCertificat(certificat);
    }


}
