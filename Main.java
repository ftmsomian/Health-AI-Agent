import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONObject;
import org.json.JSONArray;

public class Main {
    private static final String BASE_URL = "http://localhost:11434";
    private static final String DB_URL = "jdbc:h2:tcp://localhost:8082/test";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static final String EMBEDDING_MODEL = "nomic-embed-text";
    private static final String LLM_MODEL = "llama3.2";
    private static final List<String> CSV_FILES = List.of("FoodsandBeverages.csv", "megaGymDataset.csv");

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Step 1: Create embeddings table and process CSV files
            createEmbeddingTable(conn);
            for (String csvFile : CSV_FILES) {
                processCsvFile(csvFile, conn);
            }

            // Step 2: Collect user inputs
            String prompt = collectUserInputs();

            // Step 3: Generate embedding for user input and find similar records
            float[] embedding = getEmbedding(prompt);
            if (embedding != null) {
                List<String> similarRecords = findSimilarEmbeddings(conn, embedding, 50);
                System.out.println("Similar records: " + similarRecords);

                // Step 4: Send prompt and similar data to LLM
                String response = sendToLLM(prompt + " Similar data: " + similarRecords);
                System.out.println("Generated plan: " + response);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void createEmbeddingTable(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS embeddings (id INT AUTO_INCREMENT PRIMARY KEY, text CLOB, embedding CLOB)")) {
            stmt.executeUpdate();
        }
    }

    private static void processCsvFile(String csvFile, Connection conn) throws IOException, SQLException {
        try (FileReader reader = new FileReader("CSV files/" + csvFile);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : parser) {
                String text = String.join(", ", record.toMap().values());
                float[] embedding = getEmbedding(text);
                if (embedding != null) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO embeddings (text, embedding) VALUES (?, ?)")) {
                        stmt.setString(1, text);
                        stmt.setString(2, new JSONArray(embedding).toString());
                        stmt.executeUpdate();
                    }
                }
            }
        }
    }

    private static String collectUserInputs() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your age: ");
        int age = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter your height (in cm): ");
        int height = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter your weight (in kg): ");
        int weight = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Select your gender:");
        System.out.println("1. Male");
        System.out.println("2. Female");
        System.out.println("3. Other");
        System.out.print("Enter the number corresponding to your gender: ");
        int genderOption = scanner.nextInt();
        scanner.nextLine();
        String gender = genderOption == 1 ? "Male" : genderOption == 2 ? "Female" : "Other";

        System.out.println("Select your work lifestyle:");
        System.out.println("1. Sedentary");
        System.out.println("2. Moderate");
        System.out.println("3. Active");
        System.out.print("Enter the number corresponding to your lifestyle: ");
        int lifestyleOption = scanner.nextInt();
        scanner.nextLine();
        String lifestyle = lifestyleOption == 1 ? "Sedentary" : lifestyleOption == 2 ? "Moderate" : "Active";

        System.out.print("Rate your preference for fast food (1-10): ");
        int fastFoodPreference = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Rate your health (1-10): ");
        int healthRating = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Rate the importance of exercise (1-10): ");
        int exerciseImportance = scanner.nextInt();
        scanner.nextLine();

        System.out.print("How many meals do you eat per day? ");
        int mealsPerDay = scanner.nextInt();
        scanner.nextLine();

        System.out.println("What is your preferred exercise type?");
        System.out.println("1. Cardio");
        System.out.println("2. Strength");
        System.out.println("3. Yoga");
        System.out.println("4. Pilates");
        System.out.print("Enter the number corresponding to your preference: ");
        int exerciseTypeOption = scanner.nextInt();
        scanner.nextLine();
        String exerciseType = exerciseTypeOption == 1 ? "Cardio" : exerciseTypeOption == 2 ? "Strength" : exerciseTypeOption == 3 ? "Yoga" : "Pilates";

        System.out.print("How many meals contain fruits? ");
        int fruitMeals = scanner.nextInt();
        scanner.nextLine();

        System.out.print("How many meals contain vegetables? ");
        int vegetableMeals = scanner.nextInt();
        scanner.nextLine();

        System.out.print("How many cooked meals do you eat per day? ");
        int cookedMeals = scanner.nextInt();
        scanner.nextLine();

        System.out.print("How much time do you spend in the gym daily (in minutes)? ");
        int gymTime = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Do you have any allergies? ");
        String allergies = scanner.nextLine();

        System.out.println("Do you follow a specific diet?");
        System.out.println("1. None");
        System.out.println("2. Vegan");
        System.out.println("3. Vegetarian");
        System.out.println("4. Keto");
        System.out.println("5. Halal");
        System.out.print("Enter the number corresponding to your diet: ");
        int dietOption = scanner.nextInt();
        scanner.nextLine();
        String diet = dietOption == 1 ? "None" : dietOption == 2 ? "Vegan" : dietOption == 3 ? "Vegetarian" : dietOption == 4 ? "Keto" : "Halal";

        System.out.println("What is your primary fitness goal?");
        System.out.println("1. Lose Weight");
        System.out.println("2. Gain Muscle");
        System.out.println("3. Maintain Weight");
        System.out.print("Enter the number corresponding to your goal: ");
        int goalOption = scanner.nextInt();
        scanner.nextLine();
        String goal = goalOption == 1 ? "Lose Weight" : goalOption == 2 ? "Gain Muscle" : "Maintain Weight";

        int idealWeight = 0;
        if (goal.equals("Lose Weight")) {
            System.out.print("Enter your ideal weight: ");
            idealWeight = scanner.nextInt();
            scanner.nextLine();
        }

        System.out.print("Do you have any regular diseases? (Yes/No): ");
        String disease = scanner.nextLine();

        System.out.print("Do you have any additional comments about this survey? ");
        String comments = scanner.nextLine();

        String prompt = String.format("User information: %d years old, %s, %d cm tall, %d kg, %s lifestyle. " +
                        "Fast food preference: %d/10, Health: %d/10, Exercise: %d/10. " +
                        "%d meals/day, %d fruit meals, %d vegetable meals, %d cooked meals, %d min gym time. " +
                        "Allergies: %s, Diet: %s, Goal: %s, Diseases: %s, Comments: %s",
                age, gender, height, weight, lifestyle, fastFoodPreference, healthRating, exerciseImportance,
                mealsPerDay, fruitMeals, vegetableMeals, cookedMeals, gymTime, allergies, diet, goal, disease, comments);

        scanner.close();
        return prompt; // Return the constructed prompt
    }

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
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            JSONArray embeddingArray = new JSONObject(response.toString()).getJSONArray("embedding");
            float[] embedding = new float[embeddingArray.length()];
            for (int i = 0; i < embeddingArray.length(); i++) {
                embedding[i] = embeddingArray.getFloat(i);
            }
            return embedding;
        }
        return null;
    }

    private static List<String> findSimilarEmbeddings(Connection conn, float[] embedding, int limit) throws SQLException {
        List<String> similarRecords = new ArrayList<>();
        // Implement proper similarity search (e.g., cosine similarity)
        try (PreparedStatement stmt = conn.prepareStatement("SELECT text FROM embeddings LIMIT ?")) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    similarRecords.add(rs.getString("text"));
                }
            }
        }
        return similarRecords;
    }

    private static String sendToLLM(String prompt) throws IOException {
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
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            return response.toString();
        }
        return null;
    }
}
