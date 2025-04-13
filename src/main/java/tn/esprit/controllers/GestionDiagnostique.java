package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.interfaces.IService;
import tn.esprit.models.Diagnostique;
import tn.esprit.models.Personne;
import tn.esprit.services.ServiceDiagnostique;
import tn.esprit.services.ServicePersonne;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestionDiagnostique {

    @FXML
    private TextField idDiagnostique;

    @FXML
    private TextField nom;
    @FXML
    private TextField zoneCorps;
    @FXML
    private DatePicker dateDiagnostique;

    IService<Diagnostique> diag = new ServiceDiagnostique();
    ////
    @FXML private ListView<String> symptomsListView;
    @FXML private ComboBox<String> doctorComboBox;
    @FXML private Label resultLabel;
    @FXML
    private Button selectSymptomButton;
    @FXML
    private TextArea selectedSymptomsArea;

    private ObservableList<String> selectedSymptoms = FXCollections.observableArrayList();
    private Map<String, Integer> symptomsDict = new HashMap<>();
    private Map<String, Integer> doctorIdMap = new HashMap<>();
    ////

    @FXML
    public void ajouterDiagnostique(ActionEvent actionEvent) {
        Diagnostique d = new Diagnostique();
        d.setDossierMedicalId(1);
        d.setPatientId(1);
        d.setMedecinId(3);
        d.setDateDiagnostique(Date.valueOf(dateDiagnostique.getValue()));
        d.setNom("Allergy");
        d.setDescription("test java");
        d.setZoneCorps(zoneCorps.getText());
        d.setDateSymptomes(Date.valueOf(dateDiagnostique.getValue()));
        d.setStatus(0);

        String selected = String.join(",", selectedSymptoms);
        d.setSelectedSymptoms(selected);

        diag.add(d);
    }

    @FXML
    public void modifierDiagnostique(ActionEvent actionEvent) {
        int id = Integer.parseInt(idDiagnostique.getText()); // Get the ID from the UI

        Diagnostique d = new Diagnostique();
        d.setId(id); // Set the ID to update the correct record
        d.setDossierMedicalId(1);
        d.setPatientId(1);
        d.setMedecinId(3);
        d.setDateDiagnostique(Date.valueOf(dateDiagnostique.getValue()));
        d.setNom(nom.getText()); // Use input field
        d.setDescription("Mise à jour depuis JavaFX");
        d.setZoneCorps(zoneCorps.getText());
        d.setDateSymptomes(Date.valueOf(dateDiagnostique.getValue()));
        d.setStatus(1); // Status updated (example)

        String selected = String.join(",", selectedSymptoms);
        d.setSelectedSymptoms(selected);

        diag.update(d); // Calls the update method
    }
    //////

    public void initialize() {
        // Initialize symptoms list
        initSymptoms();

        // Fetch doctors from database and populate combo box
        doctorIdMap = ServiceDiagnostique.getDoctors();
        doctorComboBox.getItems().addAll(doctorIdMap.keySet());

        // Handle select symptom button
        selectSymptomButton.setOnAction(e -> handleSelectSymptom());
    }

    // Initialize symptoms list
    private void initSymptoms() {
        ObservableList<String> symptoms = FXCollections.observableArrayList(
                "itching", "skin_rash", "nodal_skin_eruptions", "continuous_sneezing", "shivering", "chills",
                "joint_pain", "stomach_pain", "acidity", "ulcers_on_tongue", "muscle_wasting", "vomiting",
                "burning_micturition", "spotting_ urination", "fatigue", "weight_gain", "anxiety",
                "cold_hands_and_feets", "mood_swings", "weight_loss", "restlessness", "lethargy",
                "patches_in_throat", "irregular_sugar_level", "cough", "high_fever", "sunken_eyes",
                "breathlessness", "sweating", "dehydration", "indigestion", "headache", "yellowish_skin",
                "dark_urine", "nausea", "loss_of_appetite", "pain_behind_the_eyes", "back_pain",
                "constipation", "abdominal_pain", "diarrhoea", "mild_fever", "yellow_urine",
                "yellowing_of_eyes", "acute_liver_failure", "fluid_overload", "swelling_of_stomach",
                "swelled_lymph_nodes", "malaise", "blurred_and_distorted_vision", "phlegm",
                "throat_irritation", "redness_of_eyes", "sinus_pressure", "runny_nose", "congestion",
                "chest_pain", "weakness_in_limbs", "fast_heart_rate", "pain_during_bowel_movements",
                "pain_in_anal_region", "bloody_stool", "irritation_in_anus", "neck_pain", "dizziness",
                "cramps", "bruising", "obesity", "swollen_legs", "swollen_blood_vessels",
                "puffy_face_and_eyes", "enlarged_thyroid", "brittle_nails", "swollen_extremeties",
                "excessive_hunger", "extra_marital_contacts", "drying_and_tingling_lips", "slurred_speech",
                "knee_pain", "hip_joint_pain", "muscle_weakness", "stiff_neck", "swelling_joints",
                "movement_stiffness", "spinning_movements", "loss_of_balance", "unsteadiness",
                "weakness_of_one_body_side", "loss_of_smell", "bladder_discomfort", "foul_smell_of urine",
                "continuous_feel_of_urine", "passage_of_gases", "internal_itching", "toxic_look_(typhos)",
                "depression", "irritability", "muscle_pain", "altered_sensorium", "red_spots_over_body",
                "belly_pain", "abnormal_menstruation", "dischromic _patches", "watering_from_eyes",
                "increased_appetite", "polyuria", "family_history", "mucoid_sputum", "rusty_sputum",
                "lack_of_concentration", "visual_disturbances", "receiving_blood_transfusion",
                "receiving_unsterile_injections", "coma", "stomach_bleeding", "distention_of_abdomen",
                "history_of_alcohol_consumption", "fluid_overload.1", "blood_in_sputum",
                "prominent_veins_on_calf", "palpitations", "painful_walking", "pus_filled_pimples",
                "blackheads", "scurring", "skin_peeling", "silver_like_dusting", "small_dents_in_nails",
                "inflammatory_nails", "blister", "red_sore_around_nose", "yellow_crust_ooze"
        );

        symptomsListView.setItems(symptoms);
        symptomsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }
    @FXML
    private void handleSelectSymptom() {
        String symptom = symptomsListView.getSelectionModel().getSelectedItem();
        if (symptom != null && !selectedSymptoms.contains(symptom)) {
            selectedSymptoms.add(symptom);
            updateSelectedSymptomsArea();
        }
    }

    private void updateSelectedSymptomsArea() {
        selectedSymptomsArea.setText(String.join(", ", selectedSymptoms));
    }

    @FXML
    private void handleDiagnose() {
        String selectedDoctor = doctorComboBox.getValue();
        String selectedZoneCorps = zoneCorps.getText();

        if (selectedSymptoms.isEmpty()) {
            resultLabel.setText("❌ Veuillez sélectionner au moins un symptôme.");
            return;
        }

        if (selectedDoctor == null || selectedDoctor.trim().isEmpty()) {
            resultLabel.setText("❌ Veuillez sélectionner un médecin.");
            return;
        }

        if (selectedZoneCorps == null || selectedZoneCorps.trim().isEmpty()) {
            resultLabel.setText("❌ Veuillez spécifier une zone du corps.");
            return;
        }

        if (selectedSymptomsArea.getText().length() > 1000) {
            resultLabel.setText("❌ Trop de texte sélectionné pour les symptômes.");
            return;
        }

        Tooltip errorTooltip = new Tooltip("Sélection requise !");
        Tooltip.install(doctorComboBox, errorTooltip);

        // Call the service to diagnose
        Map<String, String> result = ServiceDiagnostique.diagnose(selectedSymptoms);

        String disease = result.getOrDefault("disease", "Inconnu");
        String description = result.getOrDefault("description", "Pas de description");

        // Display diagnosis result
        resultLabel.setText("Maladie: " + disease + "\nDescription: " + description);

        // Prepare all required data
        int doctorId = doctorIdMap.get(selectedDoctor);
        Date currentDate = new Date(System.currentTimeMillis()); // dateDiagnostique

        Diagnostique diag = new Diagnostique();
        diag.setNom(disease);
        diag.setDescription(description);
        diag.setSelectedSymptoms(selectedSymptoms.toString());
        diag.setMedecinId(doctorId);
        diag.setPatientId(1);
        diag.setDossierMedicalId(1);
        diag.setZoneCorps(selectedZoneCorps);
        diag.setDateSymptomes(Date.valueOf(LocalDate.now()));
        diag.setStatus(0);

        // Save the diagnosis
        ServiceDiagnostique.saveDiagnosis(diag);

        // Show an alert and wait for user to click OK before navigating to the home screen
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Diagnostic effectué");
        alert.setHeaderText(null);
        alert.setContentText("Le diagnostic a été effectué avec succès !");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Now, navigate to the home screen
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) resultLabel.getScene().getWindow(); // Get the current stage
                    stage.setScene(new Scene(root)); // Set the home scene
                    stage.show(); // Show the home screen
                } catch (IOException e) {
                    e.printStackTrace();
                    resultLabel.setText("❌ Une erreur est survenue lors du changement de page.");
                }
            }
        });
    }



    //////
}
