package tn.esprit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import okhttp3.*;
import tn.esprit.interfaces.IService;
import tn.esprit.models.Diagnostique;
import tn.esprit.models.Medecin;
import tn.esprit.services.AuthService;
import tn.esprit.services.ServiceDiagnostique;
import tn.esprit.models.User;
import tn.esprit.services.ServiceUser;
import tn.esprit.utils.SceneManager;

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

    ///////chatbot///////////

    @FXML private ListView<String> chatListView;
    @FXML private TextField userInputField;
    @FXML private Button sendButton;
    private final ObservableList<String> messages = FXCollections.observableArrayList();
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey = "";
    private static final int MAX_RETRIES = 3;
    private static final long BASE_RETRY_DELAY_MS = 5000;

    //////

    private AuthService authService;
    private SceneManager sceneManager;
    private User currentUser;

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }


    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("Current user set in GestionDiagnostique: " + (user != null ? user.getNom() : "null"));
    }

    public void initialize() {
        initSymptoms();

        doctorIdMap = ServiceDiagnostique.getDoctors();
        doctorComboBox.getItems().addAll(doctorIdMap.keySet());

        selectSymptomButton.setOnAction(e -> handleSelectSymptom());

        chatListView.setItems(messages);
        System.out.println("GestionDiagnostiqueController initialized (currentUser may be null)");
        initUserData();
    }

    private void initUserData() {
        if (currentUser != null) {
            System.out.println("Initializing user data for " + currentUser.getNom());
        }
    }

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

        if (selectedSymptoms.isEmpty()) {
            resultLabel.setText("❌ Veuillez sélectionner au moins un symptôme.");
            return;
        }

        Map<String, String> result = ServiceDiagnostique.diagnose(selectedSymptoms);
        String disease = result.getOrDefault("disease", "Inconnu");

        List<Medecin> matchingDoctors = ServiceUser.findMedecinsBySpecialite(disease);

        if (!matchingDoctors.isEmpty()) {
            ObservableList<String> doctorNames = FXCollections.observableArrayList();
            for (Medecin doctor : matchingDoctors) {
                doctorNames.add(doctor.getNom());
            }
            doctorComboBox.setItems(doctorNames);
            resultLabel.setText("Maladie: " + disease);
        } else {
            resultLabel.setText(resultLabel.getText() + "\n\nAucun médecin trouvé avec cette spécialité : " + disease);
        }
    }

        /*int doctorId = doctorIdMap.get(selectedDoctor);
        Date currentDate = new Date(System.currentTimeMillis());
        */
        @FXML
        private void handleSaveDiagnostique(ActionEvent event) {

            if (currentUser == null) {
                System.out.println("Current user is NULL!");
                return;
            }

            System.out.println("Current user roles: " + currentUser.getRoles() + currentUser.getNom());

            try {
                String disease = resultLabel.getText();
                String description = "Diagnostic généré par le système.";
                String selectedSymptoms = selectedSymptomsArea.getText();
                String selectedZoneCorps = zoneCorps.getText();
                String selectedDoctorName = doctorComboBox.getValue();
                int doctorId = ServiceUser.findDoctorIdByName(selectedDoctorName);

                System.out.println(selectedDoctorName);
                System.out.println(doctorId);
                System.out.println(selectedSymptoms);
                System.out.println(disease);

                if (doctorId == -1 || selectedSymptoms.isEmpty() || disease.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Données manquantes");
                    alert.setHeaderText(null);
                    alert.setContentText("Veuillez remplir toutes les informations nécessaires avant de sauvegarder.");
                    alert.show();
                    return;
                }

                Diagnostique diag = new Diagnostique();
                diag.setNom(disease);
                diag.setDescription(description);
                diag.setSelectedSymptoms(selectedSymptoms);
                diag.setMedecinId(doctorId);
                diag.setPatientId(currentUser.getId());
                diag.setDossierMedicalId(1);
                diag.setZoneCorps(selectedZoneCorps);
                diag.setDateSymptomes(Date.valueOf(LocalDate.now()));
                diag.setStatus(0);

                ServiceDiagnostique.saveDiagnosis(diag);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Diagnostic effectué");
                alert.setHeaderText(null);
                alert.setContentText("Le diagnostic a été effectué avec succès !");
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        System.out.println("ok pressssed");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Une erreur est survenue lors de l'enregistrement du diagnostic.");
                alert.show();
            }
        }

    //////

    @FXML
    public void sendMessage() {
        String userMessage = userInputField.getText().trim();
        if (userMessage.isEmpty()) return;

        sendButton.setDisable(true);
        messages.add("You: " + userMessage);
        userInputField.clear();

        new Thread(() -> {
            try {
                String response = getChatbotResponse(userMessage);
                System.out.println("Bot response: " + response);
                Platform.runLater(() -> messages.add("Bot: " + response));
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
                Platform.runLater(() -> messages.add("Bot: Error: " + e.getMessage()));
            } finally {
                Platform.runLater(() -> sendButton.setDisable(false));
            }
        }).start();

        chatListView.scrollTo(messages.size() - 1);
    }
    private String getChatbotResponse(String userMessage) throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("La clé API n'est pas configurée. Merci de définir la variable d'environnement 'apikey'.");
        }

        String instructionSysteme = "Tu es un chatbot médical qui parle français. "
                + "Tu dois répondre uniquement aux questions médicales en français. "
                + "Si la question n'est pas liée à la médecine, réponds poliment : "
                + "'Désolé, je ne peux répondre qu'aux questions médicales.'";

        String promptComplet = instructionSysteme + "\n\nQuestion de l'utilisateur : " + userMessage;

        String jsonBody = """
        {
            "contents": [
                {
                    "parts": [
                        {"text": "%s"}
                    ]
                }
            ],
            "generationConfig": {
                "maxOutputTokens": 300
            }
        }
        """.formatted(promptComplet.replace("\"", "\\\""));

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey)
                .post(body)
                .build();

        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    return mapper.readTree(responseBody)
                            .get("candidates").get(0)
                            .get("content").get("parts").get(0)
                            .get("text").asText().trim();
                } else if (response.code() == 429) {
                    attempt++;
                    String errorBody = response.body() != null ? response.body().string() : "Pas de corps de réponse";
                    String retryAfter = response.header("Retry-After");
                    long delayMs = retryAfter != null ? Long.parseLong(retryAfter) * 1000 : BASE_RETRY_DELAY_MS * attempt;
                    System.out.println("Trop de requêtes : " + errorBody + ". Nouvel essai après " + delayMs + "ms (Essai " + attempt + "/" + MAX_RETRIES + ")");
                    if (attempt >= MAX_RETRIES) {
                        throw new IOException("Limite atteinte après " + MAX_RETRIES + " essais : " + errorBody);
                    }
                    Thread.sleep(delayMs);
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Pas de corps de réponse";
                    throw new IOException("Code inattendu " + response.code() + " : " + errorBody);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interruption pendant l'attente entre les essais : " + e.getMessage());
            }
        }
        throw new IOException("Échec après " + MAX_RETRIES + " tentatives");
    }

}