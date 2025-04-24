package tn.esprit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import okhttp3.*;
import java.io.IOException;

public class ChatbotController {
    @FXML private ListView<String> chatListView;
    @FXML private TextField userInputField;
    @FXML private Button sendButton;
    private final ObservableList<String> messages = FXCollections.observableArrayList();
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey = "";
    private static final int MAX_RETRIES = 3;
    private static final long BASE_RETRY_DELAY_MS = 5000;

    @FXML
    public void initialize() {
        chatListView.setItems(messages);
        System.out.println("API Key: " + apiKey); // Debug: Check if key is loaded
        if (apiKey == null || apiKey.isEmpty()) {
            messages.add("Bot: Error: API key is not set. Please configure the 'apikey' environment variable.");
            System.out.println("Error: API key is not set.");
        }
    }

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

    private String getChatbotResponse(String message) throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("API key is not set. Please configure the 'apikey' environment variable.");
        }

        // Construct the request body for Gemini API
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
                    "maxOutputTokens": 150
                }
            }
            """.formatted(message.replace("\"", "\\\""));

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
                    // Parse Gemini's response
                    return mapper.readTree(responseBody)
                            .get("candidates").get(0)
                            .get("content").get("parts").get(0)
                            .get("text").asText().trim();
                } else if (response.code() == 429) {
                    attempt++;
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    String retryAfter = response.header("Retry-After");
                    long delayMs = retryAfter != null ? Long.parseLong(retryAfter) * 1000 : BASE_RETRY_DELAY_MS * attempt;
                    String retryMessage = "Rate limit exceeded: " + errorBody + ". Retrying after " + delayMs + "ms (Attempt " + attempt + "/" + MAX_RETRIES + ")";
                    System.out.println(retryMessage);
                    if (attempt >= MAX_RETRIES) {
                        throw new IOException("Rate limit exceeded after " + MAX_RETRIES + " attempts: " + errorBody);
                    }
                    Thread.sleep(delayMs);
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    throw new IOException("Unexpected code " + response.code() + ": " + errorBody);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted during retry delay: " + e.getMessage());
            }
        }
        throw new IOException("Failed to get response after " + MAX_RETRIES + " attempts");
    }
}