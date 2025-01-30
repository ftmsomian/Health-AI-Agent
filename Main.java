import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.json.JSONObject;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Collect user inputs with number-based options
        System.out.print("Enter your age: ");
        int age = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        System.out.print("Enter your height (in cm, e.g., 170): ");
        int height = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        System.out.print("Enter your weight (in kg, e.g., 70): ");
        int weight = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        System.out.println("Select your gender:");
        System.out.println("1. Male");
        System.out.println("2. Female");
        System.out.println("3. Other");
        System.out.print("Enter the number corresponding to your gender: ");
        int genderOption = scanner.nextInt();
        String gender = genderOption == 1 ? "Male" : genderOption == 2 ? "Female" : "Other";

        System.out.println("Select your work lifestyle:");
        System.out.println("1. Sedentary");
        System.out.println("2. Moderate");
        System.out.println("3. Active");
        System.out.print("Enter the number corresponding to your work lifestyle: ");
        int workPreferenceOption = scanner.nextInt();
        String workPreference = workPreferenceOption == 1 ? "Sedentary" : workPreferenceOption == 2 ? "Moderate" : "Active";

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

        System.out.println("What type of exercises do you prefer?");
        System.out.println("1. Cardio");
        System.out.println("2. Strength");
        System.out.println("3. Yoga");
        System.out.println("4. Pilates");
        System.out.print("Enter the number corresponding to your preferred exercise type: ");
        int exerciseTypeOption = scanner.nextInt();
        String exerciseType = exerciseTypeOption == 1 ? "Cardio" : exerciseTypeOption == 2 ? "Strength" : exerciseTypeOption == 3 ? "Yoga" : "Pilates";

        System.out.print("How many meals contain fruits per day (e.g., 1, 2, 3)? ");
        int fruitMeals = scanner.nextInt();

        System.out.print("How many meals contain vegetables per day (e.g., 1, 2, 3)? ");
        int vegetableMeals = scanner.nextInt();

        System.out.print("How many cooked meals do you eat per day (e.g., 1, 2, 3)? ");
        int cookedMeals = scanner.nextInt();

        System.out.print("How much time do you spend in the gym per day (in minutes, e.g., 30, 60)? ");
        int gymTime = scanner.nextInt();

        scanner.nextLine(); // Consume newline
        System.out.print("Do you have any food allergies? (e.g., None, Peanuts, Gluten, Dairy): ");
        String allergies = scanner.nextLine();

        System.out.println("Do you follow any specific diet or have food restrictions?");
        System.out.println("1. None");
        System.out.println("2. Vegan");
        System.out.println("3. Vegetarian");
        System.out.println("4. Keto");
        System.out.println("5. Halal");
        System.out.print("Enter the number corresponding to your diet: ");
        int foodRestrictionsOption = scanner.nextInt();
        String foodRestrictions = foodRestrictionsOption == 1 ? "None" : foodRestrictionsOption == 2 ? "Vegan" : foodRestrictionsOption == 3 ? "Vegetarian" : foodRestrictionsOption == 4 ? "Keto" : "Halal";

        System.out.println("What is your primary fitness goal?");
        System.out.println("1. Lose Weight");
        System.out.println("2. Gain Muscles");
        System.out.println("3. Maintain Weight");
        System.out.print("Enter the number corresponding to your goal: ");
        int goalOption = scanner.nextInt();
        String goal = goalOption == 1 ? "Lose Weight" : goalOption == 2 ? "Gain Muscles" : "Maintain Weight";

        // Ask for ideal weight if the goal is weight loss
        int idealWeight = 0;
        if (goal.equalsIgnoreCase("Lose Weight")) {
            System.out.print("Enter your ideal weight (in kg, e.g., 65): ");
            idealWeight = scanner.nextInt();
            scanner.nextLine(); // Consume newline
        }

        System.out.print("Do you suffer from any regular disease? (1. Yes / 2. No): ");
        int diseaseOption = scanner.nextInt();
        String disease = diseaseOption == 1 ? "Yes" : "No";

        String review = "";
        if (disease.equalsIgnoreCase("Yes")) {
            scanner.nextLine(); // Consume newline
            System.out.print("Please provide a short review about this survey: ");
            review = scanner.nextLine();
        }

        // Generate a dynamic prompt based on user inputs
        String prompt = String.format(
                "Based on the following user information, generate a personalized diet and exercise plan. " +
                        "The user is %d years old, %s, with a height of %d cm and weight of %d kg. They have a %s work lifestyle. " +
                        "They rate their fast food preference as %d/10, health as %d/10, and importance of exercise as %d/10. " +
                        "They have %d meals a day, prefer %s workouts, consume fruits in %d meals, vegetables in %d meals, " +
                        "and eat %d cooked meals per day. They spend %d minutes in the gym daily. " +
                        "The user has the following food allergies: %s and follows this diet: %s. " +
                        "Their primary fitness goal is to %s. " +
                        (goal.equalsIgnoreCase("Lose Weight") ? "Their ideal weight is %d kg. " : "") +
                        "The user %s suffers from a regular disease. " +
                        "Please provide a structured and actionable diet and exercise plan tailored to their lifestyle.",
                age, gender, height, weight, workPreference, fastFoodRating, healthRating, exerciseImportance, mealsPerDay,
                exerciseType, fruitMeals, vegetableMeals, cookedMeals, gymTime, allergies, foodRestrictions, goal,
                (goal.equalsIgnoreCase("Lose Weight") ? idealWeight : null), // Add ideal weight if applicable
                disease.equalsIgnoreCase("Yes") ? "does" : "does not"
        );

        // Create the JSON payload
        String payload = String.format(
                "{\"model\": \"llama3.2\", " +
                        "\"prompt\": \"%s\", " +
                        "\"age\": %d, " +
                        "\"height\": %d, " +
                        "\"weight\": %d, " +
                        "\"gender\": \"%s\", " +
                        "\"workPreference\": \"%s\", " +
                        "\"fastFoodRating\": %d, " +
                        "\"healthRating\": %d, " +
                        "\"bodyWeightPreference\": %d, " +
                        "\"exerciseImportance\": %d, " +
                        "\"mealsPerDay\": %d, " +
                        "\"exerciseType\": \"%s\", " +
                        "\"fruitMeals\": %d, " +
                        "\"vegetableMeals\": %d, " +
                        "\"cookedMeals\": %d, " +
                        "\"gymTime\": %d, " +
                        "\"allergies\": \"%s\", " +
                        "\"foodRestrictions\": \"%s\", " +
                        "\"goal\": \"%s\", " +
                        (goal.equalsIgnoreCase("Lose Weight") ? "\"idealWeight\": %d, " : "") +
                        "\"disease\": \"%s\", " +
                        "\"review\": \"%s\"}",
                prompt, age, height, weight, gender, workPreference, fastFoodRating, healthRating, bodyWeightPreference,
                exerciseImportance, mealsPerDay, exerciseType, fruitMeals, vegetableMeals, cookedMeals, gymTime,
                allergies, foodRestrictions, goal,
                (goal.equalsIgnoreCase("Lose Weight") ? idealWeight : null), // Add ideal weight if applicable
                disease, review
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
