package tn.esprit.services.models;

public class Admin extends User {
    public Admin() {
        super();
        getRoles().clear();
        this.getRoles().add("ROLE_ADMIN");
    }
}
