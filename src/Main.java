import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONObject;

public class Main {
    private static final String BASE_URL = "http://localhost:11434";
    private static final String EMBEDDING_MODEL = "nomic-embed-text"; // Replace with the correct model name
    private static final String LLM_MODEL = "llama3.2"; // Replace with the correct LLM model name

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Step 1: Collect user input
        Map<String, String> userInput = collectUserInput(scanner);

        // Step 2: Load CSV data
        List<CSVRecord> csvData1 = loadCSV("CSV files/large_food_nutrition.csv");
        List<CSVRecord> csvData2 = loadCSV("CSV files/sport_exercises.csv");

        // Step 3: Calculate embeddings for CSV data
        Map<String, float[]> embeddingsMap1 = calculateEmbeddings(csvData1);
        Map<String, float[]> embeddingsMap2 = calculateEmbeddings(csvData2);

        // Combine embeddings into a single map
        Map<String, float[]> allEmbeddings = new HashMap<>();
        allEmbeddings.putAll(embeddingsMap1);
        allEmbeddings.putAll(embeddingsMap2);

        // Step 4: Calculate user input embedding
        String userInputText = String.join(" ", userInput.values());
        float[] userEmbedding = new float[0];
        try {
            userEmbedding = getEmbedding(userInputText);
        } catch (IOException e) {
            System.err.println("Failed to get embedding for user input: " + e.getMessage());
            return;
        }

        // Step 5: Find similar vectors
        List<String> similarIds = findSimilarVectors(userEmbedding, allEmbeddings, 50);

        // Step 6: Generate enhanced prompt
        String originalPrompt = generateOriginalPrompt(userInput);
        Map<String, CSVRecord> recordsMap = new HashMap<>();
        for (CSVRecord record : csvData1) recordsMap.put(generateKey(record), record);
        for (CSVRecord record : csvData2) recordsMap.put(generateKey(record), record);
        String enhancedPrompt = generateEnhancedPrompt(originalPrompt, similarIds, recordsMap);

        // Step 7: Send enhanced prompt to LLM
        try {
            String response = sendToLLM(enhancedPrompt);
            System.out.println("Response from server: " + response);
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }

        scanner.close();
    }

    // Generate a unique key for a CSVRecord
    private static String generateKey(CSVRecord record) {
        StringBuilder keyBuilder = new StringBuilder();
        for (String value : record) {
            keyBuilder.append(value).append("|");
        }
        return keyBuilder.toString();
    }

    // Step 1: Collect user input
    private static Map<String, String> collectUserInput(Scanner scanner) {
        Map<String, String> userInput = new HashMap<>();

        System.out.print("Enter your age: ");
        userInput.put("age", scanner.nextLine());

        System.out.print("Enter your height (in cm): ");
        userInput.put("height", scanner.nextLine());

        System.out.print("Enter your weight (in kg): ");
        userInput.put("weight", scanner.nextLine());

        System.out.println("Select your gender:");
        System.out.println("1. Male");
        System.out.println("2. Female");
        System.out.println("3. Other");
        System.out.print("Enter the number corresponding to your gender: ");
        int genderOption = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        String gender = genderOption == 1 ? "Male" : genderOption == 2 ? "Female" : "Other";
        userInput.put("gender", gender);

        System.out.println("Select your work lifestyle:");
        System.out.println("1. Sedentary");
        System.out.println("2. Moderate");
        System.out.println("3. Active");
        System.out.print("Enter the number corresponding to your work lifestyle: ");
        int workPreferenceOption = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        String workPreference = workPreferenceOption == 1 ? "Sedentary" : workPreferenceOption == 2 ? "Moderate" : "Active";
        userInput.put("workPreference", workPreference);

        System.out.print("Rate your liking for fast food (1-10): ");
        userInput.put("fastFoodRating", scanner.nextLine());

        System.out.print("Rate your overall health (1-10): ");
        userInput.put("healthRating", scanner.nextLine());

        System.out.print("Rate your preference to maintain body weight (1-10): ");
        userInput.put("bodyWeightPreference", scanner.nextLine());

        System.out.print("Rate the importance of exercise in your life (1-10): ");
        userInput.put("exerciseImportance", scanner.nextLine());

        System.out.print("How many meals do you have in a day? ");
        userInput.put("mealsPerDay", scanner.nextLine());

        System.out.println("What type of exercises do you prefer?");
        System.out.println("1. Cardio");
        System.out.println("2. Strength");
        System.out.println("3. Yoga");
        System.out.println("4. Pilates");
        System.out.print("Enter the number corresponding to your preferred exercise type: ");
        int exerciseTypeOption = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        String exerciseType = exerciseTypeOption == 1 ? "Cardio" : exerciseTypeOption == 2 ? "Strength" : exerciseTypeOption == 3 ? "Yoga" : "Pilates";
        userInput.put("exerciseType", exerciseType);

        System.out.print("How many meals contain fruits per day? ");
        userInput.put("fruitMeals", scanner.nextLine());

        System.out.print("How many meals contain vegetables per day? ");
        userInput.put("vegetableMeals", scanner.nextLine());

        System.out.print("How many cooked meals do you eat per day? ");
        userInput.put("cookedMeals", scanner.nextLine());

        System.out.print("How much time do you spend in the gym per day (in minutes)? ");
        userInput.put("gymTime", scanner.nextLine());

        System.out.print("Do you have any food allergies? (e.g., None, Peanuts, Gluten, Dairy): ");
        userInput.put("allergies", scanner.nextLine());

        System.out.println("Do you follow any specific diet or have food restrictions?");
        System.out.println("1. None");
        System.out.println("2. Vegan");
        System.out.println("3. Vegetarian");
        System.out.println("4. Keto");
        System.out.println("5. Halal");
        System.out.print("Enter the number corresponding to your diet: ");
        int foodRestrictionsOption = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        String foodRestrictions = foodRestrictionsOption == 1 ? "None" : foodRestrictionsOption == 2 ? "Vegan" : foodRestrictionsOption == 3 ? "Vegetarian" : foodRestrictionsOption == 4 ? "Keto" : "Halal";
        userInput.put("foodRestrictions", foodRestrictions);

        System.out.println("What is your primary fitness goal?");
        System.out.println("1. Lose Weight");
        System.out.println("2. Gain Muscles");
        System.out.println("3. Maintain Weight");
        System.out.print("Enter the number corresponding to your goal: ");
        int goalOption = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        String goal = goalOption == 1 ? "Lose Weight" : goalOption == 2 ? "Gain Muscles" : "Maintain Weight";
        userInput.put("goal", goal);

        return userInput;
    }

    // Step 2: Load CSV data
    private static List<CSVRecord> loadCSV(String filePath) {
        List<CSVRecord> records = new ArrayList<>();
        try (Reader reader = new FileReader(filePath)) {
            CSVFormat format = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreSurroundingSpaces(true)
                    .withTrim()
                    .withIgnoreEmptyLines(true); // Skip empty lines

            CSVParser parser = new CSVParser(reader, format);

            int lineNumber = 0; // Keep track of line numbers for debugging
            for (CSVRecord record : parser) {
                lineNumber++;
                try {
                    // Add valid records to the list
                    records.add(record);
                } catch (Exception ex) {
                    // Log the problematic record for debugging and skip it
                    System.err.println("Skipping invalid record at line " + lineNumber + ": " + record);
                    ex.printStackTrace();
                }
            }
        } catch (IOException e) {
            // Handle file-level errors
            System.err.println("Error reading CSV file: " + filePath);
            e.printStackTrace();
        }
        return records;
    }

    // Step 3: Calculate embeddings for CSV data
    private static Map<String, float[]> calculateEmbeddings(List<CSVRecord> records) {
        Map<String, float[]> embeddingsMap = new HashMap<>();
        for (CSVRecord record : records) {
            try {
                // Generate a unique key by concatenating all values in the record
                String key = generateKey(record);

                // Generate embedding for the entire record
                String text = record.toString();
                float[] embedding = getEmbedding(text);
                embeddingsMap.put(key, embedding);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return embeddingsMap;
    }

    // Step 4: Get embedding for a given text
    private static float[] getEmbedding(String text) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + "/api/embeddings").openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        JSONObject payload = new JSONObject();
        payload.put("model", EMBEDDING_MODEL);
        payload.put("input", text);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
        }

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8);
            StringBuilder responseBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                responseBuilder.append(scanner.nextLine());
            }
            scanner.close();

            JSONObject responseJson = new JSONObject(responseBuilder.toString());
            return parseEmbedding(responseJson);
        } else {
            throw new IOException("Failed to get embedding. Response code: " + connection.getResponseCode());
        }
    }

    // Parse embedding from JSON response
    private static float[] parseEmbedding(JSONObject responseJson) {
        List<Object> embeddingList = responseJson.getJSONArray("embedding").toList();
        float[] embedding = new float[embeddingList.size()];
        for (int i = 0; i < embeddingList.size(); i++) {
            embedding[i] = ((Number) embeddingList.get(i)).floatValue();
        }
        return embedding;
    }

    // Step 5: Find similar vectors
    private static List<String> findSimilarVectors(float[] userEmbedding, Map<String, float[]> embeddingsMap, int topK) {
        Map<String, Double> similarityScores = new HashMap<>();
        for (Map.Entry<String, float[]> entry : embeddingsMap.entrySet()) {
            String id = entry.getKey();
            float[] embedding = entry.getValue();
            double similarity = cosineSimilarity(userEmbedding, embedding);
            similarityScores.put(id, similarity);
        }

        List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(similarityScores.entrySet());
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        List<String> topKIds = new ArrayList<>();
        for (int i = 0; i < Math.min(topK, sortedEntries.size()); i++) {
            topKIds.add(sortedEntries.get(i).getKey());
        }
        return topKIds;
    }

    // Calculate cosine similarity
    private static double cosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // Step 6: Generate enhanced prompt
    private static String generateEnhancedPrompt(String originalPrompt, List<String> similarIds, Map<String, CSVRecord> recordsMap) {
        StringBuilder enhancedPrompt = new StringBuilder(originalPrompt);
        enhancedPrompt.append("\n\nRelevant Data:\n");
        for (String id : similarIds) {
            CSVRecord record = recordsMap.get(id);
            enhancedPrompt.append(record.toString()).append("\n");
        }
        return enhancedPrompt.toString();
    }

    // Generate original prompt from user input
    private static String generateOriginalPrompt(Map<String, String> userInput) {
        return String.format(
                "Based on the following user information, generate a detailed personalized diet and exercise plan." +
                        "(please give complete plans for every day of week for meals and workout sessions(7 plans for diet and 7 plans for workout). I want plan for every single day of week. and please mention the Amount of each food item in diet plan. and please give a concise and short explanation about every sport movement item in plan about how the user should do that) " +
                        "The user is %s years old, %s, with a height of %s cm and weight of %s kg. They have a %s work lifestyle. " +
                        "They rate their fast food preference as %s/10, health as %s/10, and importance of exercise as %s/10. " +
                        "They have %s meals a day, prefer %s workouts, consume fruits in %s meals, vegetables in %s meals, " +
                        "and eat %s cooked meals per day. They spend %s minutes in the gym daily. " +
                        "The user has the following food allergies: %s and follows this diet: %s. " +
                        "Their primary fitness goal is to %s. " +
                        "Please provide a structured and actionable diet and exercise plan tailored to their lifestyle.",
                userInput.get("age"), userInput.get("gender"), userInput.get("height"), userInput.get("weight"),
                userInput.get("workPreference"), userInput.get("fastFoodRating"), userInput.get("healthRating"),
                userInput.get("exerciseImportance"), userInput.get("mealsPerDay"), userInput.get("exerciseType"),
                userInput.get("fruitMeals"), userInput.get("vegetableMeals"), userInput.get("cookedMeals"),
                userInput.get("gymTime"), userInput.get("allergies"), userInput.get("foodRestrictions"),
                userInput.get("goal")
        );
    }

    // Step 7: Send prompt to LLM
    private static String sendToLLM(String prompt) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + "/api/generate").openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        JSONObject payload = new JSONObject();
        payload.put("model", LLM_MODEL);
        payload.put("prompt", prompt);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
        }

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8);
            StringBuilder responseBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                JSONObject jsonObject = new JSONObject(line);
                responseBuilder.append(jsonObject.getString("response"));
                if (jsonObject.getBoolean("done")) {
                    break;
                }
            }
            scanner.close();
            return responseBuilder.toString();
        } else {
            throw new RuntimeException("Failed to send request. Response code: " + connection.getResponseCode());
        }
    }
}
