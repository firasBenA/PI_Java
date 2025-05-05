package tn.esprit.services.models;

import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User {

    private Integer id;
    private String email;
    private List<String> roles = new ArrayList<>();
    private String userType;
    private String password;
    private String nom;
    private String prenom;
    private Integer age;
    private String adresse;
    private String sexe;
    private String telephone;
    private String imageProfil;
    private String specialite;
    private String certificat;
    private Double latitude;
    private Double longitude;
    private Boolean emailAuthEnabled = true;
    private String emailAuthCode = "";
    private LocalDateTime createdAt;
    private Integer failedLoginAttempts = 0;
    private LocalDateTime lockUntil;

    private String socialProvider; // e.g., "google"
    private String socialAccessToken;

    public User() {
        this.createdAt = LocalDateTime.now();
        this.userType = determineUserType();
    }

    public String determineUserType() {
        if (this instanceof Admin) return "ADMIN";
        if (this instanceof Patient) return "PATIENT";
        if (this instanceof Medecin) return "MEDECIN";
        return "UNKNOWN";
    }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public List<String> getRoles() { return new ArrayList<>(roles); }
    public void setRoles(List<String> roles) { this.roles = new ArrayList<>(roles); }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getImageProfil() { return imageProfil; }
    public void setImageProfil(String imageProfil) { this.imageProfil = imageProfil; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public String getCertificat() { return certificat; }
    public void setCertificat(String certificat) { this.certificat = certificat; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Boolean getEmailAuthEnabled() { return emailAuthEnabled; }
    public void setEmailAuthEnabled(Boolean emailAuthEnabled) { this.emailAuthEnabled = emailAuthEnabled; }

    public String getEmailAuthCode() { return emailAuthCode; }
    public void setEmailAuthCode(String emailAuthCode) { this.emailAuthCode = emailAuthCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public LocalDateTime getLockUntil() { return lockUntil; }
    public void setLockUntil(LocalDateTime lockUntil) { this.lockUntil = lockUntil; }

    public void hashPassword(String rawPassword) {
        this.password = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    public boolean checkPassword(String password) {
        if (this.password == null || this.password.isEmpty()) {
            System.err.println("Password hash is null or empty for user: " + this.email);
            return false;
        }
        try {
            return BCrypt.checkpw(password, this.password);
        } catch (IllegalArgumentException e) {
            System.err.println("Password verification failed for user " + this.email + ": " + e.getMessage());
            return false;
        }
    }

    public String getSocialProvider() { return socialProvider; }
    public void setSocialProvider(String socialProvider) { this.socialProvider = socialProvider; }
    public String getSocialAccessToken() { return socialAccessToken; }
    public void setSocialAccessToken(String socialAccessToken) { this.socialAccessToken = socialAccessToken; }
}