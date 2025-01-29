import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class Main {
    private static final String API_KEY = "iFHbWHdrLRrOQ223YivJbTvszOZ7tRC9yOdLsGHd"; // Replace with your valid API key
    private static final String API_URL = "https://api.cohere.com/v1/chat"; // Corrected API URL

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        HashMap<String, String> userInfo = new HashMap<>();

        // Collect user information
        System.out.print("What is your age? ");
        userInfo.put("Age", scanner.nextLine());

        System.out.print("What is your gender? (Male/Female/Other) ");
        userInfo.put("Gender", scanner.nextLine());

        System.out.print("What is your current height (in cm)? ");
        userInfo.put("Height", scanner.nextLine());

        System.out.print("What is your current weight (in kg)? ");
        userInfo.put("Weight", scanner.nextLine());

        System.out.println("What are your primary fitness goals? ");
        userInfo.put("Fitness Goals", scanner.nextLine());

        // Construct prompt
        StringBuilder userInputText = new StringBuilder();
        userInfo.forEach((key, value) -> userInputText.append(key).append(": ").append(value).append("\n"));
        userInputText.append("\nPlease provide two detailed plans based on the above information. ")
                .append("The first should be a personalized diet plan, and the second should be a personalized exercise plan.");

        // Send request to Cohere API
        try {
            String response = sendToCohere(userInputText.toString());
            System.out.println("\nResponse from Cohere:");
            System.out.println(response);
        } catch (Exception e) {
            System.out.println("An error occurred while communicating with Cohere API: " + e.getMessage());
        }

        scanner.close();
    }

    private static String sendToCohere(String userMessage) throws IOException {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set request properties
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);

        // Create JSON request body
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("model", "command-r-plus-08-2024");

        // Add messages array (Cohere needs a system message first)
        JSONArray messagesArray = new JSONArray();
        messagesArray.put(new JSONObject().put("role", "system").put("content", "You are an expert fitness and diet planner."));
        messagesArray.put(new JSONObject().put("role", "user").put("content", userMessage));

        jsonRequest.put("messages", messagesArray);

        // Send request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonRequest.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Read the response
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                throw new IOException("Server returned HTTP response code: " + responseCode + " - " + errorResponse);
            }
            throw new IOException("Server returned HTTP response code: " + responseCode);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            // Parse response JSON
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONObject message = jsonResponse.getJSONObject("message");
            JSONArray contentArray = message.getJSONArray("content");

            // Extract assistant response
            StringBuilder assistantResponse = new StringBuilder();
            for (int i = 0; i < contentArray.length(); i++) {
                assistantResponse.append(contentArray.getJSONObject(i).getString("text")).append("\n");
            }

            return assistantResponse.toString();
        }
    }
}
