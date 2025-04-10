package tn.esprit.test;

import tn.esprit.models.Personne;
import tn.esprit.models.Prescription;
import tn.esprit.services.ServicePersonne;
import tn.esprit.services.ServicePrescription;
import tn.esprit.utils.MyDataBase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class Main {


    public static void main(String[] args) {
//        Test t1 = Test.getInstance();
//        Test t2 = Test.getInstance();
//
//
//        System.out.println(t1);
//        System.out.println(t2);

        ServicePersonne sp = new ServicePersonne();
        ServicePrescription sPresc = new ServicePrescription();

        sPresc.add(new Prescription( 1, 19, 3, 1, "TESTJAVA", "test test test JAVA", LocalDateTime.now()));

        ///////// UPDATE //////////////////
        Prescription p = new Prescription();
        p.setId(1);
        p.setDossier_medical_id(1);
        p.setDiagnostique_id(1);
        p.setMedecin_id(3);
        p.setPatient_id(1);
        p.setTitre("Updated Titre");
        p.setContenue("Updated contenu");
        p.setDate_prescription(LocalDateTime.now());

        sPresc.update(p);
        ///////// *** //////////////////

        ///////// DELETE //////////////////

        Prescription prescriptionToDelete = new Prescription();
        prescriptionToDelete.setId(4);

        //sPresc.delete(prescriptionToDelete);
        ///////// *** //////////////////


        System.out.println(sPresc.getAll());
    }

}
