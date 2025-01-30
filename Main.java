import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.json.JSONObject;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Collect user inputs with options/examples
        System.out.print("Enter your age: ");
        int age = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        System.out.print("Enter your gender (Male/Female/Other): ");
        String gender = scanner.nextLine();

        System.out.print("Select your work lifestyle (Sedentary, Moderate, Active): ");
        String workPreference = scanner.nextLine();

        System.out.print("Rate your liking for fast food (1-10, 1=Dislike, 10=Love it): ");
        int fastFoodRating = scanner.nextInt();

        System.out.print("Rate your overall health (1-10, 1=Poor, 10=Excellent): ");
        int healthRating = scanner.nextInt();

        System.out.print("Rate your preference to maintain body weight (1-10, 1=Not at all, 10=Very important): ");
        int bodyWeightPreference = scanner.nextInt();

        System.out.print("Rate the importance of exercise in your life (1-10, 1=Not important, 10=Essential): ");
        int exerciseImportance = scanner.nextInt();

        System.out.print("How many meals do you have in a day (e.g., 3, 4, 5)? ");
        int mealsPerDay = scanner.nextInt();

        System.out.print("Enter your weight (in kg, e.g., 70): ");
        int weight = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        System.out.print("What type of exercises do you prefer? (e.g., Cardio, Strength, Yoga, Pilates): ");
        String exerciseType = scanner.nextLine();

        System.out.print("How many meals contain fruits per day (e.g., 1, 2, 3)? ");
        int fruitMeals = scanner.nextInt();

        System.out.print("How many meals contain vegetables per day (e.g., 1, 2, 3)? ");
        int vegetableMeals = scanner.nextInt();

        System.out.print("How many cooked meals do you eat per day (e.g., 1, 2, 3)? ");
        int cookedMeals = scanner.nextInt();

        System.out.print("How much time do you spend in the gym per day (in minutes, e.g., 30, 60)? ");
        int gymTime = scanner.nextInt();

        scanner.nextLine(); // Consume newline
        System.out.print("Do you suffer from any regular disease? (Yes/No): ");
        String disease = scanner.nextLine();

        String review = "";
        if (disease.equalsIgnoreCase("Yes")) {
            System.out.print("Please provide a short review about this survey: ");
            review = scanner.nextLine();
        }

        // Generate a dynamic prompt based on user inputs
        String prompt = String.format(
                "Based on the following user information, generate a detailed personalized diet and exercise plan for 7 days(I want the complete and detailed plans for every day). " +
                        "The user is %d years old, %s, with a %s work lifestyle. They rate their fast food preference as %d/10, " +
                        "health as %d/10, and importance of exercise as %d/10. They have %d meals a day, prefer %s workouts, " +
                        "consume fruits in %d meals, vegetables in %d meals, and eat %d cooked meals per day. They spend %d minutes " +
                        "in the gym daily. The user %s suffers from a regular disease. " +
                        "Please provide a structured and actionable diet and exercise plan tailored to their lifestyle.",
                age, gender, workPreference, fastFoodRating, healthRating, exerciseImportance, mealsPerDay, exerciseType,
                fruitMeals, vegetableMeals, cookedMeals, gymTime, disease.equalsIgnoreCase("Yes") ? "does" : "does not"
        );

        // Create the JSON payload
        String payload = String.format(
                "{\"model\": \"llama3.2\", " +
                        "\"prompt\": \"%s\", " +
                        "\"age\": %d, " +
                        "\"gender\": \"%s\", " +
                        "\"workPreference\": \"%s\", " +
                        "\"fastFoodRating\": %d, " +
                        "\"healthRating\": %d, " +
                        "\"bodyWeightPreference\": %d, " +
                        "\"exerciseImportance\": %d, " +
                        "\"mealsPerDay\": %d, " +
                        "\"weight\": %d, " +
                        "\"exerciseType\": \"%s\", " +
                        "\"fruitMeals\": %d, " +
                        "\"vegetableMeals\": %d, " +
                        "\"cookedMeals\": %d, " +
                        "\"gymTime\": %d, " +
                        "\"disease\": \"%s\", " +
                        "\"review\": \"%s\"}",
                prompt, age, gender, workPreference, fastFoodRating, healthRating, bodyWeightPreference, exerciseImportance,
                mealsPerDay, weight, exerciseType, fruitMeals, vegetableMeals, cookedMeals, gymTime, disease, review
        );

        try {
            // Send the request to the LLM API
            String response = sendToLLM(payload);
            System.out.println("Response from server: " + response);
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }

        scanner.close();
    }

    private static String sendToLLM(String payload) throws Exception {
        String urlString = "http://127.0.0.1:11434/api/generate";
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
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
            throw new RuntimeException("Failed to send request. Response code: " + responseCode);
        }
    }
}
